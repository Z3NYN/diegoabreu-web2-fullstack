package br.ueg.trindade.diego_web2_fullstack;
import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:crudtest", "spring.jpa.hibernate.ddl-auto=create-drop"})
class CrudIntegrationTests {
    @org.springframework.beans.factory.annotation.Autowired
    br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository usuarios;
    @Value("${local.server.port}") int port;
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL)).build();
    @org.junit.jupiter.api.BeforeEach void autenticar() throws Exception {
        var conta = usuarios.findByEmailIgnoreCase("crud-session@example.com").orElse(null);
        if (conta == null) {
            conta = new br.ueg.trindade.diego_web2_fullstack.model.Usuario(null, "Sessão dos testes", "crud-session", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode("Senha-segura-123"), "crud-session@example.com");
            conta.setEmailConfirmado(true); usuarios.save(conta);
        }
        assertEquals(200, call("POST", "/api/auth/login", "{\"identificador\":\"crud-session\",\"senha\":\"Senha-segura-123\"}").statusCode());
    }
    @Test void limitesDeEntradaETiposInvalidos() throws Exception {
        assertEquals(400, call("POST", "/api/produtos", "{\"nome\":\"Preço impreciso\",\"preco\":1.999}").statusCode());
        assertEquals(400, call("POST", "/api/produtos", "{\"nome\":\"Preço excessivo\",\"preco\":1000000000}").statusCode());
        assertEquals(400, call("POST", "/api/produtos", "{\"nome\":\""+"x".repeat(121)+"\",\"preco\":1}").statusCode());
        assertEquals(400, call("POST", "/api/permissoes", "{\"nome\":\"Teste\",\"descricao\":\""+"x".repeat(256)+"\"}").statusCode());
        assertEquals(400, call("POST", "/api/produtos", "{invalido").statusCode());
        assertEquals(400, call("GET", "/api/produtos/abc", null).statusCode());
        assertEquals(404, call("GET", "/api/recurso-inexistente", null).statusCode());
    }
    @Test void confirmacaoNaoPodeSerForjadaNoCadastroOuPut() throws Exception {
        String body = "{\"nome\":\"Auditoria\",\"username\":\"AUDITORIA\",\"email\":\" AUDITORIA@example.com \",\"emailConfirmado\":true,\"confirmacaoHash\":\"forjado\"}";
        var created = call("POST", "/api/usuarios", body);
        assertEquals(201, created.statusCode(), created.body());
        String id = created.body().replaceAll("(?s).*\"id\":([0-9]+).*", "$1");
        try {
            assertTrue(created.body().contains("\"emailConfirmado\":false"));
            assertTrue(created.body().contains("\"username\":\"auditoria\""));
            assertFalse(created.body().contains("confirmacaoHash"));
            var updated = call("PUT", "/api/usuarios/"+id, body);
            assertEquals(200, updated.statusCode());
            assertTrue(updated.body().contains("\"emailConfirmado\":false"));
            assertEquals(503, call("POST", "/api/usuarios/"+id+"/confirmacao-email", "{}").statusCode());
            assertEquals(400, call("POST", "/api/usuarios/confirmar-email", "{\"token\":\"forjado\"}").statusCode());
            assertEquals(409, call("POST", "/api/usuarios", body).statusCode());
        } finally { call("DELETE", "/api/usuarios/"+id, null); }
    }
    @Test void senhaOcultaEPreservadaNaEdicao() throws Exception {
        var usuario = new br.ueg.trindade.diego_web2_fullstack.model.Usuario(null, "Teste", "teste", "segredo", "teste@example.com");
        usuario = usuarios.save(usuario);
        String path = "/api/usuarios/" + usuario.getId();
        var resposta = call("GET", path, null);
        assertFalse(resposta.body().contains("senha"));
        assertFalse(resposta.body().contains("segredo"));
        assertEquals(200, call("PUT", path, "{\"nome\":\"Editado\",\"username\":\"teste\",\"email\":\"teste@example.com\",\"senha\":null}").statusCode());
        assertEquals("segredo", usuarios.findById(usuario.getId()).orElseThrow().getSenha());
        assertEquals(204, call("DELETE", path, null).statusCode());
    }
    private HttpResponse<String> call(String method, String path, String body) throws Exception {
        var csrf = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/csrf")).GET().build(), HttpResponse.BodyHandlers.ofString());
        String token = csrf.body().replaceAll("(?s).*\"token\":\"([^\"]+)\".*", "$1");
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .header("X-CSRF-TOKEN", token)
            .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void crudDasTresEntidades() throws Exception {
        String[] resources = {"usuarios", "permissoes", "produtos"};
        String[] bodies = {"{\"nome\":\"Diego\",\"username\":\"diego\",\"email\":\"diego@example.com\",\"senha\":\"segredo\"}", "{\"nome\":\"Leitura\",\"descricao\":\"Consultar\"}", "{\"nome\":\"Teclado\",\"preco\":99.90}"};
        for (int i=0; i<resources.length; i++) {
            String path = "/api/" + resources[i];
            var created = call("POST", path, bodies[i]);
            assertEquals(201, created.statusCode(), created.body());
            assertFalse(created.body().contains("senha"));
            String id = created.body().replaceAll("(?s).*\"id\":([0-9]+).*", "$1");
            assertEquals(200, call("GET", path, null).statusCode());
            assertEquals(200, call("GET", path + "/" + id, null).statusCode());
            var updated = call("PUT", path + "/" + id, bodies[i].replace("Diego", "Diego editado").replace("Leitura", "Escrita").replace("Teclado", "Mouse"));
            assertEquals(200, updated.statusCode());
            assertFalse(updated.body().contains("senha"));
            assertEquals(204, call("DELETE", path + "/" + id, null).statusCode());
            assertEquals(404, call("GET", path + "/" + id, null).statusCode());
            assertEquals(404, call("PUT", path + "/" + id, bodies[i]).statusCode());
            assertEquals(404, call("DELETE", path + "/" + id, null).statusCode());
        }
    }
    @Test void regrasDeProdutoECors() throws Exception {
        assertEquals(400, call("POST", "/api/produtos", "{\"nome\":\"Inválido\",\"preco\":-1}").statusCode());
        assertEquals(400, call("POST", "/api/produtos", "{\"nome\":\" \",\"preco\":1}").statusCode());
        var created = call("POST", "/api/produtos", "{\"nome\":\"Válido\",\"preco\":0}");
        String id = created.body().replaceAll("(?s).*\"id\":([0-9]+).*", "$1");
        assertEquals(400, call("PUT", "/api/produtos/"+id, "{\"nome\":\"Válido\",\"preco\":-1}").statusCode());
        call("DELETE", "/api/produtos/"+id, null);
        var cors = client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/produtos"))
            .header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, cors.statusCode());
        assertEquals("http://localhost:5173", cors.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertEquals(403, call("GET", "/produtos", null).statusCode());
    }
    @Test void estoqueHttpAutenticadoComCsrfESaldoNaoForjavel() throws Exception {
        var anonimo = HttpClient.newHttpClient();
        assertEquals(401, anonimo.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/estoque/resumo")).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        var criado = call("POST", "/api/produtos", "{\"nome\":\"Estoque HTTP\",\"preco\":12.50,\"estoqueMinimo\":2,\"quantidade\":999}");
        assertEquals(201, criado.statusCode()); assertTrue(criado.body().contains("\"quantidade\":0"));
        String id = criado.body().replaceAll("(?s).*\"id\":([0-9]+).*", "$1");
        String caminho = "/api/estoque/produtos/"+id+"/movimentacoes";
        String movimento = "{\"tipo\":\"ENTRADA\",\"quantidade\":5,\"motivo\":\"Reposição HTTP\",\"chave\":\""+java.util.UUID.randomUUID()+"\"}";
        assertEquals(403, client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+caminho)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(movimento)).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(201, call("POST", caminho, movimento).statusCode());
        assertEquals(201, call("POST", caminho, movimento).statusCode());
        assertTrue(call("GET", caminho, null).body().contains("\"total\":1"));
        assertTrue(call("GET", "/api/produtos/"+id, null).body().contains("\"quantidade\":5"));
        assertEquals(400, call("POST", caminho, movimento.replace("\"quantidade\":5", "\"quantidade\":1.5")).statusCode());
        assertEquals(409, call("POST", caminho, movimento.replace("ENTRADA", "SAIDA").replace("\"quantidade\":5", "\"quantidade\":6").replaceAll("[a-f0-9]{8}-[a-f0-9-]{27,}", java.util.UUID.randomUUID().toString())).statusCode());
        assertEquals(409, call("DELETE", "/api/produtos/"+id, null).statusCode());
        assertEquals(200, call("GET", "/api/estoque/resumo", null).statusCode());
    }
}
