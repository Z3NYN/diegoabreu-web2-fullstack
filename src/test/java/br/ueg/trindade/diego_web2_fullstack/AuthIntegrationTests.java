package br.ueg.trindade.diego_web2_fullstack;

import br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository;
import br.ueg.trindade.diego_web2_fullstack.service.EmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.mockito.ArgumentCaptor;
import java.net.*;
import java.net.http.*;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:authtest", "spring.jpa.hibernate.ddl-auto=create-drop"})
class AuthIntegrationTests {
    @Autowired UsuarioRepository usuarios;
    @MockitoBean EmailService email;
    @Value("${local.server.port}") int port;
    private HttpClient client;
    private static final String SENHA = "Uma-frase-segura-123";
    @BeforeEach void preparar() {
        usuarios.deleteAll(); when(email.habilitado()).thenReturn(true);
        client = novoCliente();
    }
    private HttpClient novoCliente() { return HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build(); }
    private HttpResponse<String> call(HttpClient cliente, String method, String path, String body, boolean csrf) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).header("Content-Type", "application/json");
        if (csrf) {
            var token = cliente.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/auth/csrf")).GET().build(), HttpResponse.BodyHandlers.ofString());
            builder.header("X-CSRF-TOKEN", token.body().replaceAll("(?s).*\"token\":\"([^\"]+)\".*", "$1"));
        }
        return cliente.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(String path, String body) throws Exception { return call(client, "POST", path, body, true); }
    private HttpResponse<String> login(String senha) throws Exception { return post("/api/auth/login", "{\"identificador\":\"conta@example.com\",\"senha\":\""+senha+"\"}"); }
    private String tokenConfirmacao() {
        var captor = ArgumentCaptor.forClass(String.class); verify(email, atLeastOnce()).enviarConfirmacao(eq("conta@example.com"), captor.capture()); return captor.getValue();
    }
    private String tokenRecuperacao() {
        var captor = ArgumentCaptor.forClass(String.class); verify(email, atLeastOnce()).enviarRecuperacao(eq("conta@example.com"), captor.capture()); return captor.getValue();
    }
    private void registrarEConfirmar() throws Exception {
        var cadastro = post("/api/auth/registrar", "{\"nome\":\"Conta\",\"username\":\"conta\",\"email\":\"conta@example.com\",\"senha\":\""+SENHA+"\"}");
        assertEquals(201, cadastro.statusCode(), cadastro.body());
        assertEquals(204, post("/api/usuarios/confirmar-email", "{\"token\":\""+tokenConfirmacao()+"\"}").statusCode());
    }
    @Test void apiProtegidaCsrfObrigatorioELoginSomenteAposConfirmacao() throws Exception {
        for (String recurso : new String[]{"usuarios", "permissoes", "produtos"}) {
            assertEquals(401, call(client,"GET","/api/"+recurso,null,false).statusCode());
            assertEquals(401, call(client,"POST","/api/"+recurso,"{}",true).statusCode());
            assertEquals(401, call(client,"PUT","/api/"+recurso+"/1","{}",true).statusCode());
            assertEquals(401, call(client,"DELETE","/api/"+recurso+"/1",null,true).statusCode());
        }
        assertEquals(403, call(client,"POST","/api/auth/login","{}",false).statusCode());
        assertEquals(201, post("/api/auth/registrar","{\"nome\":\"Conta\",\"username\":\"conta\",\"email\":\"conta@example.com\",\"senha\":\""+SENHA+"\"}").statusCode());
        var usuario = usuarios.findByEmailIgnoreCase("conta@example.com").orElseThrow();
        assertNotEquals(SENHA, usuario.getSenha()); assertTrue(usuario.getSenha().startsWith("$2"));
        assertEquals(401, login("senha-errada").statusCode()); assertEquals(403, login(SENHA).statusCode());
        assertEquals(204, post("/api/usuarios/confirmar-email","{\"token\":\""+tokenConfirmacao()+"\"}").statusCode());
        var sessao = login(SENHA); assertEquals(200,sessao.statusCode(),sessao.body());
        for (String secreto : new String[]{"senha","Hash","ExpiraEm","versaoCredencial"}) assertFalse(sessao.body().contains(secreto));
        assertTrue(sessao.headers().allValues("Set-Cookie").stream().anyMatch(c -> c.contains("HttpOnly") && c.contains("SameSite=Lax")));
        assertEquals(200, call(client,"GET","/api/produtos",null,false).statusCode());
        assertEquals(403, call(client,"POST","/api/produtos","{}",false).statusCode());
        assertEquals(204, post("/api/auth/logout","{}").statusCode());
        assertEquals(401, call(client,"GET","/api/auth/me",null,false).statusCode());
    }
    @Test void recuperacaoRealPorServicoTokenUnicoSenhaAntigaESessoesInvalidas() throws Exception {
        registrarEConfirmar(); assertEquals(200, login(SENHA).statusCode());
        var outra = novoCliente(); assertEquals(200, call(outra,"POST","/api/auth/login","{\"identificador\":\"conta\",\"senha\":\""+SENHA+"\"}",true).statusCode());
        var existente = post("/api/auth/recuperacao","{\"email\":\"conta@example.com\"}");
        var desconhecido = post("/api/auth/recuperacao","{\"email\":\"desconhecido@example.com\"}");
        assertEquals(202, existente.statusCode()); assertEquals(existente.body(),desconhecido.body());
        String token = tokenRecuperacao(); assertEquals(43,token.length());
        assertNotEquals(token,usuarios.findByEmailIgnoreCase("conta@example.com").orElseThrow().getRecuperacaoHash());
        assertEquals(202, post("/api/auth/recuperacao","{\"email\":\"conta@example.com\"}").statusCode());
        verify(email,times(1)).enviarRecuperacao(anyString(),anyString());
        var anonimo = novoCliente(); String corpo = "{\"token\":\""+token+"\",\"senha\":\"Outra-frase-segura-456\"}";
        assertEquals(204,call(anonimo,"POST","/api/auth/redefinir-senha",corpo,true).statusCode());
        assertEquals(400,call(anonimo,"POST","/api/auth/redefinir-senha",corpo,true).statusCode());
        assertEquals(401,call(client,"GET","/api/produtos",null,false).statusCode());
        assertEquals(401,call(outra,"GET","/api/produtos",null,false).statusCode());
        client = novoCliente(); assertEquals(401,login(SENHA).statusCode()); assertEquals(200,login("Outra-frase-segura-456").statusCode());
    }
    @Test void tokenExpiradoInvalidoEEmailNaoConfirmado() throws Exception {
        registrarEConfirmar(); post("/api/auth/recuperacao","{\"email\":\"conta@example.com\"}");
        String token = tokenRecuperacao(); var usuario = usuarios.findByEmailIgnoreCase("conta@example.com").orElseThrow();
        usuario.setRecuperacaoExpiraEm(Instant.now().minusSeconds(1)); usuarios.save(usuario);
        assertEquals(410,post("/api/auth/redefinir-senha","{\"token\":\""+token+"\",\"senha\":\""+SENHA+"\"}").statusCode());
        assertEquals(400,post("/api/auth/redefinir-senha","{\"token\":\"invalido\",\"senha\":\""+SENHA+"\"}").statusCode());
        usuario.setEmailConfirmado(false); usuarios.save(usuario); reset(email); when(email.habilitado()).thenReturn(true);
        assertEquals(202,post("/api/auth/recuperacao","{\"email\":\"conta@example.com\"}").statusCode());
        verify(email,never()).enviarRecuperacao(anyString(),anyString());
    }
    @Test void falhaSmtpNaoCriaContaNemTokenEFaltaConfiguracaoNaoSimulaEnvio() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"SMTP indisponível")).when(email).enviarConfirmacao(anyString(),anyString());
        assertEquals(503,post("/api/auth/registrar","{\"nome\":\"Conta\",\"username\":\"conta\",\"email\":\"conta@example.com\",\"senha\":\""+SENHA+"\"}").statusCode());
        assertEquals(0,usuarios.count()); reset(email); when(email.habilitado()).thenReturn(true);
        registrarEConfirmar();
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"SMTP indisponível")).when(email).enviarRecuperacao(anyString(),anyString());
        assertEquals(503,post("/api/auth/recuperacao","{\"email\":\"conta@example.com\"}").statusCode());
        assertNull(usuarios.findByEmailIgnoreCase("conta@example.com").orElseThrow().getRecuperacaoHash());
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Não configurado")).when(email).exigirConfiguracao();
        assertEquals(503,post("/api/auth/recuperacao","{\"email\":\"inexistente@example.com\"}").statusCode());
    }
    @Test void senhaFracaETrocaDeEmailInvalidamSessao() throws Exception {
        assertEquals(400,post("/api/auth/registrar","{\"nome\":\"Conta\",\"username\":\"conta\",\"email\":\"conta@example.com\",\"senha\":\"curta\"}").statusCode());
        registrarEConfirmar(); assertEquals(200,login(SENHA).statusCode());
        var usuario = usuarios.findByEmailIgnoreCase("conta@example.com").orElseThrow();
        assertEquals(200,call(client,"PUT","/api/usuarios/"+usuario.getId(),"{\"nome\":\"Conta\",\"username\":\"conta\",\"email\":\"novo@example.com\"}",true).statusCode());
        assertEquals(401,call(client,"GET","/api/produtos",null,false).statusCode());
    }
}
