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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/genero")
@Tag(name = "Genero")
public class GeneroController {

	@Autowired
	private GeneroService generoService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra gênero por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O genero literario foi encontrado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar o genero e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhum genero literario foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de generos em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<GeneroDTO>> retornarGeneroId(
			@Parameter(description = "ID do gênero") @PathVariable("id") Integer id) {
		GeneroDTO generoDTO = new GeneroDTO(generoService.retornargeneroId(id));
		EntityModel<GeneroDTO> resource = EntityModel.of(generoDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarGeneroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).atualizarGeneroId(id, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).deletarGeneroId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarTodosOsGeneros(null)).withRel("all"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo gênero")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Novo genero literario cadastrado com sucesso no sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para cadastrar o genero sao invalidos. Verifique o nome do genero."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para cadastrar generos."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe um genero com esse nome no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de cadastro de generos em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<GeneroDTO>> cadastrarGenero(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do gênero", required = true,
					content = @Content(schema = @Schema(implementation = GeneroDTO.class)))
			@Valid @RequestBody GeneroDTO g,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			throw new IllegalArgumentException("O header Idempotency-Key e obrigatorio e nao pode ser vazio.");
		}
		String payloadHash = String.valueOf(g.hashCode());
		Object cached = idempotencyService.getResponse(idempotencyKey, payloadHash);
		if (cached != null) {
			@SuppressWarnings("unchecked")
			EntityModel<GeneroDTO> cachedResource = (EntityModel<GeneroDTO>) cached;
			return ResponseEntity.ok(cachedResource);
		}

		Genero genero = generoService.cadastrarGenero(g);
		EntityModel<GeneroDTO> resource = EntityModel.of(new GeneroDTO(genero));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarGeneroId(genero.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).atualizarGeneroId(genero.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).deletarGeneroId(genero.getId())).withRel("delete"));
		idempotencyService.saveResponse(idempotencyKey, payloadHash, resource);
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um gênero por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O genero literario foi atualizado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para atualizar o genero sao invalidos. Verifique o nome do genero."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para atualizar generos."),
			@ApiResponse(responseCode = "404", description = "Nenhum genero literario foi encontrado com esse ID para atualizacao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe outro genero com esse nome no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de atualizacao de generos em sequencia. Aguarde antes de tentar novamente."),
	})
	@PutMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<GeneroDTO>> atualizarGeneroId(
			@Parameter(description = "ID do gênero") @PathVariable("id") Integer id,
			@Valid @RequestBody GeneroDTO g) {
		Genero genero = generoService.atualizarGenero(id, g);
		EntityModel<GeneroDTO> resource = EntityModel.of(new GeneroDTO(genero));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).retornarGeneroId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GeneroController.class).deletarGeneroId(id)).withRel("delete"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um gênero por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "O genero literario foi removido com sucesso do cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover o genero e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover generos."),
			@ApiResponse(responseCode = "404", description = "Nenhum genero literario foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover o genero. Existem livros vinculados a este genero."),
			@ApiResponse(responseCode = "422", description = "O genero possui livros vinculados. Remova os livros antes de excluir o genero."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes de generos em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarGeneroId(
			@Parameter(description = "ID do gênero") @PathVariable("id") Integer id) {
		generoService.deletarGeneroId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os gêneros")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de generos literarios cadastrados foi retornada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de generos em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<?> retornarTodosOsGeneros(@ParameterObject Pageable pageable) {
		Page<GeneroDTO> result = generoService.retornarTodosOsGeneros(pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Busca gêneros por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por generos literarios foi realizada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "O parametro de busca informado para pesquisar generos e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de generos em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/buscar")
	public ResponseEntity<?> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		Page<GeneroDTO> result = generoService.buscarPorNome(nome, pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}
}
