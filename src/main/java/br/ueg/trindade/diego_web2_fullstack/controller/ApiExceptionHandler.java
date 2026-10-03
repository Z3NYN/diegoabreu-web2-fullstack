package br.ueg.trindade.diego_web2_fullstack.controller;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String,String>> parametro(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "Identificador inválido. Informe um número inteiro."));
    }
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String,String>> caminho(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", "Recurso não encontrado."));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> regra(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason() == null ? "Operação inválida" : ex.getReason()));
    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> duplicado(org.springframework.dao.DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(Map.of("message", "Os dados informados já estão cadastrados ou violam uma restrição."));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> json(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "Dados inválidos. Confira os campos enviados."));
    }
}
