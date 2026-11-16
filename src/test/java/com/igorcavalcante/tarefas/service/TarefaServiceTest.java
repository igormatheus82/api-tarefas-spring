package com.igorcavalcante.tarefas.service;

import com.igorcavalcante.tarefas.dto.TarefaRequest;
import com.igorcavalcante.tarefas.dto.TarefaResponse;
import com.igorcavalcante.tarefas.exception.RecursoNaoEncontradoException;
import com.igorcavalcante.tarefas.exception.RegraNegocioException;
import com.igorcavalcante.tarefas.model.Prioridade;
import com.igorcavalcante.tarefas.model.StatusTarefa;
import com.igorcavalcante.tarefas.model.Tarefa;
import com.igorcavalcante.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitários: o repositório é simulado com Mockito (sem banco de dados). */
@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 16);

    @Mock
    private TarefaRepository repository;

    private TarefaService service;

    @BeforeEach
    void setUp() {
        Clock relogioFixo = Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneId.of("UTC"));
        service = new TarefaService(repository, relogioFixo);
    }

    private Tarefa tarefa(Long id, StatusTarefa status) {
        Tarefa t = new Tarefa();
        t.setId(id);
        t.setTitulo("Estudar Spring");
        t.alterarStatus(status);
        return t;
    }

    @Test
    @DisplayName("Ao criar sem prioridade, usa MEDIA e remove espaços do título")
    void criarComValoresPadrao() {
        when(repository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponse resposta = service.criar(new TarefaRequest("  Revisar PR  ", null, null, null));

        assertThat(resposta.titulo()).isEqualTo("Revisar PR");
        assertThat(resposta.prioridade()).isEqualTo(Prioridade.MEDIA);
        assertThat(resposta.status()).isEqualTo(StatusTarefa.PENDENTE);
    }

    @Test
    @DisplayName("Buscar id inexistente lança RecursoNaoEncontradoException")
    void buscarInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Não permite editar tarefa concluída")
    void naoEditaConcluida() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa(1L, StatusTarefa.CONCLUIDA)));

        assertThatThrownBy(() -> service.atualizar(1L, new TarefaRequest("Novo", null, null, null)))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Concluir tarefa preenche a data de conclusão")
    void concluirPreencheData() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa(1L, StatusTarefa.EM_ANDAMENTO)));
        when(repository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponse resposta = service.alterarStatus(1L, StatusTarefa.CONCLUIDA);

        assertThat(resposta.status()).isEqualTo(StatusTarefa.CONCLUIDA);
        assertThat(resposta.concluidaEm()).isNotNull();
    }

    @Test
    @DisplayName("Reabrir tarefa concluída limpa a data de conclusão")
    void reabrirLimpaData() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa(1L, StatusTarefa.CONCLUIDA)));
        when(repository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponse resposta = service.alterarStatus(1L, StatusTarefa.PENDENTE);

        assertThat(resposta.concluidaEm()).isNull();
    }

    @Test
    @DisplayName("Alterar para o mesmo status é uma regra de negócio violada")
    void mesmoStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa(1L, StatusTarefa.PENDENTE)));

        assertThatThrownBy(() -> service.alterarStatus(1L, StatusTarefa.PENDENTE))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Atrasadas usa a data de hoje do relógio injetado")
    void atrasadasUsaRelogio() {
        when(repository.buscarAtrasadas(HOJE)).thenReturn(List.of(tarefa(1L, StatusTarefa.PENDENTE)));

        assertThat(service.listarAtrasadas()).hasSize(1);

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(repository).buscarAtrasadas(captor.capture());
        assertThat(captor.getValue()).isEqualTo(HOJE);
    }

    @Test
    @DisplayName("Excluir tarefa inexistente não chama delete")
    void excluirInexistente() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(5L)).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).delete(any());
    }
}
