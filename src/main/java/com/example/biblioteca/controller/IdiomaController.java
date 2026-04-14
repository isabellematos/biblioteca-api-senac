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

import com.example.biblioteca.domain.Idioma;
import com.example.biblioteca.domain.dto.IdiomaDTO;
import com.example.biblioteca.service.IdiomaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/idioma")
@Tag(name = "Idioma")
public class IdiomaController {

	@Autowired
	private IdiomaService idiomaService;

	@Operation(summary = "Encontra idioma por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Idioma encontrado"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "404", description = "Idioma não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
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
			@ApiResponse(responseCode = "201", description = "Idioma criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@PostMapping
	public ResponseEntity<EntityModel<IdiomaDTO>> cadastrarIdioma(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do idioma", required = true,
					content = @Content(schema = @Schema(implementation = IdiomaDTO.class)))
			@Valid @RequestBody IdiomaDTO i) {
		Idioma idioma = idiomaService.cadastrarIdioma(i);
		EntityModel<IdiomaDTO> resource = EntityModel.of(new IdiomaDTO(idioma));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).retornarIdiomaId(idioma.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).atualizarIdiomaId(idioma.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IdiomaController.class).deletarIdiomaId(idioma.getId())).withRel("delete"));
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um idioma por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Idioma atualizado"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "404", description = "Idioma não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@PutMapping("/{id}")
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
			@ApiResponse(responseCode = "204", description = "Idioma deletado"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "404", description = "Idioma não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarIdiomaId(
			@Parameter(description = "ID do idioma") @PathVariable("id") Integer id) {
		idiomaService.deletarIdiomaId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os idiomas")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de idiomas retornada"),
			@ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/all")
	public ResponseEntity<Page<IdiomaDTO>> retornarTodosOsIdiomas(@ParameterObject Pageable pageable) {
		return ResponseEntity.ok(idiomaService.retornarTodosOsIdiomas(pageable));
	}

	@Operation(summary = "Busca idiomas por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Resultado da busca"),
			@ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
	})
	@GetMapping("/buscar")
	public ResponseEntity<Page<IdiomaDTO>> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(idiomaService.buscarPorNome(nome, pageable));
	}
}
