package br.ueg.trindade.diego_web2_fullstack.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ueg.trindade.diego_web2_fullstack.model.Permissao;

@RestController
public class PermissaoController {

    @GetMapping("/permissoes")
    public List<Permissao> getAllPermissoes() {

        List<Permissao> permissoes = new ArrayList<>();

        permissoes.add(new Permissao(
                1L,
                "Administrador",
                "Permissão para administrar o sistema"
        ));

        permissoes.add(new Permissao(
                2L,
                "Usuário",
                "Permissão para acessar funcionalidades básicas"
        ));

        return permissoes;
    }
}