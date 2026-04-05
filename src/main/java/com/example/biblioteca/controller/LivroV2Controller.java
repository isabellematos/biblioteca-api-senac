package com.example.biblioteca.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.biblioteca.domain.dto.LivroDTO;
import com.example.biblioteca.service.LivroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v2/livro")
@Tag(name = "Livro V2", description = "Versão 2 dos endpoints de livro - inclui status na resposta")
public class LivroV2Controller {

	@Autowired
	private LivroService livroService;

	@Operation(summary = "Encontra livro por ID (V2 - com status)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Livro encontrado"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<LivroDTO>> retornarLivroId(
			@Parameter(description = "ID do livro")
			@PathVariable("id") Integer id) {
		LivroDTO livroDTO = livroService.retornarLivroId(id);

		EntityModel<LivroDTO> resource = EntityModel.of(livroDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroV2Controller.class).retornarLivroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroV2Controller.class).retornarTodosOsLivros(null)).withRel("all"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Encontra todos os livros (V2 - com status)")
	@GetMapping("/all")
	public Page<LivroDTO> retornarTodosOsLivros(@ParameterObject Pageable pageable) {
		return livroService.retornarTodosOsLivros(pageable);
	}
}
