package com.example.biblioteca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.biblioteca.domain.ApiKey;
import com.example.biblioteca.domain.enums.AccessLevel;
import com.example.biblioteca.service.ApiKeyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "API Keys")
public class ApiKeyController {

	@Autowired
	private ApiKeyService apiKeyService;

	@Getter @Setter @NoArgsConstructor
	public static class ApiKeyRequest {
		@NotBlank(message = "O campo owner e obrigatorio")
		@Size(min = 2, max = 100, message = "Owner deve ter entre 2 e 100 caracteres")
		private String owner;
		private AccessLevel accessLevel;
	}

	@Operation(summary = "Gera uma nova API Key")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Nova chave de API gerada com sucesso."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para gerar a chave sao invalidos. Informe owner (2-100 caracteres)."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de geracao de chaves em sequencia. Aguarde antes de tentar novamente.")
	})
	@PostMapping
	public ResponseEntity<ApiKey> gerarChave(@Valid @RequestBody ApiKeyRequest request) {
		ApiKey key = apiKeyService.gerarChave(request.getOwner(), request.getAccessLevel());
		return ResponseEntity.ok(key);
	}

	@Operation(summary = "Lista todas as API Keys")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de chaves de API foi retornada com sucesso."),
			@ApiResponse(responseCode = "204", description = "Nenhum registro encontrado para esta consulta."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de chaves em sequencia. Aguarde antes de tentar novamente.")
	})
	@GetMapping
	public ResponseEntity<List<ApiKey>> listarChaves() {
		List<ApiKey> chaves = apiKeyService.listarChaves();
		if (chaves.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(chaves);
	}

	@Operation(summary = "Busca API Key por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Chave encontrada com sucesso."),
			@ApiResponse(responseCode = "404", description = "Nenhuma chave encontrada com esse ID."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas em sequencia.")
	})
	@GetMapping("/{id}")
	public ResponseEntity<ApiKey> buscarPorId(@PathVariable Integer id) {
		return ResponseEntity.ok(apiKeyService.buscarPorId(id));
	}

	@Operation(summary = "Revoga uma API Key por ID (requer nivel ADMIN)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "A chave de API foi revogada com sucesso."),
			@ApiResponse(responseCode = "400", description = "O ID informado para revogar a chave e invalido."),
			@ApiResponse(responseCode = "401", description = "Header X-API-Key ausente."),
			@ApiResponse(responseCode = "403", description = "Nivel de acesso insuficiente. Apenas chaves ADMIN podem revogar outras chaves."),
			@ApiResponse(responseCode = "404", description = "Nenhuma chave de API foi encontrada com esse ID."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de revogacao em sequencia.")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> revogarChave(@PathVariable Integer id) {
		apiKeyService.revogarChave(id);
		return ResponseEntity.noContent().build();
	}
}
