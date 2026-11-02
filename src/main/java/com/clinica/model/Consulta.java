package com.clinica.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Consulta de um paciente em um horário de um dia. Tabela: consulta.
 * Cada horário de cada dia recebe no máximo uma consulta.
 */
public class Consulta {

    private int id;
    /** Número do horário (1 a 10). */
    private int horario;
    /** Hora do horário ("08:00"), lida junto com a consulta. */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;
    private LocalDate data;
    // Polimorfismo: paciente pode guardar um Cachorro ou um Gato.
    private Animal paciente;
    private StatusConsulta status;
    private String sintomas;
    /** Soma dos itens da farmácia lançados (calculada a partir da tabela consumo). */
    private BigDecimal valorConsumido = BigDecimal.ZERO;
    private Instant criadaEm;
    private Instant iniciadaEm;
    private Instant finalizadaEm;

    /** Código mostrado no site: o id com 4 dígitos (0001, 0002...). Nunca é reaproveitado. */
    public String getCodigo() {
        return String.format("%04d", id);
    }

    /** Agendada ou em atendimento: ainda pode ser editada. */
    @JsonIgnore
    public boolean isAtiva() {
        return status != StatusConsulta.FINALIZADA;
    }

    @JsonIgnore
    public boolean temConsumo() {
        return valorConsumido.signum() > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHorario() {
        return horario;
    }

    public void setHorario(int horario) {
        this.horario = horario;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Animal getPaciente() {
        return paciente;
    }

    public void setPaciente(Animal paciente) {
        this.paciente = paciente;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public BigDecimal getValorConsumido() {
        return valorConsumido;
    }

    public void setValorConsumido(BigDecimal valorConsumido) {
        this.valorConsumido = valorConsumido;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Instant criadaEm) {
        this.criadaEm = criadaEm;
    }

    public Instant getIniciadaEm() {
        return iniciadaEm;
    }

    public void setIniciadaEm(Instant iniciadaEm) {
        this.iniciadaEm = iniciadaEm;
    }

    public Instant getFinalizadaEm() {
        return finalizadaEm;
    }

    public void setFinalizadaEm(Instant finalizadaEm) {
        this.finalizadaEm = finalizadaEm;
    }
}
