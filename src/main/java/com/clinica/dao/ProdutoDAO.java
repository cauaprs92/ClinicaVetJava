package com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
