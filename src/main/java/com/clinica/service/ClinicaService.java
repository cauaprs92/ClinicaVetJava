package com.clinica.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.clinica.dao.AnimalDAO;
import com.clinica.dao.ConexaoBD;
import com.clinica.dao.ConsultaDAO;
import com.clinica.dao.ConsumoDAO;
import com.clinica.dao.HorarioDAO;
import com.clinica.dao.InicializadorBD;
import com.clinica.dao.ProdutoDAO;
import com.clinica.dao.TutorDAO;
import com.clinica.model.Animal;
import com.clinica.model.Cachorro;
import com.clinica.model.Consulta;
import com.clinica.model.DadosConsulta;
import com.clinica.model.DadosConsulta.DadosPaciente;
import com.clinica.model.DadosConsulta.DadosTutor;
import com.clinica.model.EstadoClinica;
import com.clinica.model.Gato;
import com.clinica.model.Horario;
import com.clinica.model.ItemConsumo;
import com.clinica.model.ProdutoFarmacia;
import com.clinica.model.StatusConsulta;
import com.clinica.model.Tutor;

/**
 * Regras da clínica: agenda, atendimento, finalização e farmácia.
 *
 * Cada operação abre uma conexão, faz tudo dentro de uma transação e confirma (commit)
 * no final. Se uma regra falhar no meio, nada é gravado (rollback).
 *
 * Regras da agenda: cada horário de cada dia recebe no máximo uma consulta, e só dá
 * para agendar (ou remarcar) em horários que ainda não passaram.
 */
@Service
public class ClinicaService {

    private static final Locale BRASIL = Locale.of("pt", "BR");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // Mesmos limites das colunas das tabelas tutor, animal e consulta.
    private static final int MAX_NOME = 120;
    private static final int MAX_EMAIL = 160;
    private static final int MAX_NOME_ANIMAL = 80;
    private static final int MAX_SINTOMAS = 500;
    private static final int IDADE_MAXIMA = 40;

    // Limites da farmácia (a coluna produto.nome tem 80 caracteres).
    private static final int MAX_NOME_PRODUTO = 80;
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999.99");
    private static final int MAX_ENTRADA = 1000;
    private static final int MAX_ESTOQUE = 99999;

    /** Celular "(11) 90000-0000" (começa com 9) ou fixo "(11) 3000-0000" (começa com 2 a 5), DDD de 11 a 99. */
    private static final Pattern TELEFONE = Pattern.compile("\\([1-9]\\d\\) (9\\d{4}|[2-5]\\d{3})-\\d{4}");

    /** Nome da trava do MySQL que deixa uma alteração por vez (veja alterar()). */
    private static final String TRAVA_ALTERACOES = "clinicavet.alteracoes";

    private final ConexaoBD conexaoBD;
    private final InicializadorBD inicializadorBD;
    private final ZoneId fuso;

    public ClinicaService(ConexaoBD conexaoBD, InicializadorBD inicializadorBD,
                          @Value("${clinicavet.fuso}") String fuso) {
        this.conexaoBD = conexaoBD;
        this.inicializadorBD = inicializadorBD;
        this.fuso = ZoneId.of(fuso);
    }

    /* ---------- Consultas ao banco ---------- */

    /** Horários, consultas, itens e vendas da farmácia, lidos juntos para a tela ficar coerente. */
    public EstadoClinica carregarEstado() throws SQLException {
        return consultar(conexao -> new EstadoClinica(
                new HorarioDAO(conexao).listar(),
                new ConsultaDAO(conexao).listar(),
                new ProdutoDAO(conexao).listar(),
                new ConsumoDAO(conexao).listarVendasRecentes()));
    }

    /* ---------- Consultas da clínica ---------- */

    public String agendar(DadosConsulta d) throws SQLException {
        return alterar(conexao -> {
            ConsultaDAO consultas = new ConsultaDAO(conexao);
            LocalDate data = lerData(d.data());
            Horario horario = validarAgendamento(data, d.horario(), new HorarioDAO(conexao));
            Consulta ocupante = consultas.buscarDoHorario(data, horario.getNumero(), 0);
            if (ocupante != null) throw erro(descreverOcupante(ocupante));
            Tutor tutor = validarTutor(d.tutor());
            Animal paciente = validarPaciente(d.paciente(), tutor);
            String sintomas = validarSintomas(d.sintomas());

            new TutorDAO(conexao).salvar(tutor);
            new AnimalDAO(conexao).salvar(paciente);
            Consulta consulta = new Consulta();
            consulta.setHorario(horario.getNumero());
            consulta.setData(data);
            consulta.setPaciente(paciente);
            consulta.setStatus(StatusConsulta.AGENDADA);
            consulta.setSintomas(sintomas);
            consultas.inserir(consulta);

            return "Consulta #" + consulta.getCodigo() + " de " + paciente.getNome() + " agendada para "
                    + dataCurta(data) + " às " + hora(horario.getHora()) + ".";
        });
    }

    /** O paciente chegou: só no dia da consulta. */
    public String iniciarAtendimento(int id) throws SQLException {
        return alterar(conexao -> {
            ConsultaDAO consultas = new ConsultaDAO(conexao);
            Consulta c = consultas.buscarPorId(id);
            if (c == null || c.getStatus() != StatusConsulta.AGENDADA) {
                throw erro("Só consultas agendadas podem iniciar o atendimento.");
            }
            LocalDate hoje = hoje();
            if (hoje.isBefore(c.getData())) {
                throw erro("O atendimento desta consulta é no dia " + dataCurta(c.getData()) + ".");
            }
            if (hoje.isAfter(c.getData())) {
                throw erro("Esta consulta era para " + dataCurta(c.getData()) + ". Remarque a data para atender.");
            }
            consultas.iniciarAtendimento(id);
            return "Atendimento da consulta #" + c.getCodigo() + " iniciado: " + c.getPaciente().getNome()
                    + " está em atendimento.";
        });
    }

    public String cancelarConsulta(int id) throws SQLException {
        return alterar(conexao -> {
            ConsultaDAO consultas = new ConsultaDAO(conexao);
            Consulta c = consultas.buscarPorId(id);
            if (c == null || c.getStatus() != StatusConsulta.AGENDADA) {
                throw erro("Só consultas agendadas podem ser canceladas.");
            }
            consultas.excluir(id);
            return "Consulta #" + c.getCodigo() + " cancelada. O horário das " + hora(c.getHora())
                    + " de " + dataCurta(c.getData()) + " está livre.";
        });
    }

    /** Finaliza o atendimento. Com itens da farmácia, exige a confirmação do pagamento. */
    public String finalizarConsulta(int id, boolean pagamentoConfirmado) throws SQLException {
        return alterar(conexao -> {
            ConsultaDAO consultas = new ConsultaDAO(conexao);
            Consulta c = consultas.buscarPorId(id);
            if (c == null || c.getStatus() != StatusConsulta.EM_ATENDIMENTO) {
                throw erro("Só consultas em atendimento podem ser finalizadas.");
            }
            if (c.temConsumo() && !pagamentoConfirmado) {
                throw erro("Confirme o pagamento da farmácia para finalizar.");
            }
            consultas.finalizar(id);
            return "Consulta #" + c.getCodigo() + " de " + c.getPaciente().getNome() + " finalizada.";
        });
    }

    /**
     * Atualiza tutor, paciente, sintomas, data e horário. Com o paciente em atendimento,
     * a data e o horário não mudam.
     */
    public String editarConsulta(int id, DadosConsulta d) throws SQLException {
        return alterar(conexao -> {
            ConsultaDAO consultas = new ConsultaDAO(conexao);
            Consulta c = consultas.buscarPorId(id);
            if (c == null || !c.isAtiva()) throw erro("Essa consulta não pode mais ser editada.");

            boolean emAtendimento = c.getStatus() == StatusConsulta.EM_ATENDIMENTO;
            LocalDate data = emAtendimento ? c.getData() : lerData(d.data());
            Integer numero = emAtendimento ? Integer.valueOf(c.getHorario()) : d.horario();
            boolean remarcou = !Objects.equals(data, c.getData()) || !Objects.equals(numero, c.getHorario());
            if (remarcou) {
                Horario horario = validarAgendamento(data, numero, new HorarioDAO(conexao));
                Consulta ocupante = consultas.buscarDoHorario(data, horario.getNumero(), c.getId());
                if (ocupante != null) throw erro(descreverOcupante(ocupante));
                c.setHora(horario.getHora());
            }
            Tutor tutor = validarTutor(d.tutor());
            Animal paciente = validarPaciente(d.paciente(), tutor);
            String sintomas = validarSintomas(d.sintomas());

            new TutorDAO(conexao).salvar(tutor);
            new AnimalDAO(conexao).salvar(paciente);
            c.setPaciente(paciente);
            c.setSintomas(sintomas);
            c.setData(data);
            c.setHorario(numero);
            consultas.atualizar(c);

            return remarcou
                    ? "Consulta #" + c.getCodigo() + " remarcada para " + dataCurta(data) + " às " + hora(c.getHora()) + "."
                    : "Consulta #" + c.getCodigo() + " atualizada.";
        });
    }

    /* ---------- Farmácia ---------- */

    private record Lancamento(ProdutoFarmacia produto, int quantidade) {
    }

    /**
     * Lança vários itens de uma vez na consulta em atendimento.
     * Confere tudo antes; se algum item falhar, nada é lançado.
     */
    public String registrarConsumo(Integer consultaId, List<ItemConsumo> itens) throws SQLException {
        return alterar(conexao -> {
            Consulta c = consultaId == null ? null : new ConsultaDAO(conexao).buscarPorId(consultaId);
            if (c == null || c.getStatus() != StatusConsulta.EM_ATENDIMENTO) {
                throw erro("Escolha uma consulta em atendimento.");
            }

            // Soma as quantidades de cada item (o mesmo item pode vir mais de uma vez).
            Map<Integer, Long> pedidos = new LinkedHashMap<>();
            if (itens != null) {
                for (ItemConsumo item : itens) {
                    if (item == null || item.quantidade() == null || item.quantidade() <= 0) continue;
                    if (item.produtoId() == null) throw erro("Item inválido.");
                    pedidos.merge(item.produtoId(), (long) item.quantidade(), Long::sum);
                }
            }
            if (pedidos.isEmpty()) throw erro("Adicione pelo menos um item.");

            ProdutoDAO produtos = new ProdutoDAO(conexao);
            List<Lancamento> lancamentos = new ArrayList<>();
            for (Map.Entry<Integer, Long> pedido : pedidos.entrySet()) {
                ProdutoFarmacia produto = produtos.buscarPorId(pedido.getKey());
                if (produto == null) throw erro("Item inválido.");
                if (pedido.getValue() > produto.getQuantidade()) {
                    throw erro(produto.getNomeProduto() + ": só há " + produto.getQuantidade() + " em estoque.");
                }
                lancamentos.add(new Lancamento(produto, pedido.getValue().intValue()));
            }

            ConsumoDAO consumos = new ConsumoDAO(conexao);
            BigDecimal valor = BigDecimal.ZERO;
            int unidades = 0;
            for (Lancamento l : lancamentos) {
                ProdutoFarmacia produto = l.produto();
                if (!produtos.baixarEstoque(produto.getId(), l.quantidade())) {
                    throw erro(produto.getNomeProduto() + ": estoque insuficiente.");
                }
                consumos.inserir(c.getId(), produto.getId(), l.quantidade(), produto.getPreco());
                valor = valor.add(produto.calcularTotal(l.quantidade()));
                unidades += l.quantidade();
            }
            String rotulo = unidades == 1 ? "1 item lançado" : unidades + " itens lançados";
            return rotulo + " na consulta #" + c.getCodigo() + " (" + c.getPaciente().getNome() + ") · " + brl(valor) + ".";
        });
    }

    /** Cadastra um item novo na farmácia, já com o estoque inicial. */
    public String cadastrarProduto(String nome, BigDecimal preco, Integer quantidade) throws SQLException {
        return alterar(conexao -> {
            String texto = texto(nome);
            if (texto.isEmpty()) throw erro("Informe o nome do item.");
            if (texto.length() > MAX_NOME_PRODUTO) throw erro("O nome do item pode ter até " + MAX_NOME_PRODUTO + " caracteres.");
            if (preco == null || preco.signum() < 0 || preco.compareTo(PRECO_MAXIMO) > 0) {
                throw erro("Informe um preço de R$ 0,00 a " + brl(PRECO_MAXIMO) + ".");
            }
            if (preco.stripTrailingZeros().scale() > 2) throw erro("O preço pode ter no máximo 2 casas decimais.");
            int qtd = validarEntrada(quantidade);

            ProdutoDAO produtos = new ProdutoDAO(conexao);
            ProdutoFarmacia existente = produtos.buscarPorNome(texto);
            if (existente != null) {
                throw erro(existente.getNomeProduto() + " já está no estoque. Escolha o item na lista para repor.");
            }
            ProdutoFarmacia produto = new ProdutoFarmacia(0, texto, preco.setScale(2), qtd);
            produtos.inserir(produto);
            return produto.getNomeProduto() + " cadastrado com " + unidades(qtd) + " · " + brl(produto.getPreco()) + " cada.";
        });
    }

    /** Entrada de mercadoria: soma unidades ao estoque de um item que já existe. */
    public String reporEstoque(int id, Integer quantidade) throws SQLException {
        return alterar(conexao -> {
            ProdutoDAO produtos = new ProdutoDAO(conexao);
            ProdutoFarmacia produto = produtos.buscarPorId(id);
            if (produto == null) throw erro("Item inválido.");
            int qtd = validarEntrada(quantidade);
            if (produto.getQuantidade() + qtd > MAX_ESTOQUE) {
                throw erro(produto.getNomeProduto() + ": o estoque pode ter até " + MAX_ESTOQUE + " unidades.");
            }
            produtos.reporEstoque(id, qtd);
            return unidades(qtd) + " de " + produto.getNomeProduto() + " no estoque · agora são "
                    + (produto.getQuantidade() + qtd) + ".";
        });
    }

    private static int validarEntrada(Integer quantidade) {
        if (quantidade == null || quantidade < 1 || quantidade > MAX_ENTRADA) {
            throw erro("Informe uma quantidade de 1 a " + MAX_ENTRADA + ".");
        }
        return quantidade;
    }

    private static String unidades(int n) {
        return n == 1 ? "1 unidade" : n + " unidades";
    }

    /* ---------- Dados de exemplo ---------- */

    public String redefinir() throws SQLException {
        return alterar(conexao -> {
            inicializadorBD.redefinir(conexao);
            return "Dados de exemplo restaurados.";
        });
    }

    /* ---------- Validações ---------- */

    /** Data a partir de hoje e horário que existe e ainda não passou. */
    private Horario validarAgendamento(LocalDate data, Integer numero, HorarioDAO horarios) throws SQLException {
        if (data == null) throw erro("Informe a data da consulta.");
        if (data.isBefore(hoje())) throw erro("A consulta não pode ser em uma data que já passou.");
        Horario horario = numero == null ? null : horarios.buscar(numero);
        if (horario == null) throw erro("Escolha um horário válido.");
        if (data.equals(hoje()) && horario.getHora().isBefore(agora())) {
            throw erro("O horário das " + hora(horario.getHora()) + " de hoje já passou.");
        }
        return horario;
    }

    private static Tutor validarTutor(DadosTutor t) {
        String nome = texto(t == null ? null : t.nome());
        String email = texto(t == null ? null : t.email());
        String telefone = texto(t == null ? null : t.telefone());
        if (nome.isEmpty() || email.isEmpty() || telefone.isEmpty()) throw erro("Preencha nome, e-mail e telefone do tutor.");
        if (nome.length() > MAX_NOME) throw erro("O nome do tutor pode ter até " + MAX_NOME + " caracteres.");
        if (email.length() > MAX_EMAIL || !email.matches("[^@\\s]+@[^@\\s]+")) throw erro("Informe um e-mail válido.");
        if (!TELEFONE.matcher(telefone).matches()) throw erro("Informe um telefone válido, como (11) 90000-0000.");
        return new Tutor(nome, email, telefone);
    }

    /** Monta um Cachorro (com porte) ou um Gato (com temperamento). */
    private static Animal validarPaciente(DadosPaciente p, Tutor tutor) {
        if (p == null) throw erro("Informe os dados do animal.");
        String nome = texto(p.nome());
        if (nome.isEmpty()) throw erro("Informe o nome do animal.");
        if (nome.length() > MAX_NOME_ANIMAL) throw erro("O nome do animal pode ter até " + MAX_NOME_ANIMAL + " caracteres.");
        String especie = texto(p.especie());
        if (!especie.equals(Cachorro.ESPECIE) && !especie.equals(Gato.ESPECIE)) {
            throw erro("Escolha a espécie do animal: cachorro ou gato.");
        }
        if (p.idade() == null || p.idade() < 0 || p.idade() > IDADE_MAXIMA) {
            throw erro("Informe a idade do animal, de 0 a " + IDADE_MAXIMA + " anos.");
        }
        String caracteristica;
        if (especie.equals(Cachorro.ESPECIE)) {
            caracteristica = texto(p.porte());
            if (!Cachorro.PORTES.contains(caracteristica)) throw erro("Escolha o porte do animal.");
        } else {
            caracteristica = texto(p.temperamento());
            if (!Gato.TEMPERAMENTOS.contains(caracteristica)) throw erro("Escolha o temperamento do animal.");
        }
        return Animal.criar(especie, nome, p.idade(), caracteristica, tutor);
    }

    private static String validarSintomas(String sintomas) {
        String texto = texto(sintomas);
        if (texto.isEmpty()) throw erro("Descreva os sintomas ou a queixa principal.");
        if (texto.length() > MAX_SINTOMAS) throw erro("Os sintomas podem ter até " + MAX_SINTOMAS + " caracteres.");
        return texto;
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Data no formato AAAA-MM-DD, ou null se vier vazia ou inválida. */
    private static LocalDate lerData(String texto) {
        if (texto == null) return null;
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String descreverOcupante(Consulta c) {
        return "O horário das " + hora(c.getHora()) + " de " + dataCurta(c.getData()) + " já está com "
                + c.getPaciente().getNome() + " (consulta #" + c.getCodigo() + ").";
    }

    private static RegraNegocioException erro(String mensagem) {
        return new RegraNegocioException(mensagem);
    }

    /* ---------- Formatação (igual à do site) ---------- */

    private LocalDate hoje() {
        return LocalDate.now(fuso);
    }

    private LocalTime agora() {
        return LocalTime.now(fuso).truncatedTo(ChronoUnit.MINUTES);
    }

    /** "31/10", ou "31/10/27" quando o ano é outro. */
    private String dataCurta(LocalDate data) {
        return data.format(data.getYear() == hoje().getYear() ? DIA_MES : DIA_MES_ANO);
    }

    private static String hora(LocalTime hora) {
        return hora.format(HORA);
    }

    private static String brl(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(BRASIL).format(valor);
    }

    /* ---------- Transações ---------- */

    @FunctionalInterface
    private interface TarefaBD<T> {
        T executar(Connection conexao) throws SQLException;
    }

    /** Só leitura: todas as consultas enxergam o banco no mesmo instante. */
    private <T> T consultar(TarefaBD<T> tarefa) throws SQLException {
        try (Connection conexao = conexaoBD.conectar()) {
            conexao.setReadOnly(true);
            conexao.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            return emTransacao(conexao, tarefa);
        }
    }

    /**
     * Alteração: antes de tudo, pega a trava das alterações (GET_LOCK do MySQL). Assim, duas
     * pessoas não conseguem, por exemplo, agendar o mesmo horário ao mesmo tempo; a segunda
     * espera a primeira terminar e depois vê o horário já ocupado. Leituras continuam liberadas.
     */
    private <T> T alterar(TarefaBD<T> tarefa) throws SQLException {
        try (Connection conexao = conexaoBD.conectar()) {
            travar(conexao);
            try {
                return emTransacao(conexao, tarefa);
            } finally {
                liberar(conexao);
            }
        }
    }

    /** Espera até 10 segundos pela trava; se outra alteração demorar mais, desiste. */
    private static void travar(Connection conexao) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            stmt.setString(1, TRAVA_ALTERACOES);
            stmt.setInt(2, 10);
            try (ResultSet resultado = stmt.executeQuery()) {
                if (!resultado.next() || resultado.getInt(1) != 1) {
                    throw erro("O sistema está ocupado com outra alteração. Tente de novo.");
                }
            }
        }
    }

    private static void liberar(Connection conexao) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            stmt.setString(1, TRAVA_ALTERACOES);
            stmt.executeQuery().close();
        }
    }

    private static <T> T emTransacao(Connection conexao, TarefaBD<T> tarefa) throws SQLException {
        conexao.setAutoCommit(false);
        try {
            T resultado = tarefa.executar(conexao);
            conexao.commit();
            return resultado;
        } catch (SQLException | RuntimeException e) {
            try {
                conexao.rollback();
            } catch (SQLException falhaNoRollback) {
                e.addSuppressed(falhaNoRollback);
            }
            throw e;
        }
    }
}
