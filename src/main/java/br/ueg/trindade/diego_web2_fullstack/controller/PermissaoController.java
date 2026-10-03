package br.ueg.trindade.diego_web2_fullstack.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import br.ueg.trindade.diego_web2_fullstack.model.Permissao;
import br.ueg.trindade.diego_web2_fullstack.service.PermissaoService;
@RestController
@RequestMapping("/api/permissoes")
@CrossOrigin(origins = "http://localhost:5173")
public class PermissaoController {
    private final PermissaoService service;
    public PermissaoController(PermissaoService service) { this.service = service; }
    @GetMapping public List<Permissao> listar() { return service.listarTodos(); }
    @GetMapping("/{id}") public Permissao buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Permissao criar(@RequestBody Permissao value) { return service.criar(value); }
    @PutMapping("/{id}") public Permissao atualizar(@PathVariable Long id, @RequestBody Permissao value) { return service.atualizar(id, value); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
