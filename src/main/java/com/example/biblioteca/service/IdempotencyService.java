package com.example.biblioteca.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.biblioteca.exception.ConflictException;

@Service
public class IdempotencyService {

	private final ConcurrentHashMap<String, CachedEntry> cache = new ConcurrentHashMap<>();

	public Object getResponse(String key, String payloadHash) {
		CachedEntry entry = cache.get(key);
		if (entry == null) return null;
		if (!entry.payloadHash.equals(payloadHash)) {
			throw new ConflictException("Idempotency-Key ja utilizada com um payload diferente.");
		}
		return entry.response;
	}

	public void saveResponse(String key, String payloadHash, Object response) {
		cache.put(key, new CachedEntry(payloadHash, response));
	}

	private static class CachedEntry {
		final String payloadHash;
		final Object response;

		CachedEntry(String payloadHash, Object response) {
			this.payloadHash = payloadHash;
			this.response = response;
		}
	}
}
