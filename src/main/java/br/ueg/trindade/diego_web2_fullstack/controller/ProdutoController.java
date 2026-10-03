package br.ueg.trindade.diego_web2_fullstack.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import br.ueg.trindade.diego_web2_fullstack.model.Produto;
import br.ueg.trindade.diego_web2_fullstack.service.ProdutoService;
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "http://localhost:5173")
public class ProdutoController {
    private final ProdutoService service;
    public ProdutoController(ProdutoService service) { this.service = service; }
    @GetMapping public List<Produto> listar() { return service.listarTodos(); }
    @GetMapping("/{id}") public Produto buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Produto criar(@RequestBody Produto value) { return service.criar(value); }
    @PutMapping("/{id}") public Produto atualizar(@PathVariable Long id, @RequestBody Produto value) { return service.atualizar(id, value); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
