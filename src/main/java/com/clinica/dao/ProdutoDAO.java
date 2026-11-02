package com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.clinica.model.ProdutoFarmacia;

/** Acesso à tabela produto (farmácia). */
public class ProdutoDAO {

    private static final String SELECT = "SELECT id, nome, preco, quantidade FROM produto";

    private final Connection conexao;

    public ProdutoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public List<ProdutoFarmacia> listar() throws SQLException {
        List<ProdutoFarmacia> produtos = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " ORDER BY id");
             ResultSet resultado = stmt.executeQuery()) {
            while (resultado.next()) {
                produtos.add(ler(resultado));
            }
        }
        return produtos;
    }

    /** Devolve o item, ou null se não existir. */
    public ProdutoFarmacia buscarPorId(int id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet resultado = stmt.executeQuery()) {
                return resultado.next() ? ler(resultado) : null;
            }
        }
    }

    /** Devolve o item com esse nome (sem diferenciar maiúsculas), ou null se não existir. */
    public ProdutoFarmacia buscarPorNome(String nome) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " WHERE nome = ?")) {
            stmt.setString(1, nome);
            try (ResultSet resultado = stmt.executeQuery()) {
                return resultado.next() ? ler(resultado) : null;
            }
        }
    }

    /** Cadastra um item novo e preenche o id gerado pelo banco. */
    public void inserir(ProdutoFarmacia produto) throws SQLException {
        String sql = "INSERT INTO produto (nome, preco, quantidade) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produto.getNomeProduto());
            stmt.setBigDecimal(2, produto.getPreco());
            stmt.setInt(3, produto.getQuantidade());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) produto.setId(chaves.getInt(1));
            }
        }
    }

    /** Põe unidades no estoque (entrada de mercadoria). */
    public void reporEstoque(int id, int quantidade) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("UPDATE produto SET quantidade = quantidade + ? WHERE id = ?")) {
            stmt.setInt(1, quantidade);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /** Tira unidades do estoque. Devolve false (e não muda nada) se não houver o suficiente. */
    public boolean baixarEstoque(int id, int quantidade) throws SQLException {
        String sql = "UPDATE produto SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setInt(2, id);
            stmt.setInt(3, quantidade);
            return stmt.executeUpdate() == 1;
        }
    }

    private static ProdutoFarmacia ler(ResultSet resultado) throws SQLException {
        return new ProdutoFarmacia(
                resultado.getInt("id"),
                resultado.getString("nome"),
                resultado.getBigDecimal("preco"),
                resultado.getInt("quantidade"));
    }
}
