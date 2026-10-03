package br.ueg.trindade.diego_web2_fullstack.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import br.ueg.trindade.diego_web2_fullstack.model.Produto;
public interface ProdutoRepository extends JpaRepository<Produto, Long> {}
