package br.ueg.trindade.diego_web2_fullstack.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.ueg.trindade.diego_web2_fullstack.model.Produto;
import br.ueg.trindade.diego_web2_fullstack.repository.ProdutoRepository;
@Service
@Transactional
public class ProdutoService {
    private final ProdutoRepository repository;
    private final br.ueg.trindade.diego_web2_fullstack.repository.MovimentacaoEstoqueRepository movimentacoes;
    public ProdutoService(ProdutoRepository repository, br.ueg.trindade.diego_web2_fullstack.repository.MovimentacaoEstoqueRepository movimentacoes) { this.repository = repository; this.movimentacoes = movimentacoes; }
    public List<Produto> listarTodos() { return repository.findAll(); }
    public Produto buscarPorId(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado")); }
    private void validar(Produto value) {
        if (value.getNome() == null || value.getNome().isBlank() || value.getNome().length() > 120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um nome com até 120 caracteres.");
        if (value.getPreco() == null || value.getPreco().signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço obrigatório e não pode ser negativo");
        if (value.getPreco().stripTrailingZeros().scale() > 2 || value.getPreco().compareTo(new java.math.BigDecimal("999999999.99")) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço deve ter até duas casas decimais e ser no máximo 999999999,99.");
        value.setNome(value.getNome().trim());
        if (value.getEstoqueMinimo() < 0 || value.getEstoqueMinimo() > 1000000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque mínimo deve estar entre 0 e 1000000 unidades.");
    }
    public Produto criar(Produto value) { validar(value); value.setId(null); value.setQuantidade(0); return repository.save(value); }
    public Produto atualizar(Long id, Produto value) {
        Produto current = repository.buscarParaAtualizar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
        validar(value);
        current.setNome(value.getNome());
        current.setPreco(value.getPreco());
        current.setEstoqueMinimo(value.getEstoqueMinimo());
        return repository.save(current);
    }
    public void excluir(Long id) {
        Produto produto = repository.buscarParaAtualizar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
        if (movimentacoes.existsByProdutoId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto com histórico de estoque não pode ser excluído. Isso preserva suas movimentações.");
        repository.delete(produto);
    }
}
