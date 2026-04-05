package com.example.biblioteca.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

	private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

	public Object getResponse(String key) {
		return cache.get(key);
	}

	public void saveResponse(String key, Object response) {
		cache.put(key, response);
	}
}
