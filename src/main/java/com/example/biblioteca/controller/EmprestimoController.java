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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.domain.dto.EmprestimoCadastroDTO;
import com.example.biblioteca.domain.dto.EmprestimoDTO;
import com.example.biblioteca.domain.enums.StatusEmprestimo;
import com.example.biblioteca.service.EmprestimoService;
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
@RequestMapping("/emprestimo")
@Tag(name = "Emprestimo")
public class EmprestimoController {

	@Autowired
	private EmprestimoService emprestimoService;

	@Autowired
	private IdempotencyService idempotencyService;

	@Operation(summary = "Realiza um emprestimo de livro")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Novo emprestimo realizado com sucesso na biblioteca."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para realizar o emprestimo sao invalidos. Verifique usuario, livro e disponibilidade."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para realizar emprestimos."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. O livro ja esta emprestado ou o usuario atingiu o limite de emprestimos."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de emprestimo em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<EmprestimoDTO>> realizarEmprestimo(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do emprestimo", required = true,
					content = @Content(schema = @Schema(implementation = EmprestimoCadastroDTO.class)))
			@Valid @RequestBody EmprestimoCadastroDTO dto,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			idempotencyKey = UUID.randomUUID().toString();
		}
		String payloadHash = String.valueOf(dto.hashCode());
		Object cached = idempotencyService.getResponse(idempotencyKey, payloadHash);
		if (cached != null) {
			@SuppressWarnings("unchecked")
			EntityModel<EmprestimoDTO> cachedResource = (EntityModel<EmprestimoDTO>) cached;
			return ResponseEntity.ok(cachedResource);
		}

		Emprestimo emprestimo = emprestimoService.realizarEmprestimo(dto);
		EmprestimoDTO emprestimoDTO = new EmprestimoDTO(emprestimo);
		EntityModel<EmprestimoDTO> resource = EntityModel.of(emprestimoDTO);
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EmprestimoController.class).retornarEmprestimoId(emprestimo.getId())).withSelfRel();
		resource.add(selfLink);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EmprestimoController.class).realizarDevolucao(emprestimo.getId())).withRel("devolver"));
		idempotencyService.saveResponse(idempotencyKey, payloadHash, resource);
		return ResponseEntity.created(URI.create(selfLink.getHref())).body(resource);
	}

	@Operation(summary = "Realiza a devolucao de um emprestimo")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A devolucao do livro foi realizada com sucesso e o emprestimo foi encerrado."),
			@ApiResponse(responseCode = "400", description = "O ID informado para realizar a devolucao e invalido ou o emprestimo ja foi devolvido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para realizar devolucoes."),
			@ApiResponse(responseCode = "404", description = "Nenhum emprestimo foi encontrado com esse ID para devolucao."),
			@ApiResponse(responseCode = "409", description = "Conflito detectado. Este emprestimo ja foi devolvido anteriormente."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de devolucao em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping("/{id}/devolver")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<EntityModel<EmprestimoDTO>> realizarDevolucao(
			@Parameter(description = "ID do emprestimo") @PathVariable("id") Integer id) {
		Emprestimo emprestimo = emprestimoService.realizarDevolucao(id);
		EmprestimoDTO emprestimoDTO = new EmprestimoDTO(emprestimo);
		EntityModel<EmprestimoDTO> resource = EntityModel.of(emprestimoDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EmprestimoController.class).retornarEmprestimoId(id)).withSelfRel());
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Encontra emprestimo por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "O emprestimo foi encontrado com sucesso no sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para buscar o emprestimo e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhum emprestimo foi encontrado com esse ID no sistema."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de emprestimos em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<EmprestimoDTO>> retornarEmprestimoId(
			@Parameter(description = "ID do emprestimo") @PathVariable("id") Integer id) {
		EmprestimoDTO dto = emprestimoService.retornarEmprestimoId(id);
		EntityModel<EmprestimoDTO> resource = EntityModel.of(dto);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EmprestimoController.class).retornarEmprestimoId(id)).withSelfRel());
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EmprestimoController.class).realizarDevolucao(id)).withRel("devolver"));
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Lista todos os emprestimos")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de emprestimos registrados foi retornada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "Os parametros de paginacao informados sao invalidos."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de emprestimos em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/all")
	public ResponseEntity<?> retornarTodosOsEmprestimos(@ParameterObject Pageable pageable) {
		Page<EmprestimoDTO> result = emprestimoService.retornarTodosOsEmprestimos(pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Busca emprestimos por usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por emprestimos do usuario foi realizada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "O ID do usuario informado para buscar emprestimos e invalido."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de emprestimos em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/usuario/{usuarioId}")
	public ResponseEntity<?> buscarPorUsuario(
			@Parameter(description = "ID do usuario") @PathVariable Integer usuarioId,
			@ParameterObject Pageable pageable) {
		Page<EmprestimoDTO> result = emprestimoService.buscarPorUsuario(usuarioId, pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Busca emprestimos por status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A busca por emprestimos filtrada por status foi realizada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "400", description = "O status informado para filtrar emprestimos e invalido. Utilize ATIVO ou DEVOLVIDO."),
			@ApiResponse(responseCode = "429", description = "Muitas buscas de emprestimos por status em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping("/status")
	public ResponseEntity<?> buscarPorStatus(
			@RequestParam StatusEmprestimo status,
			@ParameterObject Pageable pageable) {
		Page<EmprestimoDTO> result = emprestimoService.buscarPorStatus(status, pageable);
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Deleta um emprestimo por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "O registro de emprestimo foi removido com sucesso do sistema da biblioteca."),
			@ApiResponse(responseCode = "400", description = "O ID informado para remover o emprestimo e invalido."),
			@ApiResponse(responseCode = "401", description = "Acesso nao autorizado. Uma X-API-Key valida e necessaria para remover emprestimos."),
			@ApiResponse(responseCode = "404", description = "Nenhum emprestimo foi encontrado com esse ID no sistema."),
			@ApiResponse(responseCode = "409", description = "Conflito ao remover o emprestimo. O emprestimo ainda esta ativo e precisa ser devolvido primeiro."),
			@ApiResponse(responseCode = "422", description = "O emprestimo esta ativo. Realize a devolucao antes de excluir o registro de emprestimo."),
			@ApiResponse(responseCode = "429", description = "Muitas remocoes de emprestimos em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	@SecurityRequirement(name = "X-API-Key")
	public ResponseEntity<Void> deletarEmprestimo(
			@Parameter(description = "ID do emprestimo") @PathVariable("id") Integer id) {
		emprestimoService.deletarEmprestimo(id);
		return ResponseEntity.noContent().build();
	}
}
