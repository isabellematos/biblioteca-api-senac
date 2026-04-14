package com.example.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API")
                        .version("1.0")
                        .description("API RESTful para gerenciamento de uma biblioteca. "
                        		+ "Permite o cadastro e consulta de livros, autores, editoras, generos e idiomas. "
                        		+ "Cada livro possui relacionamento com autor, editora, genero e idioma, "
                        		+ "alem de um status que indica sua disponibilidade (DISPONIVEL, EMPRESTADO ou RESERVADO). "
                        		+ "Todas as listagens sao paginadas e os recursos utilizam HATEOAS para navegacao entre endpoints. "
                        		+ "Projeto final do curso de Desenvolvimento de APIs com Spring Boot - SENAC."));
    }
}
