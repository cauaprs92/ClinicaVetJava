package com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.clinica.model.Horario;

/** Acesso à tabela horario. */
public class HorarioDAO {

    private static final String SELECT = "SELECT numero, hora, turno FROM horario";

    private final Connection conexao;

    public HorarioDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /** Os horários do dia, do mais cedo para o mais tarde. */
    public List<Horario> listar() throws SQLException {
        List<Horario> horarios = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " ORDER BY hora");
             ResultSet resultado = stmt.executeQuery()) {
            while (resultado.next()) {
                horarios.add(ler(resultado));
            }
        }
        return horarios;
    }

    /** Devolve o horário, ou null se não existir. */
    public Horario buscar(int numero) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " WHERE numero = ?")) {
            stmt.setInt(1, numero);
            try (ResultSet resultado = stmt.executeQuery()) {
                return resultado.next() ? ler(resultado) : null;
            }
        }
    }

    private static Horario ler(ResultSet resultado) throws SQLException {
        return new Horario(
                resultado.getInt("numero"),
                resultado.getObject("hora", LocalTime.class),
                resultado.getString("turno"));
    }
}
