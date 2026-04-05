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

A aplicação sobe na porta 8080.

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

Todos os endpoints seguem o padrão `/api/v1/{entidade}`. Cada entidade possui:

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | /{id} | Busca por ID |
| GET | /all | Lista paginada |
| GET | /buscar?nome= | Busca por nome |
| POST | / | Cadastra novo registro |
| PUT | /{id} | Atualiza registro |
| DELETE | /{id} | Remove registro (requer API Key) |

O Livro possui ainda o endpoint GET `/api/v1/livro/status?status=DISPONIVEL` para filtrar por status.

Existe tambem uma versao v2 do endpoint de livros em `/api/v2/livro`.

## Ordem de cadastro

O livro depende das outras entidades, entao a ordem correta e:

1. Editora
2. Genero
3. Idioma
4. Autor (com detalhesAutor)
5. Livro (referenciando os IDs anteriores)

## Autenticacao com API Key

Apenas os endpoints DELETE sao protegidos. Para usar:

1. Gere uma chave via POST `/api/v1/api-keys` com body `{"descricao": "minha chave"}`
2. Passe o header `X-API-Key: <chave>` nas requisicoes DELETE

O endpoint de geracao de chaves e publico.

## Idempotencia

Os endpoints POST aceitam o header `Idempotency-Key`. Se a mesma chave for enviada duas vezes, a segunda requisicao retorna a resposta cacheada sem duplicar o registro.

## Rate Limiting

Limite de 60 requisicoes por minuto por IP. Os headers `X-RateLimit-Limit` e `X-RateLimit-Remaining` sao retornados em toda resposta. Ao exceder o limite, retorna status 429.

## CORS

Configurado para aceitar requisicoes de `localhost:3000` e `localhost:8080` com os metodos GET, POST, PUT, DELETE e OPTIONS.

## HATEOAS

As respostas de GET por ID e POST incluem links para navegacao entre recursos (self, update, delete, all).

## Tratamento de erros

Erros sao tratados globalmente com @ControllerAdvice, retornando JSON com timestamp, status, tipo do erro e mensagem. Erros de validacao incluem detalhes por campo.

## Deploy no Render

### Configuracao

O projeto esta configurado para deploy no Render com Docker:

- `Dockerfile` - Configuracao do container (multi-stage build com Maven + JRE)
- `application.properties` - Usa a variavel `PORT` do Render automaticamente

### Como fazer deploy

1. Subir o codigo para o GitHub:

```bash
git add .
git commit -m "Deploy no Render"
git push origin main
```

2. Configurar no Render (Plano Gratuito):

- Acesse https://render.com
- Faca login com sua conta GitHub
- Clique em "New +" > "Web Service"
- Conecte o repositorio `isabellematos/biblioteca-api-senac`
- Configure:
  - Name: `biblioteca-api-senac`
  - Environment: Docker
  - Dockerfile Path: `./Dockerfile`
  - Build Command: (deixar vazio)
  - Start Command: (deixar vazio)
- Clique em "Create Web Service"

### URLs apos deploy

- Swagger UI: https://biblioteca-api-senac.onrender.com/swagger-ui/index.html
- Endpoints da API: https://biblioteca-api-senac.onrender.com/api/v1/
