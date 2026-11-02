package com.clinica.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.clinica.model.DadosConsulta;
import com.clinica.model.EstadoClinica;
import com.clinica.model.ItemConsumo;
import com.clinica.service.ClinicaService;

/**
 * API que o site (public/app.js) usa para ler e gravar no banco.
 *
 *   GET    /api/estado                    horários, consultas e itens da farmácia
 *   POST   /api/consultas                 nova consulta (Agendada)
 *   PUT    /api/consultas/{id}            editar tutor, paciente, sintomas, data e horário
 *   POST   /api/consultas/{id}/iniciar    Agendada → Em atendimento
 *   POST   /api/consultas/{id}/finalizar  Em atendimento → Finalizada
 *   DELETE /api/consultas/{id}            cancelar consulta agendada
 *   POST   /api/consumos                  lançar itens da farmácia
 *   POST   /api/produtos                  cadastrar item novo na farmácia
 *   POST   /api/produtos/{id}/entrada     pôr mais unidades no estoque
 *   POST   /api/redefinir                 restaurar dados de exemplo
 *
 * As alterações respondem { "ok": true, "msg": "..." }. Os erros são tratados em TratadorDeErros.
 */
@RestController
@RequestMapping("/api")
public class ClinicaApiController {

    /** Resposta das alterações: deu certo ou não, e a mensagem para mostrar na tela. */
    public record Resposta(boolean ok, String msg) {
        static Resposta sucesso(String msg) {
            return new Resposta(true, msg);
        }
    }

    public record DadosFinalizacao(Boolean pagamentoConfirmado) {
    }

    public record DadosConsumo(Integer consulta, List<ItemConsumo> itens) {
    }

    public record DadosProduto(String nome, BigDecimal preco, Integer quantidade) {
    }

    public record DadosEntrada(Integer quantidade) {
    }

    private final ClinicaService clinicaService;

    public ClinicaApiController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    @GetMapping("/estado")
    public EstadoClinica estado() throws SQLException {
        return clinicaService.carregarEstado();
    }

    @PostMapping("/consultas")
    @ResponseStatus(HttpStatus.CREATED)
    public Resposta agendar(@RequestBody DadosConsulta d) throws SQLException {
        return Resposta.sucesso(clinicaService.agendar(d));
    }

    @PutMapping("/consultas/{id}")
    public Resposta editar(@PathVariable int id, @RequestBody DadosConsulta d) throws SQLException {
        return Resposta.sucesso(clinicaService.editarConsulta(id, d));
    }

    @PostMapping("/consultas/{id}/iniciar")
    public Resposta iniciar(@PathVariable int id) throws SQLException {
        return Resposta.sucesso(clinicaService.iniciarAtendimento(id));
    }

    @PostMapping("/consultas/{id}/finalizar")
    public Resposta finalizar(@PathVariable int id,
                              @RequestBody(required = false) DadosFinalizacao d) throws SQLException {
        boolean pago = d != null && Boolean.TRUE.equals(d.pagamentoConfirmado());
        return Resposta.sucesso(clinicaService.finalizarConsulta(id, pago));
    }

    @DeleteMapping("/consultas/{id}")
    public Resposta cancelar(@PathVariable int id) throws SQLException {
        return Resposta.sucesso(clinicaService.cancelarConsulta(id));
    }

    @PostMapping("/consumos")
    public Resposta consumo(@RequestBody DadosConsumo d) throws SQLException {
        return Resposta.sucesso(clinicaService.registrarConsumo(d.consulta(), d.itens()));
    }

    @PostMapping("/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    public Resposta cadastrarProduto(@RequestBody DadosProduto d) throws SQLException {
        return Resposta.sucesso(clinicaService.cadastrarProduto(d.nome(), d.preco(), d.quantidade()));
    }

    @PostMapping("/produtos/{id}/entrada")
    public Resposta reporEstoque(@PathVariable int id, @RequestBody DadosEntrada d) throws SQLException {
        return Resposta.sucesso(clinicaService.reporEstoque(id, d.quantidade()));
    }

    @PostMapping("/redefinir")
    public Resposta redefinir() throws SQLException {
        return Resposta.sucesso(clinicaService.redefinir());
    }
}
