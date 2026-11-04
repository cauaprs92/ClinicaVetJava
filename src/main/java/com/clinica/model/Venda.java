package com.clinica.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Um lançamento da farmácia (tabela consumo), usado na Home: no gráfico de remédios
 * mais vendidos e no card de total vendido. O preço é o gravado no momento do lançamento.
 */
public record Venda(int produtoId, int quantidade, BigDecimal precoUnitario, Instant lancadoEm) {
}
