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

@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

	@Autowired
	private ApiKeyService apiKeyService;

	@Operation(summary = "Gera uma nova API Key")
	@PostMapping
	public ResponseEntity<ApiKey> gerarChave(@RequestBody Map<String, String> body) {
		String descricao = body.getOrDefault("descricao", "Chave sem descrição");
		return ResponseEntity.ok(apiKeyService.gerarChave(descricao));
	}

	@Operation(summary = "Lista todas as API Keys")
	@GetMapping
	public ResponseEntity<List<ApiKey>> listarChaves() {
		return ResponseEntity.ok(apiKeyService.listarChaves());
	}

	@Operation(summary = "Revoga uma API Key por ID")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> revogarChave(@PathVariable Integer id) {
		apiKeyService.revogarChave(id);
		return ResponseEntity.noContent().build();
	}
}
