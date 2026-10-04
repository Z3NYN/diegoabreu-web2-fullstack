package br.ueg.trindade.diego_web2_fullstack.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import br.ueg.trindade.diego_web2_fullstack.model.Produto;
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Produto p where p.id = :id")
    java.util.Optional<Produto> buscarParaAtualizar(@org.springframework.data.repository.query.Param("id") Long id);
}
