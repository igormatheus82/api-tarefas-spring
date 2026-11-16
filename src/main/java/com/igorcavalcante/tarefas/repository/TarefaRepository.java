package com.igorcavalcante.tarefas.repository;

import com.igorcavalcante.tarefas.model.Prioridade;
import com.igorcavalcante.tarefas.model.StatusTarefa;
import com.igorcavalcante.tarefas.model.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    /** Filtros opcionais: parâmetro nulo significa "não filtrar". */
    @Query("""
            SELECT t FROM Tarefa t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:prioridade IS NULL OR t.prioridade = :prioridade)
            """)
    Page<Tarefa> buscarComFiltros(@Param("status") StatusTarefa status,
                                  @Param("prioridade") Prioridade prioridade,
                                  Pageable pageable);

    /** Tarefas não concluídas com data limite já vencida. */
    @Query("""
            SELECT t FROM Tarefa t
            WHERE t.status <> com.igorcavalcante.tarefas.model.StatusTarefa.CONCLUIDA
              AND t.dataLimite < :hoje
            ORDER BY t.dataLimite
            """)
    List<Tarefa> buscarAtrasadas(@Param("hoje") LocalDate hoje);

    long countByStatus(StatusTarefa status);
}
