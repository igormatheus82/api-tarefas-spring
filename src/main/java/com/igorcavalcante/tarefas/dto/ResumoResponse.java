package com.igorcavalcante.tarefas.dto;

public record ResumoResponse(
        long total,
        long pendentes,
        long emAndamento,
        long concluidas,
        long atrasadas
) {
}
