package com.clinica.dao;

import java.sql.SQLException;

/**
 * Traduz uma SQLException em uma mensagem que diga o que fazer.
 * Usa o código de erro do MySQL (getErrorCode) e, para falhas de conexão,
 * o SQLState, o código padrão que todo banco devolve pelo JDBC.
 */
public final class ErrosBD {

    private ErrosBD() {
    }

    public static String descrever(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        return switch (e.getErrorCode()) {
            case 1049 -> "O banco de dados não existe. Rode o arquivo docs/codigo.mysql no MySQL.";
            case 1044, 1045 -> "Usuário ou senha do MySQL incorretos. Confira o arquivo banco.properties.";
            case 1146 -> "As tabelas do banco de dados não existem. Rode o arquivo docs/codigo.mysql no MySQL.";
            case 1205, 1213 -> "O banco de dados estava ocupado. Tente de novo.";
            case 1062 -> "Esse horário acabou de ser ocupado. Atualize a página e escolha outro.";
            default -> estado.startsWith("08")
                    ? "Não foi possível conectar ao MySQL. Verifique se ele está rodando (no XAMPP, clique em Start no MySQL)."
                    : "Erro no banco de dados. Veja os detalhes no terminal do servidor.";
        };
    }
}
