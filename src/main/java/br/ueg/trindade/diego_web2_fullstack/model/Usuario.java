package br.ueg.trindade.diego_web2_fullstack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Usuario {

    private Long id;
    private String nome;
    private String username;

    @JsonIgnore
    private String senha;

    private String email;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String username, String senha, String email) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.senha = senha;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}