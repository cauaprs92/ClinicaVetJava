package com.clinica.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Abre conexões JDBC com o MySQL.
 *
 * A URL, o usuário e a senha vêm do arquivo banco.properties, na raiz do projeto.
 */
@Component
public class ConexaoBD {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoBD(@Value("${clinicavet.bd.url}") String url,
                     @Value("${clinicavet.bd.usuario}") String usuario,
                     @Value("${clinicavet.bd.senha}") String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;

        // Carrega o driver do MySQL, para o JDBC reconhecer URLs "jdbc:mysql://".
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC do MySQL não encontrado: " + e.getMessage(), e);
        }
        // Se o banco não responder em 10 segundos, desiste (em vez de travar o site).
        DriverManager.setLoginTimeout(10);
    }

    /** Cria a ponte com o banco de dados. Quem chama deve fechar a conexão (try-with-resources). */
    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, propriedades(false));
    }

    /**
     * Conexão usada só quando o servidor inicia: se o banco "clinicavet" ainda não
     * existir, o próprio driver cria (createDatabaseIfNotExist).
     */
    public Connection conectarCriandoBanco() throws SQLException {
        return DriverManager.getConnection(url, propriedades(true));
    }

    private Properties propriedades(boolean criarBanco) {
        Properties p = new Properties();
        p.setProperty("user", usuario);
        p.setProperty("password", senha);
        // Acentos e "ç" chegam ao banco certinhos (utf8mb4).
        p.setProperty("characterEncoding", "UTF-8");
        // Permite rodar os scripts de docs/ (vários comandos SQL de uma vez).
        p.setProperty("allowMultiQueries", "true");
        if (criarBanco) p.setProperty("createDatabaseIfNotExist", "true");
        return p;
    }

    public String getUrl() {
        return url;
    }
}
