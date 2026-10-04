package br.ueg.trindade.diego_web2_fullstack.controller;
import br.ueg.trindade.diego_web2_fullstack.service.EstoqueService;
import br.ueg.trindade.diego_web2_fullstack.model.MovimentacaoEstoque;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
@RestController @RequestMapping("/api/estoque") @CrossOrigin(origins = "http://localhost:5173")
public class EstoqueController {
    private final EstoqueService service;
    public EstoqueController(EstoqueService service) { this.service = service; }
    @GetMapping("/resumo") public EstoqueService.Resumo resumo() { return service.resumo(); }
    @GetMapping("/produtos/{id}/movimentacoes") public EstoqueService.Historico historico(@PathVariable Long id) { return service.historico(id); }
    @PostMapping("/produtos/{id}/movimentacoes") @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoque movimentar(@PathVariable Long id, @RequestBody EstoqueService.Pedido pedido, Authentication authentication) {
        return service.movimentar(id, pedido, Long.valueOf(authentication.getName()));
    }
}
