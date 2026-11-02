package com.clinica.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Prepara o banco quando o servidor inicia, com os mesmos scripts da pasta docs/:
 *   codigo.mysql: cria o banco e as tabelas que faltam;
 *   insert.mysql: coloca os dados de exemplo, só se o banco ainda estiver vazio.
 * Assim o site funciona mesmo que ninguém tenha rodado os scripts no MySQL antes.
 */
@Component
public class InicializadorBD implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorBD.class);

    // Os arquivos de docs/ vão para dentro do .jar, na pasta banco/ (veja o pom.xml).
    private static final String SCRIPT_TABELAS = "banco/codigo.mysql";
    private static final String SCRIPT_DADOS = "banco/insert.mysql";

    /** Esvazia todas as tabelas e volta os ids para 1 (o TRUNCATE exige desligar as chaves estrangeiras). */
    private static final String APAGAR_TUDO = """
            SET FOREIGN_KEY_CHECKS = 0;
            TRUNCATE TABLE consumo;
            TRUNCATE TABLE consulta;
            TRUNCATE TABLE animal;
            TRUNCATE TABLE tutor;
            TRUNCATE TABLE produto;
            TRUNCATE TABLE horario;
            SET FOREIGN_KEY_CHECKS = 1;
            """;

    private final ConexaoBD conexaoBD;

    public InicializadorBD(ConexaoBD conexaoBD) {
        this.conexaoBD = conexaoBD;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conexao = conexaoBD.conectarCriandoBanco();
             Statement stmt = conexao.createStatement()) {
            stmt.execute(lerScript(SCRIPT_TABELAS));
            if (bancoVazio(stmt)) {
                stmt.execute(lerScript(SCRIPT_DADOS));
                log.info("Dados de exemplo inseridos (docs/insert.mysql).");
            }
            log.info("Banco de dados pronto em {}", conexaoBD.getUrl());
        } catch (SQLException e) {
            // O site continua no ar e mostra o aviso; o detalhe fica aqui no terminal.
            log.error("Não foi possível preparar o banco em {}: {} ({})",
                    conexaoBD.getUrl(), ErrosBD.descrever(e), e.getMessage());
        }
    }

    /** Apaga consultas, animais, tutores e lançamentos e volta aos dados de docs/insert.mysql. */
    public void redefinir(Connection conexao) throws SQLException {
        try (Statement stmt = conexao.createStatement()) {
            stmt.execute(APAGAR_TUDO);
            stmt.execute(lerScript(SCRIPT_DADOS));
        }
    }

    /** Sem horários cadastrados, o insert.mysql ainda não foi rodado. */
    private static boolean bancoVazio(Statement stmt) throws SQLException {
        try (ResultSet resultado = stmt.executeQuery("SELECT COUNT(*) FROM horario")) {
            return resultado.next() && resultado.getInt(1) == 0;
        }
    }

    private static String lerScript(String caminho) {
        try (InputStream entrada = new ClassPathResource(caminho).getInputStream()) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o script " + caminho, e);
        }
    }
}
