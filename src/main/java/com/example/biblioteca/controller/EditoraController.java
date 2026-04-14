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

import com.example.biblioteca.domain.Editora;
import com.example.biblioteca.domain.dto.EditoraDTO;
import com.example.biblioteca.service.EditoraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/editora")
@Tag(name = "Editora")
public class EditoraController {

	@Autowired
	private EditoraService editoraService;

	@Operation(summary = "Encontra editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Editora encontrada"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "404", description = "Editora não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
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
			@ApiResponse(responseCode = "201", description = "Editora criada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@PostMapping
	public ResponseEntity<EntityModel<EditoraDTO>> cadastrarEditora(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados da editora", required = true,
					content = @Content(schema = @Schema(implementation = EditoraDTO.class)))
			@Valid @RequestBody EditoraDTO e) {
		Editora editora = editoraService.cadastrarEditora(e);
		EntityModel<EditoraDTO> resource = EntityModel.of(new EditoraDTO(editora));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).retornarEditoraId(editora.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).atualizarEditoraId(editora.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EditoraController.class).deletarEditoraId(editora.getId())).withRel("delete"));
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera uma editora por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Editora atualizada"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "404", description = "Editora não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@PutMapping("/{id}")
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
			@ApiResponse(responseCode = "204", description = "Editora deletada"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "404", description = "Editora não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarEditoraId(
			@Parameter(description = "ID da editora") @PathVariable("id") Integer id) {
		editoraService.deletarEditoraId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todas as editoras")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de editoras retornada"),
			@ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/all")
	public ResponseEntity<Page<EditoraDTO>> retornarTodosAsEditoras(@ParameterObject Pageable pageable) {
		return ResponseEntity.ok(editoraService.retornarTodosAsEditoras(pageable));
	}

	@Operation(summary = "Busca editoras por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Resultado da busca"),
			@ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/buscar")
	public ResponseEntity<Page<EditoraDTO>> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(editoraService.buscarPorNome(nome, pageable));
	}
}
