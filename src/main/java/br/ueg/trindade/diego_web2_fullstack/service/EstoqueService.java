package br.ueg.trindade.diego_web2_fullstack.service;

import br.ueg.trindade.diego_web2_fullstack.model.MovimentacaoEstoque;
import br.ueg.trindade.diego_web2_fullstack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service @Transactional
public class EstoqueService {
    public record Pedido(MovimentacaoEstoque.Tipo tipo, BigDecimal quantidade, String motivo, String chave) {}
    public record Historico(List<MovimentacaoEstoque> movimentacoes, long total) {}
    public record Resumo(int produtos, long unidades, int estoqueBaixo, BigDecimal valorEstoque) {}
    private final ProdutoRepository produtos;
    private final MovimentacaoEstoqueRepository movimentacoes;
    public EstoqueService(ProdutoRepository produtos, MovimentacaoEstoqueRepository movimentacoes) { this.produtos = produtos; this.movimentacoes = movimentacoes; }
    public MovimentacaoEstoque movimentar(Long id, Pedido pedido, Long usuarioId) {
        if (pedido.chave() == null || !pedido.chave().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador da operação obrigatório e inválido. Atualize a tela e tente novamente.");
        if (pedido.tipo() == null || pedido.quantidade() == null || pedido.quantidade().stripTrailingZeros().scale() > 0 || pedido.quantidade().compareTo(BigDecimal.ONE) < 0 || pedido.quantidade().compareTo(BigDecimal.valueOf(1000000)) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe entrada ou saída e uma quantidade inteira entre 1 e 1000000.");
        if (pedido.motivo() == null || pedido.motivo().trim().length() < 3 || pedido.motivo().length() > 255)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o motivo da movimentação com 3 a 255 caracteres.");
        var produto = produtos.buscarParaAtualizar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        String chave = pedido.chave().toLowerCase(java.util.Locale.ROOT);
        var repetida = movimentacoes.findByChave(chave).orElse(null);
        if (repetida != null) {
            if (!repetida.getProdutoId().equals(id) || !repetida.getUsuarioId().equals(usuarioId) || repetida.getTipo() != pedido.tipo() || repetida.getQuantidade() != pedido.quantidade().intValueExact() || !repetida.getMotivo().equals(pedido.motivo().trim()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador da operação já utilizado com outros dados.");
            return repetida;
        }
        int anterior = produto.getQuantidade();
        int quantidade = pedido.quantidade().intValueExact();
        long novo = anterior + (pedido.tipo() == MovimentacaoEstoque.Tipo.ENTRADA ? (long) quantidade : -(long) quantidade);
        if (novo < 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Saldo insuficiente. Disponível: " + anterior + " unidades.");
        if (novo > 1000000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O saldo máximo é de 1000000 unidades por produto.");
        produto.setQuantidade((int) novo);
        produtos.save(produto);
        return movimentacoes.save(new MovimentacaoEstoque(id, usuarioId, pedido.tipo(), quantidade, anterior, (int) novo, pedido.motivo().trim(), chave));
    }
    @Transactional(readOnly = true)
    public Historico historico(Long id) {
        if (!produtos.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
        return new Historico(movimentacoes.findTop100ByProdutoIdOrderByIdDesc(id), movimentacoes.countByProdutoId(id));
    }
    @Transactional(readOnly = true)
    public Resumo resumo() {
        var lista = produtos.findAll();
        long unidades = 0; int baixo = 0; BigDecimal valor = BigDecimal.ZERO;
        for (var p : lista) {
            unidades += p.getQuantidade();
            if (p.getQuantidade() <= p.getEstoqueMinimo()) baixo++;
            valor = valor.add(p.getPreco().multiply(BigDecimal.valueOf(p.getQuantidade())));
        }
        return new Resumo(lista.size(), unidades, baixo, valor);
    }
}
