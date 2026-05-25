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

import com.example.biblioteca.domain.Autor;
import com.example.biblioteca.domain.dto.AutorDTO;
import com.example.biblioteca.service.AutorService;

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
@RequestMapping("/autor")
@Tag(name = "Autor")
public class AutorController {

	@Autowired
	private AutorService autorService;

	@Operation(summary = "Encontra autor por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O autor foi encontrado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar o autor e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhum autor foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de autores em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<AutorDTO>> retornarAutorId(
			@Parameter(description = "ID do autor") @PathVariable("id") Integer id) {
		AutorDTO autorDTO = new AutorDTO(autorService.retornarAutorId(id));
		EntityModel<AutorDTO> resource = EntityModel.of(autorDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).retornarAutorId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).atualizarAutorId(id, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).deletarAutorId(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).retornarTodosOsAutores(null)).withRel("all"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo autor")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Novo autor cadastrado com sucesso no sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para cadastrar o autor sao invalidos. Verifique nome e demais campos obrigatorios."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para cadastrar autores."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe um autor com esses dados no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de cadastro de autores em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<AutorDTO>> cadastrarAutor(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do autor", required = true,
					content = @Content(schema = @Schema(implementation = AutorDTO.class)))
			@Valid @RequestBody AutorDTO a) {
		Autor autor = autorService.cadastrarAutor(a);
		EntityModel<AutorDTO> resource = EntityModel.of(new AutorDTO(autor));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).retornarAutorId(autor.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).atualizarAutorId(autor.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).deletarAutorId(autor.getId())).withRel("delete"));
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um autor por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O autor foi atualizado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para atualizar o autor sao invalidos. Verifique nome e demais campos."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para atualizar autores."),
			@ApiResponse(responseCode = "404", description = "Nenhum autor foi encontrado com esse ID para atualizacao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe outro autor com esses dados no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de atualizacao de autores em sequencia. Aguarde antes de tentar novamente."),
	})
	@PutMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<AutorDTO>> atualizarAutorId(
			@Parameter(description = "ID do autor") @PathVariable("id") Integer id,
			@Valid @RequestBody AutorDTO a) {
		Autor autor = autorService.atualizarAutor(id, a);
		EntityModel<AutorDTO> resource = EntityModel.of(new AutorDTO(autor));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).retornarAutorId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AutorController.class).deletarAutorId(id)).withRel("delete"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um autor por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "O autor foi removido com sucesso do cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover o autor e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover autores."),
			@ApiResponse(responseCode = "404", description = "Nenhum autor foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover o autor. Existem livros vinculados a este autor."),
			@ApiResponse(responseCode = "422", description = "O autor possui livros vinculados. Remova os livros antes de excluir o autor."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes de autores em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarAutorId(
			@Parameter(description = "ID do autor") @PathVariable("id") Integer id) {
		autorService.deletarAutorId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os autores")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de autores cadastrados foi retornada com sucesso."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de autores em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<Page<AutorDTO>> retornarTodosOsAutores(@ParameterObject Pageable pageable) {
		return ResponseEntity.ok(autorService.retornarTodosOsAutores(pageable));
	}

	@Operation(summary = "Busca autores por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por autores foi realizada com sucesso."),
			@ApiResponse(responseCode = "400", description = "O parametro de busca informado para pesquisar autores e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de autores em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/buscar")
	public ResponseEntity<Page<AutorDTO>> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(autorService.buscarPorNome(nome, pageable));
	}
}
