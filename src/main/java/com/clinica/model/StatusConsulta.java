package com.clinica.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Ciclo de uma consulta:
 *   AGENDADA       → (iniciar atendimento)    → EM_ATENDIMENTO
 *   EM_ATENDIMENTO → (finalizar + pagamento)  → FINALIZADA
 *   AGENDADA       → (cancelar consulta)      → consulta apagada
 */
public enum StatusConsulta {

    AGENDADA("agendada", "Agendada"),
    EM_ATENDIMENTO("atendimento", "Em atendimento"),
    FINALIZADA("finalizada", "Finalizada");

    /** Valor gravado na coluna consulta.status e enviado ao site. */
    private final String codigo;
    /** Texto mostrado para as pessoas. */
    private final String rotulo;

    StatusConsulta(String codigo, String rotulo) {
        this.codigo = codigo;
        this.rotulo = rotulo;
    }

    @JsonValue
    public String getCodigo() {
        return codigo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static StatusConsulta deCodigo(String codigo) {
        for (StatusConsulta s : values()) {
            if (s.codigo.equals(codigo)) return s;
        }
        throw new IllegalArgumentException("Status de consulta desconhecido: " + codigo);
    }
}
