package com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.clinica.model.Animal;
import com.clinica.model.Cachorro;
import com.clinica.model.Consulta;
import com.clinica.model.StatusConsulta;
import com.clinica.model.Tutor;

/** Acesso à tabela consulta (junto com o horário, o paciente, o tutor e o total da farmácia). */
public class ConsultaDAO {

    /** Consulta + hora + paciente + tutor + soma do que foi lançado na farmácia. */
    private static final String SELECT = """
            SELECT c.id, c.horario, h.hora, c.data, c.status, c.sintomas,
                   c.criada_em, c.iniciada_em, c.finalizada_em,
                   a.id AS animal_id, a.nome AS animal_nome, a.especie, a.idade, a.porte, a.temperamento,
                   t.id AS tutor_id, t.nome AS tutor_nome, t.email, t.telefone,
                   COALESCE((SELECT SUM(i.quantidade * i.preco_unitario)
                             FROM consumo i
                             WHERE i.consulta_id = c.id), 0) AS valor_consumido
            FROM consulta c
            JOIN horario h ON h.numero = c.horario
            JOIN animal a ON a.id = c.animal_id
            JOIN tutor t ON t.id = a.tutor_id
            """;

    private final Connection conexao;

    public ConsultaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Consulta> listar() throws SQLException {
        List<Consulta> consultas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " ORDER BY c.id");
             ResultSet resultado = stmt.executeQuery()) {
            while (resultado.next()) {
                consultas.add(ler(resultado));
            }
        }
        return consultas;
    }

    /** Devolve a consulta, ou null se não existir. */
    public Consulta buscarPorId(int id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT + " WHERE c.id = ?")) {
            stmt.setInt(1, id);
            return lerUma(stmt);
        }
    }

    /** Consulta marcada naquele horário do dia (no máximo uma), ou null. */
    public Consulta buscarDoHorario(LocalDate data, int horario, int ignorarId) throws SQLException {
        String sql = SELECT + " WHERE c.data = ? AND c.horario = ? AND c.id <> ? LIMIT 1";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, data);
            stmt.setInt(2, horario);
            stmt.setInt(3, ignorarId);
            return lerUma(stmt);
        }
    }

    /** Grava uma nova consulta e preenche o id gerado pelo banco (AUTO_INCREMENT). */
    public void inserir(Consulta consulta) throws SQLException {
        String sql = "INSERT INTO consulta (horario, data, animal_id, status, sintomas) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, consulta.getHorario());
            stmt.setObject(2, consulta.getData());
            stmt.setInt(3, consulta.getPaciente().getId());
            stmt.setString(4, consulta.getStatus().getCodigo());
            stmt.setString(5, consulta.getSintomas());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                chaves.next();
                consulta.setId(chaves.getInt(1));
            }
        }
    }

    /** Atualiza horário, data, paciente e sintomas. */
    public void atualizar(Consulta consulta) throws SQLException {
        String sql = "UPDATE consulta SET horario = ?, data = ?, animal_id = ?, sintomas = ? WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, consulta.getHorario());
            stmt.setObject(2, consulta.getData());
            stmt.setInt(3, consulta.getPaciente().getId());
            stmt.setString(4, consulta.getSintomas());
            stmt.setInt(5, consulta.getId());
            stmt.executeUpdate();
        }
    }

    public void iniciarAtendimento(int id) throws SQLException {
        String sql = "UPDATE consulta SET status = 'atendimento', iniciada_em = CURRENT_TIMESTAMP(3) WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void finalizar(int id) throws SQLException {
        String sql = "UPDATE consulta SET status = 'finalizada', finalizada_em = CURRENT_TIMESTAMP(3) WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("DELETE FROM consulta WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /* ---------- Leitura do ResultSet ---------- */

    private static Consulta lerUma(PreparedStatement stmt) throws SQLException {
        try (ResultSet resultado = stmt.executeQuery()) {
            return resultado.next() ? ler(resultado) : null;
        }
    }

    private static Consulta ler(ResultSet resultado) throws SQLException {
        Tutor tutor = new Tutor(
                resultado.getString("tutor_nome"),
                resultado.getString("email"),
                resultado.getString("telefone"));
        tutor.setId(resultado.getInt("tutor_id"));

        // A coluna especie decide se o objeto criado é um Cachorro ou um Gato.
        String especie = resultado.getString("especie");
        String caracteristica = Cachorro.ESPECIE.equals(especie)
                ? resultado.getString("porte")
                : resultado.getString("temperamento");
        Animal paciente = Animal.criar(especie, resultado.getString("animal_nome"),
                resultado.getInt("idade"), caracteristica, tutor);
        paciente.setId(resultado.getInt("animal_id"));

        Consulta consulta = new Consulta();
        consulta.setId(resultado.getInt("id"));
        consulta.setHorario(resultado.getInt("horario"));
        consulta.setHora(resultado.getObject("hora", LocalTime.class));
        consulta.setData(resultado.getObject("data", LocalDate.class));
        consulta.setPaciente(paciente);
        consulta.setStatus(StatusConsulta.deCodigo(resultado.getString("status")));
        consulta.setSintomas(resultado.getString("sintomas"));
        consulta.setValorConsumido(resultado.getBigDecimal("valor_consumido"));
        consulta.setCriadaEm(instante(resultado, "criada_em"));
        consulta.setIniciadaEm(instante(resultado, "iniciada_em"));
        consulta.setFinalizadaEm(instante(resultado, "finalizada_em"));
        return consulta;
    }

    /** Data e hora gravadas (ou null), em milissegundos, que é a precisão que o navegador entende. */
    private static Instant instante(ResultSet resultado, String coluna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(coluna);
        return valor == null ? null : valor.toInstant().truncatedTo(ChronoUnit.MILLIS);
    }
}
