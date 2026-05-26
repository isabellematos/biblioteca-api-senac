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

import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.domain.dto.UsuarioDTO;
import com.example.biblioteca.service.IdempotencyService;
import com.example.biblioteca.service.UsuarioService;

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
@RequestMapping("/usuario")
@Tag(name = "Usuario")
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Encontra usuario por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O usuario foi encontrado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar o usuario e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhum usuario foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de usuarios em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<UsuarioDTO>> retornarUsuarioId(
			@Parameter(description = "ID do usuario") @PathVariable("id") Integer id) {
		UsuarioDTO dto = new UsuarioDTO(usuarioService.retornarUsuarioId(id));
		EntityModel<UsuarioDTO> resource = EntityModel.of(dto);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).retornarUsuarioId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).atualizarUsuario(id, null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).deletarUsuario(id)).withRel("delete"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).retornarTodosOsUsuarios(null)).withRel("all"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Cadastra um novo usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Novo usuario cadastrado com sucesso no sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para cadastrar o usuario sao invalidos. Verifique nome, email e demais campos obrigatorios."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para cadastrar usuarios."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe um usuario com esse email no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de cadastro de usuarios em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<UsuarioDTO>> cadastrarUsuario(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do usuario", required = true,
					content = @Content(schema = @Schema(implementation = UsuarioDTO.class)))
			@Valid @RequestBody UsuarioDTO dto,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			idempotencyKey = UUID.randomUUID().toString();
		}
		String payloadHash = String.valueOf(dto.hashCode());
		Object cached = idempotencyService.getResponse(idempotencyKey, payloadHash);
		if (cached != null) {
			@SuppressWarnings("unchecked")
			EntityModel<UsuarioDTO> cachedResource = (EntityModel<UsuarioDTO>) cached;
			return ResponseEntity.ok(cachedResource);
		}

		Usuario usuario = usuarioService.cadastrarUsuario(dto);
		EntityModel<UsuarioDTO> resource = EntityModel.of(new UsuarioDTO(usuario));
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).retornarUsuarioId(usuario.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).atualizarUsuario(usuario.getId(), null)).withRel("update"));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).deletarUsuario(usuario.getId())).withRel("delete"));
		idempotencyService.saveResponse(idempotencyKey, payloadHash, resource);
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Altera um usuario por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O usuario foi atualizado com sucesso no cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para atualizar o usuario sao invalidos. Verifique nome, email e demais campos."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para atualizar usuarios."),
			@ApiResponse(responseCode = "404", description = "Nenhum usuario foi encontrado com esse ID para atualizacao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Ja existe outro usuario com esse email no cadastro."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de atualizacao de usuarios em sequencia. Aguarde antes de tentar novamente."),
	})
	@PutMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<UsuarioDTO>> atualizarUsuario(
			@Parameter(description = "ID do usuario") @PathVariable("id") Integer id,
			@Valid @RequestBody UsuarioDTO dto) {
		Usuario usuario = usuarioService.atualizarUsuario(id, dto);
		EntityModel<UsuarioDTO> resource = EntityModel.of(new UsuarioDTO(usuario));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).retornarUsuarioId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).deletarUsuario(id)).withRel("delete"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Deleta um usuario por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "O usuario foi removido com sucesso do cadastro da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover o usuario e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover usuarios."),
			@ApiResponse(responseCode = "404", description = "Nenhum usuario foi encontrado com esse ID no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover o usuario. Existem emprestimos ativos vinculados a este usuario."),
			@ApiResponse(responseCode = "422", description = "O usuario possui emprestimos vinculados. Remova os emprestimos antes de excluir o usuario."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes de usuarios em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarUsuario(
			@Parameter(description = "ID do usuario") @PathVariable("id") Integer id) {
		usuarioService.deletarUsuario(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encontra todos os usuarios")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de usuarios cadastrados foi retornada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de usuarios em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<?> retornarTodosOsUsuarios(@ParameterObject Pageable pageable) {
		Page<UsuarioDTO> result = usuarioService.retornarTodosOsUsuarios(pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Busca usuarios por nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por usuarios foi realizada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "O parametro de busca informado para pesquisar usuarios e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de usuarios em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/buscar")
	public ResponseEntity<?> buscarPorNome(
			@RequestParam String nome, @ParameterObject Pageable pageable) {
		Page<UsuarioDTO> result = usuarioService.buscarPorNome(nome, pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Empresta um livro para o usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O livro foi emprestado com sucesso para o usuario."),
			@ApiResponse(responseCode = "400", description = "Os IDs informados sao invalidos ou o livro nao esta disponivel para emprestimo."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para realizar emprestimos."),
			@ApiResponse(responseCode = "404", description = "O usuario ou o livro informado nao foi encontrado no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. O livro ja esta emprestado para outro usuario."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de emprestimo em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping("/{usuarioId}/emprestar/{livroId}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<UsuarioDTO>> emprestarLivro(
			@Parameter(description = "ID do usuario") @PathVariable Integer usuarioId,
			@Parameter(description = "ID do livro") @PathVariable Integer livroId) {
		Usuario usuario = usuarioService.emprestarLivro(usuarioId, livroId);
		EntityModel<UsuarioDTO> resource = EntityModel.of(new UsuarioDTO(usuario));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).retornarUsuarioId(usuarioId)).withSelfRel());
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Devolve um livro emprestado")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O livro foi devolvido com sucesso pelo usuario."),
			@ApiResponse(responseCode = "400", description = "Os IDs informados sao invalidos ou o livro nao esta emprestado para este usuario."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para realizar devolucoes."),
			@ApiResponse(responseCode = "404", description = "O usuario ou o livro informado nao foi encontrado no cadastro."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. O livro nao consta como emprestado para este usuario."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de devolucao em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping("/{usuarioId}/devolver/{livroId}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<UsuarioDTO>> devolverLivro(
			@Parameter(description = "ID do usuario") @PathVariable Integer usuarioId,
			@Parameter(description = "ID do livro") @PathVariable Integer livroId) {
		Usuario usuario = usuarioService.devolverLivro(usuarioId, livroId);
		EntityModel<UsuarioDTO> resource = EntityModel.of(new UsuarioDTO(usuario));
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).retornarUsuarioId(usuarioId)).withSelfRel());
		return ResponseEntity.ok(resource);
	}
}
