package com.igorcavalcante.tarefas.dto;

import com.igorcavalcante.tarefas.model.Prioridade;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dados enviados pelo cliente para criar ou atualizar uma tarefa. */
public record TarefaRequest(

        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String titulo,

        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String descricao,

        Prioridade prioridade,

        @FutureOrPresent(message = "não pode estar no passado")
        LocalDate dataLimite
) {
}
