package com.example.biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.ApiKey;
import com.example.biblioteca.domain.enums.AccessLevel;
import com.example.biblioteca.repository.ApiKeyRepository;

@Service
public class ApiKeyService {

	@Autowired
	private ApiKeyRepository apiKeyRepository;

	public Optional<ApiKey> findByKeyValue(String keyValue) {
		return apiKeyRepository.findByKeyValueAndActiveTrue(keyValue);
	}

	public boolean isValidKey(String keyValue) {
		return apiKeyRepository.findByKeyValueAndActiveTrue(keyValue).isPresent();
	}

	public ApiKey gerarChave(String owner, AccessLevel accessLevel) {
		ApiKey apiKey = new ApiKey();
		apiKey.setOwner(owner);
		apiKey.setAccessLevel(accessLevel != null ? accessLevel : AccessLevel.WRITE);
		return apiKeyRepository.save(apiKey);
	}

	public List<ApiKey> listarChaves() {
		return apiKeyRepository.findAll();
	}

	public ApiKey buscarPorId(Integer id) {
		return apiKeyRepository.findById(id).orElseThrow();
	}

	public void revogarChave(Integer id) {
		ApiKey apiKey = apiKeyRepository.findById(id).orElseThrow();
		apiKey.setActive(false);
		apiKeyRepository.save(apiKey);
	}
}
