package com.example.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API")
                        .version("1.0")
                        .description("API RESTful para gerenciamento de uma biblioteca. Projeto final do curso de Desenvolvimento de APIs com Spring Boot - SENAC.\n\n"
                        		+ "PARTE 1 - Funcionalidades base:\n"
                        		+ "- Desenvolvida com Spring Boot 4, Java 21 e Maven. Banco de dados H2 em memoria com Spring Data JPA.\n"
                        		+ "- 8 entidades com relacionamentos OneToOne (Autor-DetalhesAutor), OneToMany (Editora-Livro, Genero-Livro, Idioma-Livro) e ManyToMany (Livro-Autor, Usuario-Livro).\n"
                        		+ "- Validacao de dados com Bean Validation em todas as entidades. Enums StatusLivro (DISPONIVEL, EMPRESTADO, RESERVADO) e StatusEmprestimo (ATIVO, DEVOLVIDO, ATRASADO).\n"
                        		+ "- CRUD completo para cada entidade com no minimo 5 endpoints por recurso. Todas as listagens sao paginadas com Pageable.\n"
                        		+ "- Consultas personalizadas por nome em cada entidade, busca por status no livro e busca por usuario no emprestimo.\n"
                        		+ "- HATEOAS implementado com Spring HATEOAS utilizando EntityModel, com links self, update, delete e all nas respostas.\n"
                        		+ "- Codigos de status HTTP corretos: 200, 201, 204, 400, 404, 409, 429, 500.\n"
                        		+ "- Documentacao completa via Springdoc OpenAPI com descricoes, exemplos e codigos de resposta.\n\n"
                        		+ "PARTE 2 - Recursos avancados:\n"
                        		+ "- Idempotencia: operacoes POST aceitam o header X-Idempotency-Key para evitar processamento duplicado.\n"
                        		+ "- Autenticacao com API Key: endpoints POST, PUT e DELETE exigem header X-API-Key. Endpoint /api-keys para gerar e gerenciar chaves.\n"
                        		+ "- Rate Limiting: limite de 10 requisicoes por minuto por IP. Retorna headers X-RateLimit-Limit, X-RateLimit-Remaining e Retry-After. Status 429 quando excedido.\n"
                        		+ "- CORS: configurado para permitir origens localhost:3000 e localhost:8080 com metodos GET, POST, PUT, DELETE e OPTIONS.\n"
                        		+ "- Versionamento: via header X-API-Version no endpoint /livro/versioned. Versao 1 retorna formato completo com HATEOAS, versao 2 retorna formato resumido.\n"
                        		+ "- Tratamento global de erros com @ControllerAdvice retornando JSON estruturado com timestamp, status, tipo e mensagem."))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave de API para autenticacao. Necessaria para POST, PUT e DELETE.")));
    }
}
