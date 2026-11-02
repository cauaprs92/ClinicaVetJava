package com.clinica.service;

/** Pedido que fere uma regra da clínica (horário ocupado, data inválida...). A mensagem vai para a tela. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
