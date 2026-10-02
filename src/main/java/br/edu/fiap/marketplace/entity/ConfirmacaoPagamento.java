package br.edu.fiap.marketplace.entity;

import br.edu.fiap.marketplace.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Confirma o resultado de pagamento de um carrinho pertencente a um usuário. */
@Entity
@Table(name = "confirmacoes_pagamento")
public class ConfirmacaoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrinho_id", nullable = false, unique = true)
    private Carrinho carrinho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "id_pagamento", nullable = false, unique = true, length = 80)
    private String idPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Column(name = "valor_pago", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "confirmado_em")
    private Instant confirmadoEm;

    protected ConfirmacaoPagamento() {
    }

    public ConfirmacaoPagamento(
            Carrinho carrinho,
            Usuario usuario,
            String idPagamento,
            BigDecimal valorPago) {
        this.carrinho = carrinho;
        this.usuario = usuario;
        this.idPagamento = idPagamento;
        this.valorPago = valorPago;
        this.status = StatusPagamento.PENDENTE;
    }

    public void aprovar() {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException("Pagamento já foi processado e só pode mudar de estado uma única vez.");
        }
        this.status = StatusPagamento.PAGO;
        this.confirmadoEm = Instant.now();
        this.carrinho.finalizar();
    }

    public void recusar() {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException("Pagamento já foi processado e só pode mudar de estado uma única vez.");
        }
        this.status = StatusPagamento.RECUSADO;
        this.confirmadoEm = Instant.now();
    }

    public boolean estaPago() {
        return this.status == StatusPagamento.PAGO;
    }

    public Long getId() { return id; }
    public Carrinho getCarrinho() { return carrinho; }
    public Usuario getUsuario() { return usuario; }
    public String getIdPagamento() { return idPagamento; }
    public StatusPagamento getStatus() { return status; }
    public BigDecimal getValorPago() { return valorPago; }
    public Instant getConfirmadoEm() { return confirmadoEm; }
}
