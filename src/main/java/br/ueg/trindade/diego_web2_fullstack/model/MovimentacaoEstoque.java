package br.ueg.trindade.diego_web2_fullstack.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(indexes = @Index(name = "idx_movimentacao_produto", columnList = "produtoId,id"))
public class MovimentacaoEstoque {
    public enum Tipo { ENTRADA, SAIDA }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false, length = 36) private String chave;
    @Column(nullable = false, updatable = false) private Long produtoId;
    @Column(nullable = false, updatable = false) private Long usuarioId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false) private Tipo tipo;
    @Column(nullable = false, updatable = false) private int quantidade;
    @Column(nullable = false, updatable = false) private int saldoAnterior;
    @Column(nullable = false, updatable = false) private int saldoAtual;
    @Column(nullable = false, updatable = false, length = 255) private String motivo;
    @Column(nullable = false, updatable = false) private Instant criadoEm;
    public MovimentacaoEstoque() {}
    public MovimentacaoEstoque(Long produtoId, Long usuarioId, Tipo tipo, int quantidade, int saldoAnterior, int saldoAtual, String motivo, String chave) {
        this.produtoId = produtoId; this.usuarioId = usuarioId; this.tipo = tipo;
        this.quantidade = quantidade; this.saldoAnterior = saldoAnterior; this.saldoAtual = saldoAtual;
        this.motivo = motivo; this.criadoEm = Instant.now();
        this.chave = chave;
    }
    public Long getId() { return id; }
    public Long getProdutoId() { return produtoId; }
    public Long getUsuarioId() { return usuarioId; }
    public Tipo getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public int getSaldoAnterior() { return saldoAnterior; }
    public int getSaldoAtual() { return saldoAtual; }
    public String getMotivo() { return motivo; }
    public Instant getCriadoEm() { return criadoEm; }
}
