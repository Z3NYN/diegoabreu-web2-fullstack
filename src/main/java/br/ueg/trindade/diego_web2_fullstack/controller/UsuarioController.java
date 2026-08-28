package br.ueg.trindade.diego_web2_fullstack.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ueg.trindade.diego_web2_fullstack.model.Usuario;

@RestController
public class UsuarioController {

    @GetMapping("/usuarios")
    public List<Usuario> getAllUsuarios() {

        List<Usuario> usuarios = new ArrayList<>();

        usuarios.add(new Usuario(
                1L,
                "Diego",
                "diego",
                "123456",
                "diego@email.com"
        ));

        usuarios.add(new Usuario(
                2L,
                "João",
                "joao",
                "senha123",
                "joao@email.com"
        ));

        return usuarios;
    }
}