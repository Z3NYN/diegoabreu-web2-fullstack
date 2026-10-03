package br.ueg.trindade.diego_web2_fullstack.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.ueg.trindade.diego_web2_fullstack.model.Permissao;
import br.ueg.trindade.diego_web2_fullstack.repository.PermissaoRepository;
@Service
@Transactional
public class PermissaoService {
    private final PermissaoRepository repository;
    public PermissaoService(PermissaoRepository repository) { this.repository = repository; }
    public List<Permissao> listarTodos() { return repository.findAll(); }
    public Permissao buscarPorId(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado")); }
    private void validar(Permissao value) {
        if (value.getNome() == null || value.getNome().isBlank() || value.getNome().length() > 120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um nome com até 120 caracteres.");
        if (value.getDescricao() != null && value.getDescricao().length() > 255) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descrição deve ter até 255 caracteres.");
        value.setNome(value.getNome().trim());
    }
    public Permissao criar(Permissao value) { validar(value); value.setId(null); return repository.save(value); }
    public Permissao atualizar(Long id, Permissao value) {
        Permissao current = buscarPorId(id);
        validar(value);
        current.setNome(value.getNome());
        current.setDescricao(value.getDescricao());
        return repository.save(current);
    }
    public void excluir(Long id) { repository.delete(buscarPorId(id)); }
}
