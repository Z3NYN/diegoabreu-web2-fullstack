package br.ueg.trindade.braullyweb2fullstack.dto;

import br.ueg.trindade.braullyweb2fullstack.model.Usuario;

public record UsuarioResposta(Long id, String nome, String username, String email) {
    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getEmail());
    }
}
