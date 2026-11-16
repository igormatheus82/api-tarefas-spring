package com.igorcavalcante.tarefas.controller;

import com.igorcavalcante.tarefas.dto.AtualizarStatusRequest;
import com.igorcavalcante.tarefas.dto.ResumoResponse;
import com.igorcavalcante.tarefas.dto.TarefaRequest;
import com.igorcavalcante.tarefas.dto.TarefaResponse;
import com.igorcavalcante.tarefas.model.Prioridade;
import com.igorcavalcante.tarefas.model.StatusTarefa;
import com.igorcavalcante.tarefas.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista tarefas com filtros opcionais e paginação")
    public Page<TarefaResponse> listar(
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) Prioridade prioridade,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(status, prioridade, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa pelo id")
    public TarefaResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cria uma nova tarefa")
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest request) {
        TarefaResponse criada = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma tarefa")
    public TarefaResponse atualizar(@PathVariable Long id, @Valid @RequestBody TarefaRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status de uma tarefa")
    public TarefaResponse alterarStatus(@PathVariable Long id,
                                        @Valid @RequestBody AtualizarStatusRequest request) {
        return service.alterarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma tarefa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/atrasadas")
    @Operation(summary = "Lista tarefas não concluídas com prazo vencido")
    public List<TarefaResponse> atrasadas() {
        return service.listarAtrasadas();
    }

    @GetMapping("/resumo")
    @Operation(summary = "Contagem de tarefas por status")
    public ResumoResponse resumo() {
        return service.resumo();
    }
}
