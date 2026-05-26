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

import com.example.biblioteca.domain.Idioma;
import com.example.biblioteca.domain.dto.IdiomaDTO;
import com.example.biblioteca.service.IdiomaService;
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
@RequestMapping("/idioma")
@Tag(name = "Idioma")
public class IdiomaController {

	@Autowired
	private IdiomaService idiomaService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra idioma por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O idioma foi encontrado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar o idioma e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhum idioma foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de idiomas em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<IdiomaDTO>> retornarIdiomaId(
			@Parameter(description = "ID do idioma") @PathVariable("id") Integer id) {
		IdiomaDTO idiomaDTO = new IdiomaDTO(idiomaService.retornaridiomaId(id));
		EntityModel<IdiomaDTO> resource = EntityModel.of(idiomaDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).retornarIdiomaId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).atualizarIdiomaId(id, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).deletarIdiomaId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).retornarTodosOsIdiomas(null)).withRel("all"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo idioma")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Novo idioma cadastrado com sucesso no sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para cadastrar o idioma sao invalidos. Verifique o nome do idioma."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para cadastrar idiomas."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe um idioma com esse nome no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de cadastro de idiomas em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<IdiomaDTO>> cadastrarIdioma(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do idioma", required = true,
					content = @Content(schema = @Schema(implementation = IdiomaDTO.class)))
			@Valid @RequestBody IdiomaDTO i,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			throw new IllegalArgumentException("O header Idempotency-Key e obrigatorio e nao pode ser vazio.");
		}
		String payloadHash = String.valueOf(i.hashCode());
		Object cached = idempotencyService.getResponse(idempotencyKey, payloadHash);
		if (cached != null) {
			@SuppressWarnings("unchecked")
			EntityModel<IdiomaDTO> cachedResource = (EntityModel<IdiomaDTO>) cached;
			return ResponseEntity.ok(cachedResource);
		}

		Idioma idioma = idiomaService.cadastrarIdioma(i);
		EntityModel<IdiomaDTO> resource = EntityModel.of(new IdiomaDTO(idioma));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).retornarIdiomaId(idioma.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).atualizarIdiomaId(idioma.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).deletarIdiomaId(idioma.getId())).withRel("delete"));
		idempotencyService.saveResponse(idempotencyKey, payloadHash, resource);
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um idioma por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O idioma foi atualizado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para atualizar o idioma sao invalidos. Verifique o nome do idioma."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para atualizar idiomas."),
			@ApiResponse(responseCode = "404", description = "Nenhum idioma foi encontrado com esse ID para atualizacao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe outro idioma com esse nome no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de atualizacao de idiomas em sequencia. Aguarde antes de tentar novamente."),
	})
	@PutMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<IdiomaDTO>> atualizarIdiomaId(
			@Parameter(description = "ID do idioma") @PathVariable("id") Integer id,
			@Valid @RequestBody IdiomaDTO i) {
		Idioma idioma = idiomaService.atualizarIdioma(id, i);
		EntityModel<IdiomaDTO> resource = EntityModel.of(new IdiomaDTO(idioma));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).retornarIdiomaId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).deletarIdiomaId(id)).withRel("delete"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um idioma por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "O idioma foi removido com sucesso do cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover o idioma e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover idiomas."),
			@ApiResponse(responseCode = "404", description = "Nenhum idioma foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover o idioma. Existem livros vinculados a este idioma."),
			@ApiResponse(responseCode = "422", description = "O idioma possui livros vinculados. Remova os livros antes de excluir o idioma."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes de idiomas em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarIdiomaId(
			@Parameter(description = "ID do idioma") @PathVariable("id") Integer id) {
		idiomaService.deletarIdiomaId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os idiomas")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de idiomas cadastrados foi retornada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de idiomas em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<?> retornarTodosOsIdiomas(@ParameterObject Pageable pageable) {
		Page<IdiomaDTO> result = idiomaService.retornarTodosOsIdiomas(pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Busca idiomas por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por idiomas foi realizada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "O parametro de busca informado para pesquisar idiomas e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de idiomas em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/buscar")
	public ResponseEntity<?> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		Page<IdiomaDTO> result = idiomaService.buscarPorNome(nome, pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}
}
