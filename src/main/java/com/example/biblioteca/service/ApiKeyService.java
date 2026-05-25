package com.example.biblioteca.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.ApiKey;
import com.example.biblioteca.repository.ApiKeyRepository;

@Service
public class ApiKeyService {

	@Autowired
	private ApiKeyRepository apiKeyRepository;

	public boolean isValidKey(String chave) {
		return apiKeyRepository.findByChaveAndAtivaTrue(chave).isPresent();
	}

	public ApiKey gerarChave(String descricao) {
		ApiKey apiKey = new ApiKey();
		apiKey.setChave(UUID.randomUUID().toString());
		apiKey.setDescricao(descricao);
		return apiKeyRepository.save(apiKey);
	}

	public List<ApiKey> listarChaves() {
		return apiKeyRepository.findAll();
	}

	public void revogarChave(Integer id) {
		ApiKey apiKey = apiKeyRepository.findById(id).orElseThrow();
		apiKey.setAtiva(false);
		apiKeyRepository.save(apiKey);
	}
}
