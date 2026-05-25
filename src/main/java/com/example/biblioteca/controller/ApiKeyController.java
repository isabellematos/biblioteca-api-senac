package com.example.biblioteca.controller;

import java.util.List;
import java.util.Map;

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
import com.example.biblioteca.service.ApiKeyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "API Keys")
public class ApiKeyController {

	@Autowired
	private ApiKeyService apiKeyService;

	@Operation(summary = "Gera uma nova API Key")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Nova chave de API gerada com sucesso."),
			@ApiResponse(responseCode = "400", description = "Os dados enviados para gerar a chave sao invalidos. Informe uma descricao."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de geracao de chaves em sequencia. Aguarde antes de tentar novamente."),
	})
	@PostMapping
	public ResponseEntity<ApiKey> gerarChave(@RequestBody Map<String, String> body) {
		String descricao = body.getOrDefault("descricao", "Chave sem descrição");
		return ResponseEntity.ok(apiKeyService.gerarChave(descricao));
	}

	@Operation(summary = "Lista todas as API Keys")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "A lista de chaves de API foi retornada com sucesso."),
			@ApiResponse(responseCode = "429", description = "Muitas consultas de chaves em sequencia. Aguarde antes de tentar novamente."),
	})
	@GetMapping
	public ResponseEntity<List<ApiKey>> listarChaves() {
		return ResponseEntity.ok(apiKeyService.listarChaves());
	}

	@Operation(summary = "Revoga uma API Key por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "A chave de API foi revogada com sucesso."),
			@ApiResponse(responseCode = "400", description = "O ID informado para revogar a chave e invalido."),
			@ApiResponse(responseCode = "404", description = "Nenhuma chave de API foi encontrada com esse ID."),
			@ApiResponse(responseCode = "429", description = "Muitas tentativas de revogacao em sequencia. Aguarde antes de tentar novamente."),
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> revogarChave(@PathVariable Integer id) {
		apiKeyService.revogarChave(id);
		return ResponseEntity.noContent().build();
	}
}
