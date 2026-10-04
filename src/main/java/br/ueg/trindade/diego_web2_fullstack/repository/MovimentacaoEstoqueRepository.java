package br.ueg.trindade.diego_web2_fullstack.repository;
import br.ueg.trindade.diego_web2_fullstack.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findTop100ByProdutoIdOrderByIdDesc(Long produtoId);
    boolean existsByProdutoId(Long produtoId);
    long countByProdutoId(Long produtoId);
    java.util.Optional<MovimentacaoEstoque> findByChave(String chave);
}
