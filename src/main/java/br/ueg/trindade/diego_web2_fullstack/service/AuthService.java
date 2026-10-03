package br.ueg.trindade.diego_web2_fullstack.service;

import br.ueg.trindade.diego_web2_fullstack.model.Usuario;
import br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Locale;
import java.util.Base64;
import java.util.HexFormat;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class AuthService {
    private final UsuarioRepository repository;
    private final UsuarioService usuarios;
    private final EmailService email;
    private final PasswordEncoder encoder;
    private final String hashInexistente;
    public AuthService(UsuarioRepository repository, UsuarioService usuarios, EmailService email, PasswordEncoder encoder) {
        this.repository = repository; this.usuarios = usuarios; this.email = email; this.encoder = encoder;
        hashInexistente = encoder.encode("credencial-inexistente");
    }
    public Usuario registrar(String nome, String username, String endereco, String senha) {
        validarSenha(senha); email.exigirConfiguracao();
        return usuarios.criar(new Usuario(null, nome, username, encoder.encode(senha), endereco));
    }
    public Usuario autenticar(String identificador, String senha) {
        Usuario usuario = validarCredenciais(identificador, senha);
        if (!usuario.isEmailConfirmado()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Confirme seu e-mail antes de entrar. Você pode solicitar um novo link ou corrigir o endereço.");
        return usuario;
    }
    private Usuario validarCredenciais(String identificador, String senha) {
        String login = identificador == null ? "" : identificador.trim().toLowerCase(Locale.ROOT);
        if (login.length() > 254 || senha == null || senha.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail, username ou senha incorretos.");
        Usuario usuario = repository.findByEmailIgnoreCaseOrUsernameIgnoreCase(login, login).orElse(null);
        String hash = usuario == null || usuario.getSenha() == null || !usuario.getSenha().startsWith("$2") ? hashInexistente : usuario.getSenha();
        if (!encoder.matches(senha, hash) || usuario == null || hash.equals(hashInexistente))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail, username ou senha incorretos.");
        return usuario;
    }
    public void corrigirEmail(String identificador, String senha, String novoEmail) {
        Usuario usuario = validarCredenciais(identificador, senha);
        if (usuario.isEmailConfirmado()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conta já está confirmada. Entre para alterar seus dados.");
        email.exigirConfiguracao();
        String endereco = normalizarEmail(novoEmail);
        if (endereco.equals(usuario.getEmail())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O endereço já corresponde ao cadastro. Use Reenviar confirmação.");
        usuarios.atualizar(usuario.getId(), new Usuario(null, usuario.getNome(), usuario.getUsername(), null, endereco));
    }
    public void solicitarRecuperacao(String endereco) {
        email.exigirConfiguracao();
        Usuario encontrado = repository.findByEmailIgnoreCase(normalizarEmail(endereco)).orElse(null);
        if (encontrado == null) return;
        Usuario usuario = repository.findParaAtualizar(encontrado.getId()).orElseThrow();
        if (!usuario.isEmailConfirmado() || (usuario.getRecuperacaoEnviadaEm() != null && usuario.getRecuperacaoEnviadaEm().plusSeconds(60).isAfter(Instant.now()))) return;
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        usuario.setRecuperacaoHash(hash(token)); usuario.setRecuperacaoExpiraEm(Instant.now().plusSeconds(1800)); usuario.setRecuperacaoEnviadaEm(Instant.now());
        repository.saveAndFlush(usuario);
        email.enviarRecuperacao(usuario.getEmail(), token);
    }
    public void redefinirSenha(String token, String senha) {
        validarSenha(senha);
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalido();
        String hash = hash(token);
        Usuario usuario = repository.findByRecuperacaoHash(hash).orElseThrow(this::invalido);
        if (!hash.equals(usuario.getRecuperacaoHash()) || !usuario.isEmailConfirmado()) throw invalido();
        if (usuario.getRecuperacaoExpiraEm() == null || !usuario.getRecuperacaoExpiraEm().isAfter(Instant.now()))
            throw new ResponseStatusException(HttpStatus.GONE, "O link expirou. Solicite uma nova recuperação.");
        usuario.setSenha(encoder.encode(senha)); usuario.setVersaoCredencial(usuario.getVersaoCredencial() + 1);
        usuario.setRecuperacaoHash(null); usuario.setRecuperacaoExpiraEm(null);
        repository.saveAndFlush(usuario);
    }
    public void reenviarConfirmacao(String endereco) {
        email.exigirConfiguracao();
        Usuario usuario = repository.findByEmailIgnoreCase(normalizarEmail(endereco)).orElse(null);
        if (usuario == null || usuario.isEmailConfirmado()) return;
        if (usuario.getConfirmacaoEnviadaEm() != null && usuario.getConfirmacaoEnviadaEm().plusSeconds(60).isAfter(Instant.now())) return;
        usuarios.reenviar(usuario.getId());
    }
    public Usuario buscarSessao(String id) { return usuarios.buscarPorId(Long.valueOf(id)); }
    private String normalizarEmail(String value) {
        if (value == null || value.length() > 254 || !value.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido.");
        return value.trim().toLowerCase(Locale.ROOT);
    }
    private void validarSenha(String value) {
        if (value == null || value.length() < 12 || value.getBytes(StandardCharsets.UTF_8).length > 72 || value.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use uma senha com pelo menos 12 caracteres e até 72 bytes.");
    }
    private String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private ResponseStatusException invalido() { return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link inválido ou já utilizado. Solicite uma nova recuperação."); }
}
