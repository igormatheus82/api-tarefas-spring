package com.igorcavalcante.tarefas.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato padrão de erro devolvido pela API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos,
        LocalDateTime timestamp
) {
    public ErroResponse(int status, String erro, String mensagem) {
        this(status, erro, mensagem, null, LocalDateTime.now());
    }

    public ErroResponse(int status, String erro, String mensagem, Map<String, String> campos) {
        this(status, erro, mensagem, campos, LocalDateTime.now());
    }
}
