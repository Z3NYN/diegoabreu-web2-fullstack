package br.ueg.trindade.diego_web2_fullstack.controller;

import br.ueg.trindade.diego_web2_fullstack.service.AuthService;
import br.ueg.trindade.diego_web2_fullstack.model.Usuario;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import jakarta.servlet.http.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {
    private final AuthService service;
    private final SecurityContextRepository contextRepository;
    private final Map<String, long[]> tentativas = new HashMap<>();
    public AuthController(AuthService service, SecurityContextRepository contextRepository) { this.service = service; this.contextRepository = contextRepository; }
    public record Login(String identificador, String senha) {}
    public record Cadastro(String nome, String username, String email, String senha) {}
    public record Endereco(String email) {}
    public record Recuperacao(String token, String senha) {}
    public record Correcao(String identificador, String senha, String email) {}
    @GetMapping("/csrf") public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }
    @GetMapping("/me") public Usuario me(Authentication auth) { return service.buscarSessao(auth.getName()); }
    @PostMapping("/registrar") @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> registrar(@RequestBody Cadastro cadastro, HttpServletRequest request) {
        limitar(request, "email", 20);
        service.registrar(cadastro.nome(), cadastro.username(), cadastro.email(), cadastro.senha());
        return Map.of("message", "Cadastro realizado. Confira seu e-mail para confirmar a conta antes de entrar.");
    }
    @PostMapping("/login") public Usuario login(@RequestBody Login login, HttpServletRequest request, HttpServletResponse response) {
        limitar(request, "login", 10);
        Usuario usuario = service.autenticar(login.identificador(), login.senha());
        if (request.getSession(false) != null) request.changeSessionId();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(usuario.getId().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute("NEXUS_AUTH_HASH", usuario.getSenha());
        request.getSession().setAttribute("NEXUS_AUTH_VERSION", usuario.getVersaoCredencial());
        contextRepository.saveContext(context, request, response);
        new HttpSessionCsrfTokenRepository().saveToken(null, request, response);
        return usuario;
    }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        response.addHeader("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
    }
    @PostMapping("/recuperacao") @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> recuperar(@RequestBody Endereco endereco, HttpServletRequest request) {
        limitar(request, "email", 20); service.solicitarRecuperacao(endereco.email());
        return Map.of("message", "Se existir uma conta com esse e-mail confirmado, enviaremos um link de recuperação. Confira também o spam.");
    }
    @PostMapping("/reenviar-confirmacao") @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> reenviar(@RequestBody Endereco endereco, HttpServletRequest request) {
        limitar(request, "email", 20); service.reenviarConfirmacao(endereco.email());
        return Map.of("message", "Se houver uma conta pendente com esse e-mail, enviaremos um link. Aguarde um minuto entre solicitações.");
    }
    @PostMapping("/redefinir-senha") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinir(@RequestBody Recuperacao recuperacao, HttpServletRequest request) {
        limitar(request, "email", 20); service.redefinirSenha(recuperacao.token(), recuperacao.senha());
    }
    @PostMapping("/corrigir-email") public Map<String, String> corrigir(@RequestBody Correcao correcao, HttpServletRequest request) {
        limitar(request, "login", 10); service.corrigirEmail(correcao.identificador(), correcao.senha(), correcao.email());
        return Map.of("message", "E-mail corrigido. Enviamos um novo link para o endereço informado; o link anterior foi invalidado.");
    }
    private synchronized void limitar(HttpServletRequest request, String fluxo, int limite) {
        long agora = System.currentTimeMillis();
        tentativas.entrySet().removeIf(entry -> agora - entry.getValue()[0] >= 60000);
        if (tentativas.size() >= 10000) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas solicitações. Aguarde um minuto.");
        long[] estado = tentativas.computeIfAbsent(fluxo + ":" + request.getRemoteAddr(), key -> new long[]{agora, 0});
        if (++estado[1] > limite) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Aguarde um minuto antes de tentar novamente.");
    }
}
