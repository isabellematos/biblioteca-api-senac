package com.example.biblioteca.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.dto.LivroCadastroDTO;
import com.example.biblioteca.domain.dto.LivroDTO;
import com.example.biblioteca.domain.enums.StatusLivro;
import com.example.biblioteca.service.LivroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/livro")
@Tag(name = "Livro")
public class LivroController {

	@Autowired
	private LivroService livroService;

	@Operation(summary = "Encontra livro por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Livro encontrado"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<LivroDTO>> retornarLivroId(
			@Parameter(description = "ID do livro") @PathVariable("id") Integer id) {
		LivroDTO livroDTO = livroService.retornarLivroId(id);
		EntityModel<LivroDTO> resource = EntityModel.of(livroDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarLivroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).atualizarLivroId(id, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarTodosOsLivros(null)).withRel("all"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo livro")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Livro criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@PostMapping
	public ResponseEntity<EntityModel<LivroDTO>> cadastrarLivro(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do livro", required = true,
					content = @Content(schema = @Schema(implementation = LivroCadastroDTO.class)))
			@Valid @RequestBody LivroCadastroDTO l) {
		Livro livro = livroService.cadastrarLivro(l);
		EntityModel<LivroDTO> resource = EntityModel.of(new LivroDTO(livro));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarLivroId(livro.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).atualizarLivroId(livro.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(livro.getId())).withRel("delete"));
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um livro por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Livro atualizado"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@PutMapping("/{id}")
	public ResponseEntity<EntityModel<LivroDTO>> atualizarLivroId(
			@Parameter(description = "ID do livro") @PathVariable("id") Integer id,
			@Valid @RequestBody LivroCadastroDTO l) {
		Livro livro = livroService.atualizarLivro(id, l);
		EntityModel<LivroDTO> resource = EntityModel.of(new LivroDTO(livro));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarLivroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(id)).withRel("delete"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um livro por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Livro deletado"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarLivroId(
			@Parameter(description = "ID do livro") @PathVariable("id") Integer id) {
		livroService.deletarLivroId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os livros")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de livros retornada"),
			@ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/all")
	public ResponseEntity<Page<LivroDTO>> retornarTodosOsLivros(@ParameterObject Pageable pageable) {
		return ResponseEntity.ok(livroService.retornarTodosOsLivros(pageable));
	}

	@Operation(summary = "Busca livros por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Resultado da busca"),
			@ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/buscar")
	public ResponseEntity<Page<LivroDTO>> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(livroService.buscarPorNome(nome, pageable));
	}

	@Operation(summary = "Busca livros por status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Resultado da busca"),
			@ApiResponse(responseCode = "400", description = "Status inválido"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/status")
	public ResponseEntity<Page<LivroDTO>> buscarPorStatus(
			@RequestParam StatusLivro status, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(livroService.buscarPorStatus(status, pageable));
	}
}
