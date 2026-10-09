package br.ueg.trindade.braullyweb2fullstack.service;

import java.util.List;
import br.ueg.trindade.braullyweb2fullstack.model.Permissao;
import br.ueg.trindade.braullyweb2fullstack.repository.PermissaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermissaoService {
    private final PermissaoRepository repository;

    public PermissaoService(PermissaoRepository repository) { this.repository = repository; }

    public List<Permissao> listar() { return repository.findAll(); }

    public Permissao buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Permissão não encontrada"));
    }

    public Permissao criar(Permissao permissao) {
        permissao.setId(null);
        return repository.save(permissao);
    }

    public Permissao atualizar(Long id, Permissao dados) {
        Permissao permissao = buscar(id);
        permissao.setNome(dados.getNome());
        permissao.setDescricao(dados.getDescricao());
        return repository.save(permissao);
    }

    public void remover(Long id) { repository.delete(buscar(id)); }
}
