# Marketplace Desafio API

API multicamada desenvolvida com **Java 25**, **Spring Boot**, **PostgreSQL**, **BCrypt**, **JWT** e **OpenAPI / Swagger**.

---

## Integrantes do Grupo e Divisão de Papéis

1. **Integrante 1 - Domínio e Banco**: Entities, invariantes e alinhamento com scripts SQL.
2. **Integrante 2 - Usuário e Autenticação**: Cadastro, criptografia BCrypt, login e emissão de JWT.
3. **Integrante 3 - Catálogo**: Administração de produtos, preço, controle de estoque e endpoints.
4. **Integrante 4 - Carrinho**: Criação de carrinhos, alteração de quantidade, cálculo de totais e validações.
5. **Integrante 5 - Pagamento e Qualidade**: Confirmação transacional de pagamentos, exceções globais, Swagger e testes.

---

## Decisões de Negócio

> [!IMPORTANT]
> **Momento da Baixa de Estoque**:
> A baixa física do estoque do produto ocorre **unicamente no momento da aprovação do pagamento** em uma operação transacional atômica (`@Transactional`). Caso o saldo seja insuficiente na aprovação ou ocorra qualquer erro na transação, a operação faz rollback completo e nenhuma alteração parcial permanece no banco de dados.

> [!NOTE]
> **Segurança de Senha e Hash**:
> A entidade `Usuario` armazena somente o hash codificado via BCrypt `PasswordEncoder`. A senha — assim como seu hash — nunca é exposta em `UsuarioResponse`, logs de aplicação ou mensagens de erro.

---

## Banco de Dados Local (PostgreSQL)

1. Conecte-se ao banco `postgres` e execute o script `sql/01_criar_database.sql` (fora de transação).
2. Conecte-se ao banco `marketplace_desafio`.
3. Execute o DDL de tabelas e índices em `sql/02_criar_tabelas.sql`.
4. Preencha a carga inicial de dados com `sql/03_carga_dados.sql`.
5. Valide os relacionamentos e estados com `sql/04_consultas_evidencias.sql`.

### Configuração de Variáveis de Ambiente (opcional)

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/marketplace_desafio}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:sua_senha}
```

---

## Como Executar a Aplicação

```bash
mvn spring-boot:run
```

- **Swagger UI**: `http://localhost:8082/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8082/v3/api-docs`

---

## Executar os Testes Automatizados

```bash
mvn test
```

A suíte inclui:
- `CatalogoProdutoTest`: Teste unitário de proteção de estoque (saldo inalterado em baixa inválida).
- `CarrinhoTest`: Teste unitário de cálculo de total e proteção contra alteração em carrinho finalizado.
- `CarrinhoControllerIntegrationTest`: Teste de integração WebMvc com `MockMvc` para `POST /api/carrinhos` (respostas HTTP 201 + Header Location e 400 em payload inválido).

---

## Matriz de Rotas HTTP

| Método | Rota | Acesso | Descrição / Retorno Esperado |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/usuarios` | Público | `201 Created` + Header `Location` |
| `POST` | `/api/auth/login` | Público | `200 OK` (devolve TokenResponse JWT) |
| `GET` | `/api/produtos` | Público | `200 OK` (lista catálogo) |
| `GET` | `/api/produtos/{id}` | Público | `200 OK` ou `404 Not Found` |
| `GET` | `/api/usuarios/{id}` | Protegido | `200 OK` ou `404 Not Found` |
| `POST` | `/api/produtos` | Protegido | `201 Created` + Header `Location` |
| `PUT` | `/api/produtos/{id}` | Protegido | `200 OK` (atualiza dados válidos) |
| `PATCH` | `/api/produtos/{id}/estoque` | Protegido | `200 OK` (reposição de estoque) |
| `POST` | `/api/carrinhos` | Protegido | `201 Created` + Header `Location` |
| `GET` | `/api/carrinhos/{id}` | Protegido | `200 OK` ou `404 Not Found` |
| `GET` | `/api/carrinhos/usuario/{usuarioId}` | Protegido | `200 OK` (carrinhos do usuário) |
| `PATCH` | `/api/carrinhos/{id}/quantidade` | Protegido | `200 OK`, `400 Bad Request` ou `409 Conflict` |
| `DELETE` | `/api/carrinhos/{id}` | Protegido | `204 No Content` (cancela carrinho aberto) |
| `POST` | `/api/pagamentos` | Protegido | `201 Created` + Header `Location` |
| `GET` | `/api/pagamentos/{id}` | Protegido | `200 OK` ou `404 Not Found` |
| `PATCH` | `/api/pagamentos/{id}/aprovar` | Protegido | `200 OK` (operação transacional) |
| `PATCH` | `/api/pagamentos/{id}/recusar` | Protegido | `200 OK` (recusa pagamento) |

---

## Definição de Pronto (Checklist)

- [x] Projeto compila e `mvn test` executa com sucesso (`BUILD SUCCESS`).
- [x] Entidades implementam comportamentos sem setters públicos genéricos.
- [x] Repositórios com consultas derivadas Spring Data JPA.
- [x] Services tratam validações, orquestração e limites de transação (`@Transactional`).
- [x] Controllers usam DTOs com Bean Validation, status HTTP coerentes, Header Location e OpenAPI annotations.
- [x] Senha criptografada com BCrypt e sem vazamento em respostas.
- [x] Token JWT gerado no login e utilizado para autenticar rotas protegidas no Swagger (`bearerAuth`).
- [x] Tratamento global de exceções retornando formato padronizado JSON (`ApiErrorResponse`) para 400, 401, 404, 409 e 500.
- [x] Aprovação de pagamento realiza baixa de estoque e finalização do carrinho atomicamente.
- [x] Testes unitários e de integração passando.

