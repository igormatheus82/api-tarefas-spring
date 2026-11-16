package com.igorcavalcante.tarefas.controller;

import com.igorcavalcante.tarefas.model.StatusTarefa;
import com.igorcavalcante.tarefas.model.Tarefa;
import com.igorcavalcante.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração: sobem a aplicação inteira com banco H2 em memória
 * e fazem requisições HTTP simuladas. Cada teste é revertido ao final (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TarefaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TarefaRepository repository;

    private Tarefa salvar(String titulo, StatusTarefa status, LocalDate dataLimite) {
        Tarefa t = new Tarefa();
        t.setTitulo(titulo);
        t.setDataLimite(dataLimite);
        t.alterarStatus(status);
        return repository.save(t);
    }

    @Test
    @DisplayName("POST cria tarefa e retorna 201 com Location")
    void criar() throws Exception {
        String json = """
                {"titulo": "Escrever casos de teste", "prioridade": "ALTA", "dataLimite": "%s"}
                """.formatted(LocalDate.now().plusDays(3));

        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/tarefas/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Escrever casos de teste"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.atrasada").value(false));
    }

    @Test
    @DisplayName("POST sem título retorna 400 com o campo inválido")
    void criarSemTitulo() throws Exception {
        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content("{\"titulo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").value("é obrigatório"));
    }

    @Test
    @DisplayName("POST com data limite no passado retorna 400")
    void criarComDataPassada() throws Exception {
        String json = "{\"titulo\": \"Tarefa\", \"dataLimite\": \"2020-01-01\"}";
        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.dataLimite").exists());
    }

    @Test
    @DisplayName("POST com prioridade inexistente retorna 400")
    void criarComPrioridadeInvalida() throws Exception {
        String json = "{\"titulo\": \"Tarefa\", \"prioridade\": \"URGENTISSIMA\"}";
        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET por id inexistente retorna 404")
    void buscarInexistente() throws Exception {
        mvc.perform(get("/api/tarefas/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Tarefa 999999 não encontrada."));
    }

    @Test
    @DisplayName("GET lista com filtro por status")
    void listarComFiltro() throws Exception {
        salvar("A", StatusTarefa.PENDENTE, null);
        salvar("B", StatusTarefa.CONCLUIDA, null);

        mvc.perform(get("/api/tarefas").param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo").value("B"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("GET com status inválido no filtro retorna 400")
    void filtroInvalido() throws Exception {
        mvc.perform(get("/api/tarefas").param("status", "XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT atualiza tarefa")
    void atualizar() throws Exception {
        Tarefa t = salvar("Antigo", StatusTarefa.PENDENTE, null);

        mvc.perform(put("/api/tarefas/{id}", t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Novo título\", \"prioridade\": \"BAIXA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo título"))
                .andExpect(jsonPath("$.prioridade").value("BAIXA"));
    }

    @Test
    @DisplayName("PUT em tarefa concluída retorna 422")
    void atualizarConcluida() throws Exception {
        Tarefa t = salvar("Feita", StatusTarefa.CONCLUIDA, null);

        mvc.perform(put("/api/tarefas/{id}", t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Mudar\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PATCH /status conclui a tarefa")
    void concluir() throws Exception {
        Tarefa t = salvar("Fazer deploy", StatusTarefa.EM_ANDAMENTO, null);

        mvc.perform(patch("/api/tarefas/{id}/status", t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.concluidaEm").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE remove a tarefa")
    void excluir() throws Exception {
        Tarefa t = salvar("Apagar", StatusTarefa.PENDENTE, null);

        mvc.perform(delete("/api/tarefas/{id}", t.getId())).andExpect(status().isNoContent());
        mvc.perform(get("/api/tarefas/{id}", t.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /atrasadas retorna apenas não concluídas com prazo vencido")
    void atrasadas() throws Exception {
        LocalDate ontem = LocalDate.now().minusDays(1);
        salvar("Atrasada", StatusTarefa.PENDENTE, ontem);
        salvar("Concluída no prazo", StatusTarefa.CONCLUIDA, ontem);
        salvar("Futura", StatusTarefa.PENDENTE, LocalDate.now().plusDays(5));

        mvc.perform(get("/api/tarefas/atrasadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Atrasada"))
                .andExpect(jsonPath("$[0].atrasada").value(true));
    }

    @Test
    @DisplayName("GET /resumo conta tarefas por status")
    void resumo() throws Exception {
        salvar("1", StatusTarefa.PENDENTE, null);
        salvar("2", StatusTarefa.EM_ANDAMENTO, null);
        salvar("3", StatusTarefa.CONCLUIDA, null);

        mvc.perform(get("/api/tarefas/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pendentes").value(1))
                .andExpect(jsonPath("$.emAndamento").value(1))
                .andExpect(jsonPath("$.concluidas").value(1));
    }
}
