package br.ueg.trindade.diego_web2_fullstack.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import br.ueg.trindade.diego_web2_fullstack.model.Usuario;
import br.ueg.trindade.diego_web2_fullstack.service.UsuarioService;
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }
    @PostMapping("/{id}/confirmacao-email") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reenviar(@PathVariable Long id) { service.reenviar(id); }
    public record ConfirmacaoRequest(String token) {}
    @PostMapping("/confirmar-email") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmar(@RequestBody ConfirmacaoRequest request) { service.confirmar(request.token()); }
    @GetMapping public List<Usuario> listar() { return service.listarTodos(); }
    @GetMapping("/{id}") public Usuario buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Usuario criar(@RequestBody Usuario value) { return service.criar(value); }
    @PutMapping("/{id}") public Usuario atualizar(@PathVariable Long id, @RequestBody Usuario value) { return service.atualizar(id, value); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
