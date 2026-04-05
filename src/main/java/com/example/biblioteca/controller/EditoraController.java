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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/editora")
public class EditoraController {

	@Autowired
	private EditoraService editoraService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Editora encontrada"),
			@ApiResponse(responseCode = "404", description = "Editora não encontrada")
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<EditoraDTO>> retornarEditoraId(
			@Parameter(description = "ID da editora")
			@PathVariable("id") Integer id) {
		EditoraDTO editoraDTO = new EditoraDTO(editoraService.retornarEditoraId(id));

		EntityModel<EditoraDTO> resource = EntityModel.of(editoraDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).atualizarEditoraId(id, null, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarTodosAsEditoras(null)).withRel("all"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra uma nova editora")
	@PostMapping
	public ResponseEntity<EntityModel<EditoraDTO>> cadastrarEditora(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados da editora a serem enviados",
					required = true,
					content = @Content(schema = @Schema(implementation = EditoraDTO.class)))
			@Valid @RequestBody EditoraDTO e,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Autowired UriComponentsBuilder ucb) {

		if (idempotencyKey != null) {
			Object cached = idempotencyService.getResponse(idempotencyKey);
			if (cached != null) {
				@SuppressWarnings("unchecked")
				EntityModel<EditoraDTO> cachedResource = (EntityModel<EditoraDTO>) cached;
				return ResponseEntity.ok(cachedResource);
			}
		}

		Editora editora = editoraService.cadastrarEditora(e);

		EntityModel<EditoraDTO> resource = EntityModel.of(new EditoraDTO(editora));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(editora.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).atualizarEditoraId(editora.getId(), null, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(editora.getId())).withRel("delete"));

		if (idempotencyKey != null) {
			idempotencyService.saveResponse(idempotencyKey, resource);
		}

		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera uma editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Editora atualizada"),
			@ApiResponse(responseCode = "404", description = "Editora não encontrada")
	})
	@PutMapping("/{id}")
	public ResponseEntity<EntityModel<EditoraDTO>> atualizarEditoraId(
			@Parameter(description = "ID da editora")
			@PathVariable("id") Integer id,
			@Valid @RequestBody EditoraDTO e,
			@Autowired UriComponentsBuilder ucb) {
		Editora editora = editoraService.atualizarEditora(id, e);

		EntityModel<EditoraDTO> resource = EntityModel.of(new EditoraDTO(editora));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(id)).withRel("delete"));

		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta uma editora por ID")
	@DeleteMapping("/{id}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Editora deletada"),
			@ApiResponse(responseCode = "404", description = "Editora não encontrada")
	})
	public ResponseEntity<Void> deletarEditoraId(
			@Parameter(description = "ID da editora")
			@PathVariable("id") Integer id) {
		editoraService.deletarEditoraId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todas as editoras")
	@GetMapping("/all")
	public Page<EditoraDTO> retornarTodosAsEditoras(@ParameterObject Pageable pageable) {
		return editoraService.retornarTodosAsEditoras(pageable);
	}

	@Operation(summary = "Busca editoras por nome")
	@GetMapping("/buscar")
	public Page<EditoraDTO> buscarPorNome(
			@RequestParam String nome,
			@ParameterObject Pageable pageable) {
		return editoraService.buscarPorNome(nome, pageable);
	}
}
