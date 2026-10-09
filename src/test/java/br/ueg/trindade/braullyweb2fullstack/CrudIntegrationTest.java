package br.ueg.trindade.braullyweb2fullstack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import br.ueg.trindade.braullyweb2fullstack.repository.PermissaoRepository;
import br.ueg.trindade.braullyweb2fullstack.repository.ProdutoRepository;
import br.ueg.trindade.braullyweb2fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:n1-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class CrudIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PermissaoRepository permissoes;
    @Autowired private ProdutoRepository produtos;

    @BeforeEach
    void limparBanco() {
        usuarios.deleteAll();
        permissoes.deleteAll();
        produtos.deleteAll();
    }

    @Test
    void usuarioCrudOcultaEPreservaSenha() throws Exception {
        String resposta = mvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id":999,"nome":"Ana","username":"ana","senha":"senha-inicial","email":"ana@example.com"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = idDaResposta(resposta);
        assertThat(id).isNotEqualTo(999);
        assertThat(usuarios.findById(id).orElseThrow().getSenha()).isEqualTo("senha-inicial");

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
        mvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mvc.perform(put("/api/usuarios/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","username":"ana.silva","senha":"outra-senha","email":"ana.silva@example.com"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.username").value("ana.silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@example.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        assertThat(usuarios.findById(id).orElseThrow().getSenha()).isEqualTo("senha-inicial");

        mvc.perform(put("/api/usuarios/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","username":"ana.silva","email":"ana.silva@example.com"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());
        assertThat(usuarios.findById(id).orElseThrow().getSenha()).isEqualTo("senha-inicial");
        mvc.perform(delete("/api/usuarios/{id}", id)).andExpect(status().isNoContent());
        assertThat(usuarios.existsById(id)).isFalse();
        mvc.perform(get("/api/usuarios/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void permissaoCrud() throws Exception {
        String resposta = mvc.perform(post("/api/permissoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"CONSULTAR","descricao":"Consultar registros"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = idDaResposta(resposta);
        mvc.perform(get("/api/permissoes"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("CONSULTAR"));
        mvc.perform(get("/api/permissoes/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.descricao").value("Consultar registros"));
        mvc.perform(put("/api/permissoes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"EDITAR","descricao":"Editar registros"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("EDITAR"))
                .andExpect(jsonPath("$.descricao").value("Editar registros"));
        assertThat(permissoes.findById(id).orElseThrow().getNome()).isEqualTo("EDITAR");
        mvc.perform(delete("/api/permissoes/{id}", id)).andExpect(status().isNoContent());
        assertThat(permissoes.existsById(id)).isFalse();
        mvc.perform(get("/api/permissoes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void produtoCrud() throws Exception {
        String resposta = mvc.perform(post("/api/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Caderno","descricao":"Caderno de estudo","preco":12.50}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = idDaResposta(resposta);
        mvc.perform(get("/api/produtos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Caderno"));
        mvc.perform(get("/api/produtos/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(12.50));
        mvc.perform(put("/api/produtos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Caderno novo","descricao":"Descrição atualizada","preco":0}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Caderno novo"))
                .andExpect(jsonPath("$.descricao").value("Descrição atualizada"))
                .andExpect(jsonPath("$.preco").value(0));
        assertThat(produtos.findById(id).orElseThrow().getPreco()).isZero();
        mvc.perform(delete("/api/produtos/{id}", id)).andExpect(status().isNoContent());
        assertThat(produtos.existsById(id)).isFalse();
        mvc.perform(get("/api/produtos/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void regraDeNegocioProdutoRejeitaNomeVazioEPrecoNegativo() throws Exception {
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Produto\",\"preco\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"  \",\"preco\":1}"))
                .andExpect(status().isBadRequest());
        assertThat(produtos.count()).isZero();
        String resposta = mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Produto\",\"preco\":5}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = idDaResposta(resposta);
        mvc.perform(put("/api/produtos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Produto\",\"preco\":-10}"))
                .andExpect(status().isBadRequest());
        assertThat(produtos.findById(id).orElseThrow().getPreco()).isEqualByComparingTo("5");
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarios", "permissoes", "produtos"})
    void recursoInexistenteRetorna404(String recurso) throws Exception {
        String url = "/api/" + recurso + "/999999";
        mvc.perform(get(url)).andExpect(status().isNotFound());
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete(url)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:5173", "http://127.0.0.1:5173"})
    void corsPermiteFrontendSemAutenticacao(String origem) throws Exception {
        mvc.perform(options("/api/produtos/1")
                .header("Origin", origem)
                .header("Access-Control-Request-Method", "PUT")
                .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origem));
        mvc.perform(get("/api/usuarios").header("Origin", origem))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origem));
    }

    @Test
    void repositoriesNaoExportamCrudAlternativo() throws Exception {
        mvc.perform(get("/usuarios")).andExpect(status().isNotFound());
        mvc.perform(get("/permissoes")).andExpect(status().isNotFound());
        mvc.perform(get("/produtos")).andExpect(status().isNotFound());
    }

    private long idDaResposta(String resposta) {
        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }
}
