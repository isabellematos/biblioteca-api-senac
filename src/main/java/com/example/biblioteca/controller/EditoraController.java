package com.example.biblioteca.controller;

import java.net.URI;
import java.util.UUID;

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

import com.example.biblioteca.domain.Editora;
import com.example.biblioteca.domain.dto.EditoraDTO;
import com.example.biblioteca.service.EditoraService;
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
@RequestMapping("/editora")
@Tag(name = "Editora")
public class EditoraController {

	@Autowired
	private EditoraService editoraService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A editora foi encontrada com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar a editora e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhuma editora foi encontrada com esse ID no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de editoras em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<EditoraDTO>> retornarEditoraId(
			@Parameter(description = "ID da editora") @PathVariable("id") Integer id) {
		EditoraDTO editoraDTO = new EditoraDTO(editoraService.retornarEditoraId(id));
		EntityModel<EditoraDTO> resource = EntityModel.of(editoraDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).atualizarEditoraId(id, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarTodosAsEditoras(null)).withRel("all"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra uma nova editora")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Nova editora cadastrada com sucesso no sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para cadastrar a editora sao invalidos. Verifique nome e demais campos obrigatorios."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para cadastrar editoras."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe uma editora com esse nome no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de cadastro de editoras em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<EditoraDTO>> cadastrarEditora(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados da editora", required = true,
					content = @Content(schema = @Schema(implementation = EditoraDTO.class)))
			@Valid @RequestBody EditoraDTO e,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			idempotencyKey = UUID.randomUUID().toString();
		}
		String payloadHash = String.valueOf(e.hashCode());
		Object cached = idempotencyService.getResponse(idempotencyKey, payloadHash);
		if (cached != null) {
			@SuppressWarnings("unchecked")
			EntityModel<EditoraDTO> cachedResource = (EntityModel<EditoraDTO>) cached;
			return ResponseEntity.ok(cachedResource);
		}

		Editora editora = editoraService.cadastrarEditora(e);
		EntityModel<EditoraDTO> resource = EntityModel.of(new EditoraDTO(editora));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(editora.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).atualizarEditoraId(editora.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(editora.getId())).withRel("delete"));
		idempotencyService.saveResponse(idempotencyKey, payloadHash, resource);
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera uma editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A editora foi atualizada com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para atualizar a editora sao invalidos. Verifique nome e demais campos."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para atualizar editoras."),
			@ApiResponse(responseCode = "404", description = "Nenhuma editora foi encontrada com esse ID para atualizacao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe outra editora com esse nome no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de atualizacao de editoras em sequencia. Aguarde antes de tentar novamente."),
	})
	@PutMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<EditoraDTO>> atualizarEditoraId(
			@Parameter(description = "ID da editora") @PathVariable("id") Integer id,
			@Valid @RequestBody EditoraDTO e) {
		Editora editora = editoraService.atualizarEditora(id, e);
		EntityModel<EditoraDTO> resource = EntityModel.of(new EditoraDTO(editora));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(id)).withRel("delete"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta uma editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "A editora foi removida com sucesso do cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover a editora e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover editoras."),
			@ApiResponse(responseCode = "404", description = "Nenhuma editora foi encontrada com esse ID no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover a editora. Existem livros vinculados a esta editora."),
			@ApiResponse(responseCode = "422", description = "A editora possui livros vinculados. Remova os livros antes de excluir a editora."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes de editoras em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarEditoraId(
			@Parameter(description = "ID da editora") @PathVariable("id") Integer id) {
		editoraService.deletarEditoraId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todas as editoras")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de editoras cadastradas foi retornada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de editoras em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<?> retornarTodosAsEditoras(@ParameterObject Pageable pageable) {
		Page<EditoraDTO> result = editoraService.retornarTodosAsEditoras(pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Busca editoras por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por editoras foi realizada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "O parametro de busca informado para pesquisar editoras e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de editoras em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/buscar")
	public ResponseEntity<?> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		Page<EditoraDTO> result = editoraService.buscarPorNome(nome, pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}
}
