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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/livro")
@Tag(name = "Livro")
public class LivroController {

	@Autowired
	private LivroService livroService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra livro por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O livro foi encontrado com sucesso no acervo da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar o livro e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhum livro foi encontrado com esse ID no acervo."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas ao acervo em sequencia. Aguarde antes de tentar novamente."),
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
			@ApiResponse(responseCode = "201", description = "Novo livro cadastrado com sucesso no acervo da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para cadastrar o livro sao invalidos. Verifique nome, descricao, ISBN, paginas ou X-Idempotency-Key."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para cadastrar livros."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe um livro com esse ISBN ou a X-Idempotency-Key foi reutilizada incorretamente."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de cadastro em sequencia. O sistema atingiu o limite temporario de requisicoes."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<LivroDTO>> cadastrarLivro(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do livro", required = true,
					content = @Content(schema = @Schema(implementation = LivroCadastroDTO.class)))
			@Valid @RequestBody LivroCadastroDTO l,
			@RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

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
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).atualizarLivroId(livro.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroController.class).deletarLivroId(livro.getId())).withRel("delete"));

		if (idempotencyKey != null) {
			idempotencyService.saveResponse(idempotencyKey, resource);
		}

		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um livro por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O livro foi atualizado com sucesso no acervo da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para atualizar o livro sao invalidos. Verifique nome, descricao, ISBN ou paginas."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para atualizar livros."),
			@ApiResponse(responseCode = "404", description = "Nenhum livro foi encontrado com esse ID para atualizacao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe outro livro com esse ISBN no acervo."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de atualizacao em sequencia. Aguarde antes de tentar novamente."),
	})
	@PutMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
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
			@ApiResponse(responseCode = "204", description = "O livro foi removido com sucesso do acervo da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover o livro e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover livros."),
			@ApiResponse(responseCode = "404", description = "Nenhum livro foi encontrado com esse ID no acervo."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover o livro. Existem emprestimos ativos vinculados."),
			@ApiResponse(responseCode = "422", description = "O livro possui emprestimos vinculados. Remova os emprestimos antes de excluir o livro."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes em sequencia. O sistema bloqueou temporariamente novas exclusoes."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarLivroId(
			@Parameter(description = "ID do livro") @PathVariable("id") Integer id) {
		livroService.deletarLivroId(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os livros")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de livros do acervo foi retornada com sucesso."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas ao acervo em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<Page<LivroDTO>> retornarTodosOsLivros(@ParameterObject Pageable pageable) {
		return ResponseEntity.ok(livroService.retornarTodosOsLivros(pageable));
	}

	@Operation(summary = "Busca livros por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por livros foi realizada com sucesso."),
			@ApiResponse(responseCode = "400", description = "O parametro de busca informado para pesquisar livros e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas ao acervo em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/buscar")
	public ResponseEntity<Page<LivroDTO>> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(livroService.buscarPorNome(nome, pageable));
	}

	@Operation(summary = "Busca livros por status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por livros filtrada por status foi realizada com sucesso."),
			@ApiResponse(responseCode = "400", description = "O status informado para filtrar livros e invalido. Utilize DISPONIVEL ou EMPRESTADO."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas por status em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/status")
	public ResponseEntity<Page<LivroDTO>> buscarPorStatus(
			@RequestParam StatusLivro status, @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(livroService.buscarPorStatus(status, pageable));
	}
}
