package com.clinica.model;

import java.math.BigDecimal;

/** Vacina ou medicamento da farmácia, com o estoque atual. Tabela: produto. */
public class ProdutoFarmacia {

    private int id;
    private String nomeProduto;
    private BigDecimal preco;
    private int quantidade;

    public ProdutoFarmacia() {
    }

    public ProdutoFarmacia(int id, String nomeProduto, BigDecimal preco, int quantidade) {
        this.id = id;
        this.nomeProduto = nomeProduto;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal calcularTotal(int qtdUsada) {
        return preco.multiply(BigDecimal.valueOf(qtdUsada));
    }
}
