package br.ueg.trindade.diego_web2_fullstack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String senha;

    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Email
    @jakarta.validation.constraints.Size(max = 254)
    @jakarta.validation.constraints.Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")
    @Column(unique = true)
    private String email;
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private boolean emailConfirmado;
    @JsonIgnore
    private String confirmacaoHash;
    @JsonIgnore
    private java.time.Instant confirmacaoExpiraEm;
    @JsonIgnore
    private java.time.Instant confirmacaoEnviadaEm;
    @JsonIgnore
    private String recuperacaoHash;
    @JsonIgnore
    private java.time.Instant recuperacaoExpiraEm;
    @JsonIgnore
    private java.time.Instant recuperacaoEnviadaEm;
    @JsonIgnore
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long versaoCredencial;

    @JsonIgnore
    public long getVersaoCredencial() { return versaoCredencial; }
    public void setVersaoCredencial(long value) { versaoCredencial = value; }

    @JsonIgnore
    public String getRecuperacaoHash() { return recuperacaoHash; }
    public void setRecuperacaoHash(String value) { recuperacaoHash = value; }
    @JsonIgnore
    public java.time.Instant getRecuperacaoExpiraEm() { return recuperacaoExpiraEm; }
    public void setRecuperacaoExpiraEm(java.time.Instant value) { recuperacaoExpiraEm = value; }
    @JsonIgnore
    public java.time.Instant getRecuperacaoEnviadaEm() { return recuperacaoEnviadaEm; }
    public void setRecuperacaoEnviadaEm(java.time.Instant value) { recuperacaoEnviadaEm = value; }

    public boolean isEmailConfirmado() { return emailConfirmado; }
    public void setEmailConfirmado(boolean value) { emailConfirmado = value; }
    @JsonIgnore
    public String getConfirmacaoHash() { return confirmacaoHash; }
    public void setConfirmacaoHash(String value) { confirmacaoHash = value; }
    @JsonIgnore
    public java.time.Instant getConfirmacaoExpiraEm() { return confirmacaoExpiraEm; }
    public void setConfirmacaoExpiraEm(java.time.Instant value) { confirmacaoExpiraEm = value; }
    @JsonIgnore
    public java.time.Instant getConfirmacaoEnviadaEm() { return confirmacaoEnviadaEm; }
    public void setConfirmacaoEnviadaEm(java.time.Instant value) { confirmacaoEnviadaEm = value; }
    public String getStatusEmail() { return emailConfirmado ? "CONFIRMADO" : confirmacaoEnviadaEm == null ? "PENDENTE_ENVIO" : "AGUARDANDO_CONFIRMACAO"; }


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
