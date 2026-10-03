package br.ueg.trindade.diego_web2_fullstack.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import br.ueg.trindade.diego_web2_fullstack.model.Usuario;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    java.util.Optional<Usuario> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);
    java.util.Optional<Usuario> findByEmailIgnoreCase(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<Usuario> findByRecuperacaoHash(String hash);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<Usuario> findByConfirmacaoHash(String hash);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.id = :id")
    java.util.Optional<Usuario> findParaAtualizar(@org.springframework.data.repository.query.Param("id") Long id);
}
