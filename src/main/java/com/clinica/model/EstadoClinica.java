package com.clinica.model;

import java.util.List;

/** Tudo o que o site precisa para montar as telas, lido do banco de uma vez. */
public record EstadoClinica(List<Horario> horarios, List<Consulta> consultas, List<ProdutoFarmacia> produtos) {
}
