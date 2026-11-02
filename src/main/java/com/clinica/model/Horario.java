package com.clinica.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Um dos 10 horários da agenda. Tabela: horario.
 * Se ele está livre ou ocupado depende das consultas de cada dia, não do horário.
 */
public class Horario {

    private int numero;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;
    /** "manha" ou "tarde". */
    private String turno;

    public Horario() {
    }

    public Horario(int numero, LocalTime hora, String turno) {
        this.numero = numero;
        this.hora = hora;
        this.turno = turno;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }
}
