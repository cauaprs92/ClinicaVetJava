package com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.clinica.model.Animal;
import com.clinica.model.Cachorro;
import com.clinica.model.Gato;

/**
 * Acesso à tabela animal. Cachorros e gatos ficam na mesma tabela:
 * a coluna especie diz qual é, e cada um preenche a sua característica.
 */
public class AnimalDAO {

    private final Connection conexao;

    public AnimalDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /**
     * Grava o animal (o tutor já precisa ter id) e preenche o id dele.
     * Se o tutor já tem um animal com o mesmo nome, reaproveita o cadastro e atualiza
     * espécie, idade e característica: assim as consultas do mesmo paciente ficam juntas.
     */
    public void salvar(Animal animal) throws SQLException {
        String sql = """
                INSERT INTO animal (tutor_id, nome, especie, idade, porte, temperamento)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (tutor_id, nome) DO UPDATE
                SET especie = EXCLUDED.especie, idade = EXCLUDED.idade,
                    porte = EXCLUDED.porte, temperamento = EXCLUDED.temperamento
                RETURNING id
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, animal.getTutor().getId());
            stmt.setString(2, animal.getNome());
            stmt.setString(3, animal.getEspecie());
            stmt.setInt(4, animal.getIdade());
            // Cada espécie grava a sua característica; a outra coluna fica vazia (NULL).
            stmt.setString(5, animal instanceof Cachorro cachorro ? cachorro.getPorte() : null);
            stmt.setString(6, animal instanceof Gato gato ? gato.getTemperamento() : null);
            try (ResultSet resultado = stmt.executeQuery()) {
                resultado.next();
                animal.setId(resultado.getInt("id"));
            }
        }
    }
}
