package br.ueg.trindade.diego_web2_fullstack.service;
import java.util.List;
import java.util.Locale;
import java.time.Instant;
import java.time.Duration;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.ueg.trindade.diego_web2_fullstack.model.Usuario;
import br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository;
@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepository repository;
    private final Validator validator;
    private final EmailService emailService;
    public UsuarioService(UsuarioRepository repository, Validator validator, EmailService emailService) {
        this.repository = repository; this.validator = validator; this.emailService = emailService;
    }
    public List<Usuario> listarTodos() { return repository.findAll(); }
    public Usuario buscarPorId(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado")); }
    private void validar(Usuario value, Long id) {
        if (value.getNome() == null || value.getNome().isBlank() || value.getNome().length() > 120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um nome com até 120 caracteres.");
        if (value.getUsername() == null || !value.getUsername().matches("[a-zA-Z0-9._-]{3,50}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username deve ter de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.");
        if (value.getEmail() != null) value.setEmail(value.getEmail().trim().toLowerCase(Locale.ROOT));
        if (!validator.validateProperty(value, "email").isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido, como nome@dominio.com.");
        value.setNome(value.getNome().trim()); value.setUsername(value.getUsername().trim().toLowerCase(Locale.ROOT));
        if (id == null ? repository.existsByEmailIgnoreCase(value.getEmail()) : repository.existsByEmailIgnoreCaseAndIdNot(value.getEmail(), id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        if (id == null ? repository.existsByUsernameIgnoreCase(value.getUsername()) : repository.existsByUsernameIgnoreCaseAndIdNot(value.getUsername(), id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este username já está cadastrado.");
    }
    public Usuario criar(Usuario value) {
        validar(value, null); value.setId(null); limparConfirmacao(value);
        value = repository.save(value);
        if (emailService.habilitado()) enviar(value);
        return value;
    }
    public Usuario atualizar(Long id, Usuario value) {
        Usuario current = repository.findParaAtualizar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado")); validar(value, id);
        boolean emailMudou = !value.getEmail().equals(current.getEmail());
        current.setNome(value.getNome()); current.setUsername(value.getUsername()); current.setEmail(value.getEmail());
        if (emailMudou) {
            current.setVersaoCredencial(current.getVersaoCredencial() + 1);
            limparConfirmacao(current);
            current.setRecuperacaoHash(null); current.setRecuperacaoExpiraEm(null); current.setRecuperacaoEnviadaEm(null);
            if (emailService.habilitado()) enviar(current);
        }
        return repository.save(current);
    }
    private void limparConfirmacao(Usuario usuario) {
        usuario.setEmailConfirmado(false); usuario.setConfirmacaoHash(null); usuario.setConfirmacaoExpiraEm(null); usuario.setConfirmacaoEnviadaEm(null);
    }
    public void excluir(Long id) { repository.delete(buscarPorId(id)); }
    public void reenviar(Long id) {
        Usuario usuario = repository.findParaAtualizar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
        if (usuario.isEmailConfirmado()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já foi confirmado.");
        if (usuario.getConfirmacaoEnviadaEm() != null && usuario.getConfirmacaoEnviadaEm().plusSeconds(60).isAfter(Instant.now())) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Aguarde um minuto antes de solicitar outro e-mail.");
        enviar(usuario);
    }
    private void enviar(Usuario usuario) {
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        usuario.setConfirmacaoHash(hash(token)); usuario.setConfirmacaoExpiraEm(Instant.now().plus(Duration.ofHours(24))); usuario.setConfirmacaoEnviadaEm(Instant.now());
        repository.saveAndFlush(usuario);
        emailService.enviarConfirmacao(usuario.getEmail(), token);
    }
    private String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    public void confirmar(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link de confirmação inválido.");
        Usuario usuario = repository.findByConfirmacaoHash(hash(token)).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link inválido ou já utilizado. Solicite um novo e-mail."));
        if (!hash(token).equals(usuario.getConfirmacaoHash())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link já utilizado.");
        if (usuario.getConfirmacaoExpiraEm() == null || !usuario.getConfirmacaoExpiraEm().isAfter(Instant.now())) throw new ResponseStatusException(HttpStatus.GONE, "Este link expirou. Solicite um novo e-mail de confirmação.");
        usuario.setEmailConfirmado(true); usuario.setConfirmacaoHash(null); usuario.setConfirmacaoExpiraEm(null); repository.save(usuario);
    }
}
