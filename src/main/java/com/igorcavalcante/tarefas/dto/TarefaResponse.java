package com.igorcavalcante.tarefas.dto;

import com.igorcavalcante.tarefas.model.Prioridade;
import com.igorcavalcante.tarefas.model.StatusTarefa;
import com.igorcavalcante.tarefas.model.Tarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Representação da tarefa devolvida pela API (não expõe a entidade JPA). */
public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        Prioridade prioridade,
        LocalDate dataLimite,
        boolean atrasada,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime concluidaEm
) {
    public static TarefaResponse de(Tarefa t) {
        return new TarefaResponse(
                t.getId(),
                t.getTitulo(),
                t.getDescricao(),
                t.getStatus(),
                t.getPrioridade(),
                t.getDataLimite(),
                t.estaAtrasada(),
                t.getCriadoEm(),
                t.getAtualizadoEm(),
                t.getConcluidaEm()
        );
    }
}
