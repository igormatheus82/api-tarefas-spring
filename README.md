# API de Gerenciamento de Tarefas — Java + Spring Boot

![CI](https://github.com/igormatheus82/api-tarefas-spring/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F)
![JUnit](https://img.shields.io/badge/Testes-JUnit%205%20%2B%20Mockito-25A162)

API REST para gerenciar tarefas, construída com **Spring Boot**, **Spring Data JPA** e **Bean Validation**, com testes unitários e de integração. Projeto de estudo focado em boas práticas de back-end Java e em **qualidade de software**.

## Funcionalidades

- CRUD completo de tarefas (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`)
- Listagem **paginada** com filtros por status e prioridade
- Fluxo de status: `PENDENTE` → `EM_ANDAMENTO` → `CONCLUIDA` (com reabertura)
- Regras de negócio: tarefa concluída não pode ser editada; data limite não pode estar no passado
- Endpoint de **tarefas atrasadas** (JPQL) e **resumo** por status
- Tratamento global de erros com respostas JSON padronizadas (`400`, `404`, `422`)
- Documentação interativa com **Swagger UI**
- Banco **H2** em memória para desenvolvimento e **PostgreSQL** via Docker
- **21 testes** (unitários com Mockito + integração com MockMvc) rodando no **GitHub Actions**

## Arquitetura

```
controller  →  service  →  repository  →  banco de dados
   (HTTP)      (regras)     (Spring Data JPA)
```

```
src/main/java/com/igorcavalcante/tarefas/
├── controller/   # endpoints REST
├── service/      # regras de negócio
├── repository/   # acesso a dados (JPA + consultas JPQL)
├── model/        # entidade Tarefa e enums
├── dto/          # objetos de entrada/saída (records)
└── exception/    # exceções e handler global
```

## Como rodar

Pré-requisitos: **Java 21** e **Maven 3.9+** (ou abra o projeto no IntelliJ IDEA, que já traz o Maven).

```bash
git clone https://github.com/igormatheus82/api-tarefas-spring.git
cd api-tarefas-spring
mvn spring-boot:run
```

- API: http://localhost:8080/api/tarefas
- Swagger UI: http://localhost:8080/swagger-ui.html
- Console do H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:tarefas`, usuário `sa`)

### Com PostgreSQL (Docker)

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Testes

```bash
mvn test
```

| Tipo | Classe | O que cobre |
|---|---|---|
| Unitário | `TarefaServiceTest` | Regras de negócio isoladas, com repositório simulado (Mockito) e relógio fixo |
| Integração | `TarefaControllerTest` | Requisições HTTP reais contra a aplicação completa e banco H2 |

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/tarefas?status=&prioridade=&page=&size=` | Lista paginada com filtros |
| GET | `/api/tarefas/{id}` | Busca por id |
| POST | `/api/tarefas` | Cria tarefa |
| PUT | `/api/tarefas/{id}` | Atualiza tarefa |
| PATCH | `/api/tarefas/{id}/status` | Altera status |
| DELETE | `/api/tarefas/{id}` | Exclui tarefa |
| GET | `/api/tarefas/atrasadas` | Tarefas com prazo vencido |
| GET | `/api/tarefas/resumo` | Contagem por status |

### Exemplo

```http
POST /api/tarefas
Content-Type: application/json

{ "titulo": "Escrever casos de teste do login", "prioridade": "ALTA", "dataLimite": "2030-12-31" }
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "titulo": "Escrever casos de teste do login",
  "descricao": null,
  "status": "PENDENTE",
  "prioridade": "ALTA",
  "dataLimite": "2030-12-31",
  "atrasada": false,
  "criadoEm": "2026-11-16T15:00:00",
  "atualizadoEm": "2026-11-16T15:00:00",
  "concluidaEm": null
}
```

Erro de validação `400`:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "campos": { "titulo": "é obrigatório" },
  "timestamp": "2026-11-16T15:00:00"
}
```

## Próximos passos

- [ ] Autenticação com Spring Security + JWT
- [ ] Migrações de banco com Flyway
- [ ] Testes de API com REST Assured

---

Feito por **Igor Cavalcante** · [LinkedIn](https://www.linkedin.com/in/cavalcanteigor)
