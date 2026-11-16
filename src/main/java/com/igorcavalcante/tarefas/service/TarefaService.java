package com.igorcavalcante.tarefas.service;

import com.igorcavalcante.tarefas.dto.ResumoResponse;
import com.igorcavalcante.tarefas.dto.TarefaRequest;
import com.igorcavalcante.tarefas.dto.TarefaResponse;
import com.igorcavalcante.tarefas.exception.RecursoNaoEncontradoException;
import com.igorcavalcante.tarefas.exception.RegraNegocioException;
import com.igorcavalcante.tarefas.model.Prioridade;
import com.igorcavalcante.tarefas.model.StatusTarefa;
import com.igorcavalcante.tarefas.model.Tarefa;
import com.igorcavalcante.tarefas.repository.TarefaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Regras de negócio das tarefas. */
@Service
public class TarefaService {

    private final TarefaRepository repository;
    private final Clock clock;

    public TarefaService(TarefaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<TarefaResponse> listar(StatusTarefa status, Prioridade prioridade, Pageable pageable) {
        return repository.buscarComFiltros(status, prioridade, pageable).map(TarefaResponse::de);
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        return TarefaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TarefaResponse criar(TarefaRequest request) {
        Tarefa tarefa = new Tarefa();
        preencher(tarefa, request);
        return TarefaResponse.de(repository.save(tarefa));
    }

    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa tarefa = buscarEntidade(id);
        if (tarefa.getStatus() == StatusTarefa.CONCLUIDA) {
            throw new RegraNegocioException("Tarefas concluídas não podem ser editadas. Reabra a tarefa antes.");
        }
        preencher(tarefa, request);
        return TarefaResponse.de(repository.save(tarefa));
    }

    @Transactional
    public TarefaResponse alterarStatus(Long id, StatusTarefa novoStatus) {
        Tarefa tarefa = buscarEntidade(id);
        if (tarefa.getStatus() == novoStatus) {
            throw new RegraNegocioException("A tarefa já está com o status " + novoStatus + ".");
        }
        tarefa.alterarStatus(novoStatus);
        return TarefaResponse.de(repository.save(tarefa));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listarAtrasadas() {
        return repository.buscarAtrasadas(LocalDate.now(clock)).stream()
                .map(TarefaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumo() {
        return new ResumoResponse(
                repository.count(),
                repository.countByStatus(StatusTarefa.PENDENTE),
                repository.countByStatus(StatusTarefa.EM_ANDAMENTO),
                repository.countByStatus(StatusTarefa.CONCLUIDA),
                repository.buscarAtrasadas(LocalDate.now(clock)).size()
        );
    }

    private Tarefa buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa " + id + " não encontrada."));
    }

    private void preencher(Tarefa tarefa, TarefaRequest request) {
        tarefa.setTitulo(request.titulo().trim());
        tarefa.setDescricao(request.descricao());
        tarefa.setPrioridade(request.prioridade() != null ? request.prioridade() : Prioridade.MEDIA);
        tarefa.setDataLimite(request.dataLimite());
    }
}
