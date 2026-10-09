package br.ueg.trindade.braullyweb2fullstack.controller;

import java.util.List;
import br.ueg.trindade.braullyweb2fullstack.model.Permissao;
import br.ueg.trindade.braullyweb2fullstack.service.PermissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissoes")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class PermissaoController {
    private final PermissaoService service;

    public PermissaoController(PermissaoService service) { this.service = service; }

    @GetMapping
    public List<Permissao> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Permissao buscar(@PathVariable("id") Long id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Permissao criar(@RequestBody Permissao permissao) { return service.criar(permissao); }

    @PutMapping("/{id}")
    public Permissao atualizar(@PathVariable("id") Long id, @RequestBody Permissao permissao) {
        return service.atualizar(id, permissao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable("id") Long id) { service.remover(id); }
}
