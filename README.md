# Biblioteca API

API RESTful de gerenciamento de biblioteca desenvolvida com Spring Boot como projeto final do curso de Desenvolvimento de APIs - SENAC.

## Tecnologias

- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- Spring HATEOAS
- Spring Validation
- Springdoc OpenAPI (Swagger)
- Banco de dados H2 (em memória)
- Lombok
- Maven

## Como rodar

Precisa ter o Java 21 instalado.

```bash
./mvnw spring-boot:run
```

A aplicacao sobe na porta 8080.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`, senha vazia)

## Entidades

O sistema possui 6 entidades com os seguintes relacionamentos:

- Livro - entidade central
- Autor (ManyToMany com Livro)
- DetalhesAutor (OneToOne com Autor)
- Editora (OneToMany com Livro)
- Genero (OneToMany com Livro)
- Idioma (OneToMany com Livro)

A entidade Livro possui um enum `StatusLivro` com os valores: DISPONIVEL, EMPRESTADO, RESERVADO.

## Endpoints

Cada entidade possui os seguintes endpoints:

| Metodo | Rota | Status | Descricao |
|--------|------|--------|-----------|
| GET | /{id} | 200 / 404 | Busca por ID |
| GET | /all | 200 | Lista paginada |
| GET | /buscar?nome= | 200 | Busca por nome |
| POST | / | 201 / 400 | Cadastra novo registro |
| PUT | /{id} | 200 / 400 / 404 | Atualiza registro |
| DELETE | /{id} | 204 / 404 | Remove registro |

O Livro possui ainda o endpoint GET `/livro/status?status=DISPONIVEL` para filtrar por status.

## Ordem de cadastro

O livro depende das outras entidades, entao a ordem correta e:

1. Editora
2. Genero
3. Idioma
4. Autor (com detalhesAutor)
5. Livro (referenciando os IDs anteriores)

## HATEOAS

As respostas de GET por ID e POST incluem links para navegacao entre recursos (self, update, delete, all).

## Tratamento de erros

Erros sao tratados globalmente com @ControllerAdvice, retornando JSON com timestamp, status, tipo do erro e mensagem. Erros de validacao incluem detalhes por campo.

- 200: Operacao realizada com sucesso
- 201: Recurso criado com sucesso
- 204: Recurso deletado com sucesso
- 400: Dados invalidos ou erro de validacao
- 404: Recurso nao encontrado
- 500: Erro interno do servidor

## Deploy no Render

### Configuracao

O projeto esta configurado para deploy no Render com Docker:

- `Dockerfile` - Configuracao do container (multi-stage build com Maven + JRE)

### Como fazer deploy

1. Subir o codigo para o GitHub
2. No Render, criar um Web Service conectado ao repositorio
3. Selecionar Environment: Docker
4. Criar o servico

### URLs apos deploy

- Swagger UI: https://biblioteca-api-senac.onrender.com/swagger-ui/index.html
- Endpoints da API: https://biblioteca-api-senac.onrender.com/
