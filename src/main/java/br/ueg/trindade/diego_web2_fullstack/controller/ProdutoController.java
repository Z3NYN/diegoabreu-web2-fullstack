package br.ueg.trindade.diego_web2_fullstack.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ueg.trindade.diego_web2_fullstack.model.Produto;

@RestController
public class ProdutoController {

    @GetMapping("/produtos")
    public List<Produto> getAllProdutos() {

        List<Produto> produtos = new ArrayList<>();

        produtos.add(new Produto(
                1L,
                "Notebook",
                3500.00
        ));

        produtos.add(new Produto(
                2L,
                "Mouse Gamer",
                150.00
        ));

        produtos.add(new Produto(
                3L,
                "Teclado Mecânico",
                280.00
        ));

        return produtos;
    }
}