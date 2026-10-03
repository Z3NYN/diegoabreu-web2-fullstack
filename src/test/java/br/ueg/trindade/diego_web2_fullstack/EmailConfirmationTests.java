package br.ueg.trindade.diego_web2_fullstack;

import br.ueg.trindade.diego_web2_fullstack.model.Usuario;
import br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository;
import br.ueg.trindade.diego_web2_fullstack.service.EmailService;
import br.ueg.trindade.diego_web2_fullstack.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:emailtest", "spring.jpa.hibernate.ddl-auto=create-drop"})
class EmailConfirmationTests {
    @Autowired UsuarioService service;
    @Autowired UsuarioRepository repository;
    @MockitoBean EmailService email;
    @BeforeEach void preparar() { repository.deleteAll(); when(email.habilitado()).thenReturn(true); }
    private Usuario dados(String endereco) { return new Usuario(null, "Teste", "teste", null, endereco); }
    private String tokenEnviado() {
        var token = ArgumentCaptor.forClass(String.class);
        verify(email, atLeastOnce()).enviarConfirmacao(anyString(), token.capture());
        return token.getValue();
    }
    @Test void apenasUmaConfirmacaoSimultaneaPodeConsumirOToken() throws Exception {
        service.criar(dados("concorrente@example.com"));
        String token = tokenEnviado();
        var inicio = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Boolean> confirmar = () -> {
            inicio.await();
            try { service.confirmar(token); return true; }
            catch (ResponseStatusException ex) { assertEquals(400, ex.getStatusCode().value()); return false; }
        };
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var primeiro = executor.submit(confirmar); var segundo = executor.submit(confirmar); inicio.countDown();
            assertNotEquals(primeiro.get(10, java.util.concurrent.TimeUnit.SECONDS), segundo.get(10, java.util.concurrent.TimeUnit.SECONDS));
        }
    }
    @Test void envioConfirmacaoEUsoUnico() {
        Usuario usuario = service.criar(dados("Teste@Example.com"));
        assertEquals("teste@example.com", usuario.getEmail());
        assertEquals("AGUARDANDO_CONFIRMACAO", usuario.getStatusEmail());
        String token = tokenEnviado();
        assertEquals(43, token.length());
        assertNotEquals(token, repository.findById(usuario.getId()).orElseThrow().getConfirmacaoHash());
        service.confirmar(token);
        assertTrue(service.buscarPorId(usuario.getId()).isEmailConfirmado());
        assertThrows(ResponseStatusException.class, () -> service.confirmar(token));
    }
    @Test void linkExpiradoEReenvioComLimite() {
        Usuario usuario = service.criar(dados("teste@example.com"));
        String token = tokenEnviado();
        Long id = usuario.getId();
        assertEquals(429, assertThrows(ResponseStatusException.class, () -> service.reenviar(id)).getStatusCode().value());
        usuario = repository.findById(usuario.getId()).orElseThrow();
        usuario.setConfirmacaoExpiraEm(Instant.now().minusSeconds(1));
        usuario.setConfirmacaoEnviadaEm(Instant.now().minusSeconds(61));
        repository.save(usuario);
        assertEquals(410, assertThrows(ResponseStatusException.class, () -> service.confirmar(token)).getStatusCode().value());
        service.reenviar(usuario.getId());
        String novoToken = tokenEnviado();
        assertNotEquals(token, novoToken);
        assertThrows(ResponseStatusException.class, () -> service.confirmar(token));
        service.confirmar(novoToken);
    }
    @Test void alterarEmailExigeNovaConfirmacao() {
        Usuario usuario = service.criar(dados("teste@example.com"));
        String token = tokenEnviado(); service.confirmar(token);
        usuario = service.atualizar(usuario.getId(), dados("novo@example.com"));
        assertFalse(usuario.isEmailConfirmado());
        assertEquals("AGUARDANDO_CONFIRMACAO", usuario.getStatusEmail());
        assertThrows(ResponseStatusException.class, () -> service.confirmar(token));
    }
    @Test void emailInvalidoDuplicadoEFalhaDeEnvio() {
        for (String invalido : new String[]{"a@", "a@@example.com", "nome@example", "nome com espaco@example.com", ""})
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.criar(dados(invalido))).getStatusCode().value());
        service.criar(dados("teste@example.com"));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.criar(dados("TESTE@example.com"))).getStatusCode().value());
        repository.deleteAll();
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Falha SMTP")).when(email).enviarConfirmacao(anyString(), anyString());
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> service.criar(dados("teste@example.com"))).getStatusCode().value());
        assertEquals(0, repository.count());
    }
}
