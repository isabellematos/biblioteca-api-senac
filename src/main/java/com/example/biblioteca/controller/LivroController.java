package com.example.biblioteca.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.dto.LivroCadastroDTO;
import com.example.biblioteca.domain.dto.LivroDTO;
import com.example.biblioteca.domain.enums.StatusLivro;
import com.example.biblioteca.service.IdempotencyService;
import com.example.biblioteca.service.LivroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/livro")
public class LivroController {

	@Autowired
	private LivroService livroService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra livro por ID")
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
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarLivroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).atualizarLivroId(id, null, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarTodosOsLivro(null)).withRel("all"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo livro")
	@PostMapping
	public ResponseEntity<EntityModel<LivroDTO>> cadastrarLivro(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do livro a serem enviados",
					required = true,
					content = @Content(schema = @Schema(implementation = LivroCadastroDTO.class)))
			@Valid @RequestBody LivroCadastroDTO l,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Autowired UriComponentsBuilder ucb) {

		if (idempotencyKey != null) {
			Object cached = idempotencyService.getResponse(idempotencyKey);
			if (cached != null) {
				@SuppressWarnings("unchecked")
				EntityModel<LivroDTO> cachedResource = (EntityModel<LivroDTO>) cached;
				return ResponseEntity.ok(cachedResource);
			}
		}

		Livro livro = livroService.cadastrarLivro(l);

		EntityModel<LivroDTO> resource = EntityModel.of(new LivroDTO(livro));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarLivroId(livro.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).atualizarLivroId(livro.getId(), null, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(livro.getId())).withRel("delete"));

		if (idempotencyKey != null) {
			idempotencyService.saveResponse(idempotencyKey, resource);
		}

		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um livro por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Livro atualizado"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	})
	@PutMapping("/{id}")
	public ResponseEntity<EntityModel<LivroDTO>> atualizarLivroId(
			@Parameter(description = "ID do livro")
			@PathVariable("id") Integer id,
			@Valid @RequestBody LivroCadastroDTO l,
			@Autowired UriComponentsBuilder ucb) {
		Livro livro = livroService.atualizarLivro(id, l);

		EntityModel<LivroDTO> resource = EntityModel.of(new LivroDTO(livro));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).retornarLivroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(id)).withRel("delete"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um livro por ID")
	@DeleteMapping("/{id}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Livro deletado"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	})
	public ResponseEntity<Void> deletarLivroId(
			@Parameter(description = "ID do livro")
			@PathVariable("id") Integer id) {
		livroService.deletarLivroId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os livros")
	@GetMapping("/all")
	public Page<LivroDTO> retornarTodosOsLivro(@ParameterObject Pageable pageable) {
		return livroService.retornarTodosOsLivros(pageable);
	}

	@Operation(summary = "Busca livros por nome")
	@GetMapping("/buscar")
	public Page<LivroDTO> buscarPorNome(
			@RequestParam String nome,
			@ParameterObject Pageable pageable) {
		return livroService.buscarPorNome(nome, pageable);
	}

	@Operation(summary = "Busca livros por status")
	@GetMapping("/status")
	public Page<LivroDTO> buscarPorStatus(
			@RequestParam StatusLivro status,
			@ParameterObject Pageable pageable) {
		return livroService.buscarPorStatus(status, pageable);
	}
}
