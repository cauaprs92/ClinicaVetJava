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
        // E-mail repetido (chave única): em vez de dar erro, atualiza o cadastro que já existe.
        String sql = """
                INSERT INTO tutor (nome, email, telefone) VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE nome = VALUES(nome), telefone = VALUES(telefone)
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, tutor.getNome());
            stmt.setString(2, tutor.getEmail());
            stmt.setString(3, tutor.getTelefone());
            stmt.executeUpdate();
        }
        // Novo ou já existente, o id é o do tutor com esse e-mail.
        try (PreparedStatement stmt = conexao.prepareStatement("SELECT id FROM tutor WHERE email = ?")) {
            stmt.setString(1, tutor.getEmail());
            try (ResultSet resultado = stmt.executeQuery()) {
                resultado.next();
                tutor.setId(resultado.getInt("id"));
            }
        }
    }
}
