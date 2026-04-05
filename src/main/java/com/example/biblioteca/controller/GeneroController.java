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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.biblioteca.domain.Genero;
import com.example.biblioteca.domain.dto.GeneroDTO;
import com.example.biblioteca.service.GeneroService;
import com.example.biblioteca.service.IdempotencyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/genero")
public class GeneroController {

	@Autowired
	private GeneroService generoService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra gênero por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Gênero encontrado"),
			@ApiResponse(responseCode = "404", description = "Gênero não encontrado")
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<GeneroDTO>> retornarGeneroId(
			@Parameter(description = "ID do gênero")
			@PathVariable("id") Integer id) {
		GeneroDTO generoDTO = new GeneroDTO(generoService.retornargeneroId(id));

		EntityModel<GeneroDTO> resource = EntityModel.of(generoDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarGeneroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).atualizarGeneroId(id, null, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).deletarGeneroId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarTodosOsGeneros(null)).withRel("all"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo gênero")
	@PostMapping
	public ResponseEntity<EntityModel<GeneroDTO>> cadastrarGenero(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do gênero a serem enviados",
					required = true,
					content = @Content(schema = @Schema(implementation = GeneroDTO.class)))
			@Valid @RequestBody GeneroDTO g,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Autowired UriComponentsBuilder ucb) {

		if (idempotencyKey != null) {
			Object cached = idempotencyService.getResponse(idempotencyKey);
			if (cached != null) {
				@SuppressWarnings("unchecked")
				EntityModel<GeneroDTO> cachedResource = (EntityModel<GeneroDTO>) cached;
				return ResponseEntity.ok(cachedResource);
			}
		}

		Genero genero = generoService.cadastrarGenero(g);

		EntityModel<GeneroDTO> resource = EntityModel.of(new GeneroDTO(genero));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarGeneroId(genero.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).atualizarGeneroId(genero.getId(), null, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).deletarGeneroId(genero.getId())).withRel("delete"));

		if (idempotencyKey != null) {
			idempotencyService.saveResponse(idempotencyKey, resource);
		}

		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um gênero por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Gênero atualizado"),
			@ApiResponse(responseCode = "404", description = "Gênero não encontrado")
	})
	@PutMapping("/{id}")
	public ResponseEntity<EntityModel<GeneroDTO>> atualizarGeneroId(
			@Parameter(description = "ID do gênero")
			@PathVariable("id") Integer id,
			@Valid @RequestBody GeneroDTO g,
			@Autowired UriComponentsBuilder ucb) {
		Genero genero = generoService.atualizarGenero(id, g);

		EntityModel<GeneroDTO> resource = EntityModel.of(new GeneroDTO(genero));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarGeneroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).deletarGeneroId(id)).withRel("delete"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um gênero por ID")
	@DeleteMapping("/{id}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Gênero deletado"),
			@ApiResponse(responseCode = "404", description = "Gênero não encontrado")
	})
	public ResponseEntity<Void> deletarGeneroId(
			@Parameter(description = "ID do gênero")
			@PathVariable("id") Integer id) {
		generoService.deletarGeneroId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os gêneros")
	@GetMapping("/all")
	public Page<GeneroDTO> retornarTodosOsGeneros(@ParameterObject Pageable pageable) {
		return generoService.retornarTodosOsGeneros(pageable);
	}

	@Operation(summary = "Busca gêneros por nome")
	@GetMapping("/buscar")
	public Page<GeneroDTO> buscarPorNome(
			@RequestParam String nome,
			@ParameterObject Pageable pageable) {
		return generoService.buscarPorNome(nome, pageable);
	}
}
