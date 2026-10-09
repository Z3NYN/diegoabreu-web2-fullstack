package br.ueg.trindade.braullyweb2fullstack.controller;

import java.util.List;
import br.ueg.trindade.braullyweb2fullstack.dto.UsuarioEntrada;
import br.ueg.trindade.braullyweb2fullstack.dto.UsuarioResposta;
import br.ueg.trindade.braullyweb2fullstack.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<UsuarioResposta> listar() {
        return service.listar().stream().map(UsuarioResposta::de).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResposta buscar(@PathVariable("id") Long id) { return UsuarioResposta.de(service.buscar(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta criar(@RequestBody UsuarioEntrada usuario) {
        return UsuarioResposta.de(service.criar(usuario));
    }

    @PutMapping("/{id}")
    public UsuarioResposta atualizar(@PathVariable("id") Long id, @RequestBody UsuarioEntrada usuario) {
        return UsuarioResposta.de(service.atualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable("id") Long id) { service.remover(id); }
}
