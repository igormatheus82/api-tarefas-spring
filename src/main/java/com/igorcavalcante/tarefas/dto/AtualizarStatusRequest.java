package com.igorcavalcante.tarefas.dto;

import com.igorcavalcante.tarefas.model.StatusTarefa;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(
        @NotNull(message = "é obrigatório")
        StatusTarefa status
) {
}
