package com.clinica.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.clinica.model.Venda;

/** Acesso à tabela consumo: cada item da farmácia lançado na conta de uma consulta. */
public class ConsumoDAO {

    private final Connection conexao;

    public ConsumoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void inserir(int consultaId, int produtoId, int quantidade, BigDecimal precoUnitario) throws SQLException {
        String sql = "INSERT INTO consumo (consulta_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, consultaId);
            stmt.setInt(2, produtoId);
            stmt.setInt(3, quantidade);
            stmt.setBigDecimal(4, precoUnitario);
            stmt.executeUpdate();
        }
    }

    /** Lançamentos dos últimos 13 meses (o maior filtro do gráfico da Home é 1 ano), do mais antigo ao mais novo. */
    public List<Venda> listarVendasRecentes() throws SQLException {
        String sql = "SELECT produto_id, quantidade, preco_unitario, lancado_em FROM consumo"
                + " WHERE lancado_em >= CURDATE() - INTERVAL 13 MONTH ORDER BY lancado_em, id";
        List<Venda> vendas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet resultado = stmt.executeQuery()) {
            while (resultado.next()) {
                vendas.add(new Venda(
                        resultado.getInt("produto_id"),
                        resultado.getInt("quantidade"),
                        resultado.getBigDecimal("preco_unitario"),
                        resultado.getTimestamp("lancado_em").toInstant().truncatedTo(ChronoUnit.MILLIS)));
            }
        }
        return vendas;
    }
}
