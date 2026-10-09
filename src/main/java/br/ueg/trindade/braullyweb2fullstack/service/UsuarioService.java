package br.ueg.trindade.braullyweb2fullstack.service;

import java.util.List;
import br.ueg.trindade.braullyweb2fullstack.dto.UsuarioEntrada;
import br.ueg.trindade.braullyweb2fullstack.model.Usuario;
import br.ueg.trindade.braullyweb2fullstack.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) { this.repository = repository; }

    public List<Usuario> listar() { return repository.findAll(); }

    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public Usuario criar(UsuarioEntrada dados) {
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setUsername(dados.username());
        usuario.setSenha(dados.senha());
        usuario.setEmail(dados.email());
        return repository.save(usuario);
    }

    public Usuario atualizar(Long id, UsuarioEntrada dados) {
        Usuario usuario = buscar(id);
        usuario.setNome(dados.nome());
        usuario.setUsername(dados.username());
        usuario.setEmail(dados.email());
        // A atualização da Aula 05 preserva a senha definida no cadastro.
        return repository.save(usuario);
    }

    public void remover(Long id) { repository.delete(buscar(id)); }
}
