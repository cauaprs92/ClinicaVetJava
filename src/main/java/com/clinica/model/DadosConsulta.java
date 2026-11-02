package com.clinica.model;

/**
 * O que o site envia para agendar ou editar uma consulta:
 * { horario, data, sintomas, tutor: { nome, email, telefone },
 *   paciente: { nome, especie, idade, porte | temperamento } }
 */
public record DadosConsulta(Integer horario, String data, String sintomas, DadosTutor tutor, DadosPaciente paciente) {

    public record DadosTutor(String nome, String email, String telefone) {
    }

    /** O cachorro informa o porte; o gato, o temperamento. */
    public record DadosPaciente(String nome, String especie, Integer idade, String porte, String temperamento) {
    }
}
