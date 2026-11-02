package com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.clinica.model.Tutor;

/** Acesso à tabela tutor. */
public class TutorDAO {

    private final Connection conexao;

    public TutorDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /**
     * Grava o tutor e preenche o id dele.
     * Se já existe alguém com o mesmo e-mail, reaproveita o cadastro e atualiza nome e telefone.
     */
    public void salvar(Tutor tutor) throws SQLException {
        String sql = """
                INSERT INTO tutor (nome, email, telefone) VALUES (?, ?, ?)
                ON CONFLICT (email) DO UPDATE SET nome = EXCLUDED.nome, telefone = EXCLUDED.telefone
                RETURNING id
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, tutor.getNome());
            stmt.setString(2, tutor.getEmail());
            stmt.setString(3, tutor.getTelefone());
            try (ResultSet resultado = stmt.executeQuery()) {
                resultado.next();
                tutor.setId(resultado.getInt("id"));
            }
        }
    }
}
