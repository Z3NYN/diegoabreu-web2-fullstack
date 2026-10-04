package br.ueg.trindade.diego_web2_fullstack;

import br.ueg.trindade.diego_web2_fullstack.model.*;
import br.ueg.trindade.diego_web2_fullstack.repository.*;
import br.ueg.trindade.diego_web2_fullstack.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:estoquetest", "spring.jpa.hibernate.ddl-auto=create-drop"})
class EstoqueIntegrationTests {
    @Autowired EstoqueService estoque;
    @Autowired ProdutoService catalogo;
    @Autowired ProdutoRepository produtos;
    @Autowired MovimentacaoEstoqueRepository movimentos;
    @BeforeEach void limpar() { movimentos.deleteAll(); produtos.deleteAll(); }
    Produto produto() { var p = new Produto(null, "Teclado", new BigDecimal("10.25")); p.setEstoqueMinimo(2); return catalogo.criar(p); }
    EstoqueService.Pedido pedido(MovimentacaoEstoque.Tipo tipo, String quantidade) { return new EstoqueService.Pedido(tipo, new BigDecimal(quantidade), "Movimentação de teste", UUID.randomUUID().toString()); }
    void status(int esperado, Runnable operacao) { assertEquals(esperado, assertThrows(ResponseStatusException.class, operacao::run).getStatusCode().value()); }
    @Test void entradaSaidaHistoricoEResumoPersistidos() {
        var p = produto();
        var entrada = estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.ENTRADA, "5"), 1L);
        assertEquals(0, entrada.getSaldoAnterior()); assertEquals(5, entrada.getSaldoAtual()); assertEquals(1L, entrada.getUsuarioId());
        assertEquals(new BigDecimal("51.25"), estoque.resumo().valorEstoque());
        assertEquals(0, estoque.resumo().estoqueBaixo());
        estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.SAIDA, "3"), 1L);
        assertEquals(2, catalogo.buscarPorId(p.getId()).getQuantidade());
        assertEquals(2, estoque.historico(p.getId()).total());
        assertEquals(MovimentacaoEstoque.Tipo.SAIDA, estoque.historico(p.getId()).movimentacoes().getFirst().getTipo());
        assertEquals(2, estoque.resumo().unidades()); assertEquals(1, estoque.resumo().estoqueBaixo());
        assertEquals(new BigDecimal("20.50"), estoque.resumo().valorEstoque());
    }
    @Test void validacaoESaidaInsuficienteNaoMudamSaldoOuHistorico() {
        var p = produto();
        for (String q : new String[]{"0", "-1", "1.5", "1000001"}) status(400, () -> estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.ENTRADA, q), 1L));
        status(400, () -> estoque.movimentar(p.getId(), new EstoqueService.Pedido(null, BigDecimal.ONE, "Teste", UUID.randomUUID().toString()), 1L));
        status(400, () -> estoque.movimentar(p.getId(), new EstoqueService.Pedido(MovimentacaoEstoque.Tipo.ENTRADA, BigDecimal.ONE, " ", UUID.randomUUID().toString()), 1L));
        status(400, () -> estoque.movimentar(p.getId(), new EstoqueService.Pedido(MovimentacaoEstoque.Tipo.ENTRADA, BigDecimal.ONE, "Teste", "invalido"), 1L));
        status(409, () -> estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.SAIDA, "1"), 1L));
        assertEquals(0, produtos.findById(p.getId()).orElseThrow().getQuantidade()); assertEquals(0, movimentos.count());
    }
    @Test void saldoMaximoEQuantidadeInteira() {
        var p = produto(); estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.ENTRADA, "1000000"), 1L);
        status(400, () -> estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.ENTRADA, "1"), 1L));
        assertEquals(1, movimentos.count()); assertEquals(1000000, catalogo.buscarPorId(p.getId()).getQuantidade());
    }
    @Test void cadastroNaoAlteraSaldoEHistoricoImpedeExclusao() {
        var p = produto(); estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.ENTRADA, "4"), 1L);
        var alterado = new Produto(null, "Teclado editado", new BigDecimal("12.50")); alterado.setQuantidade(999); alterado.setEstoqueMinimo(3);
        assertEquals(4, catalogo.atualizar(p.getId(), alterado).getQuantidade());
        assertEquals(3, catalogo.buscarPorId(p.getId()).getEstoqueMinimo());
        status(409, () -> catalogo.excluir(p.getId()));
        estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.SAIDA, "4"), 1L);
        status(409, () -> catalogo.excluir(p.getId()));
        var semHistorico = produto(); catalogo.excluir(semHistorico.getId()); assertFalse(produtos.existsById(semHistorico.getId()));
        alterado.setEstoqueMinimo(-1); status(400, () -> catalogo.atualizar(p.getId(), alterado));
    }
    @Test void repeticaoDaOperacaoNaoDuplicaMovimento() {
        var p = produto(); var pedido = pedido(MovimentacaoEstoque.Tipo.ENTRADA, "3");
        var primeiro = estoque.movimentar(p.getId(), pedido, 1L);
        assertEquals(primeiro.getId(), estoque.movimentar(p.getId(), pedido, 1L).getId());
        assertEquals(3, catalogo.buscarPorId(p.getId()).getQuantidade()); assertEquals(1, movimentos.count());
        status(409, () -> estoque.movimentar(p.getId(), new EstoqueService.Pedido(pedido.tipo(), BigDecimal.ONE, pedido.motivo(), pedido.chave()), 1L));
    }
    @Test void saidasConcorrentesNaoDeixamSaldoNegativo() throws Exception {
        var p = produto(); estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.ENTRADA, "5"), 1L);
        var partida = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> tarefa = () -> { partida.await(); try { estoque.movimentar(p.getId(), pedido(MovimentacaoEstoque.Tipo.SAIDA, "4"), 1L); return true; } catch (ResponseStatusException e) { assertEquals(409, e.getStatusCode().value()); return false; } };
            var a = executor.submit(tarefa); var b = executor.submit(tarefa); partida.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
        assertEquals(1, catalogo.buscarPorId(p.getId()).getQuantidade()); assertEquals(2, movimentos.count());
    }
    @Test void duplicatasConcorrentesSaoIdempotentes() throws Exception {
        var p = produto(); var pedido = pedido(MovimentacaoEstoque.Tipo.ENTRADA, "2"); var partida = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Long> tarefa = () -> { partida.await(); return estoque.movimentar(p.getId(), pedido, 1L).getId(); };
            var a = executor.submit(tarefa); var b = executor.submit(tarefa); partida.countDown(); assertEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
        assertEquals(2, catalogo.buscarPorId(p.getId()).getQuantidade()); assertEquals(1, movimentos.count());
    }
    @Test void produtoInexistenteEResumoVazio() {
        assertEquals(0, estoque.resumo().produtos()); assertEquals(0, estoque.resumo().unidades());
        status(404, () -> estoque.historico(999999L));
        status(404, () -> estoque.movimentar(999999L, pedido(MovimentacaoEstoque.Tipo.ENTRADA, "1"), 1L));
    }
}
