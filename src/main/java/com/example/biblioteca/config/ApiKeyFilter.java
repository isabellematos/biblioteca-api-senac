package com.example.biblioteca.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.biblioteca.service.ApiKeyService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class ApiKeyFilter implements Filter {

	private static final String API_KEY_HEADER = "X-API-Key";

	@Autowired
	private ApiKeyService apiKeyService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Só exige API Key para requisições POST, PUT e DELETE
		String method = httpRequest.getMethod().toUpperCase();
		if ("GET".equals(method) || "OPTIONS".equals(method)) {
			chain.doFilter(request, response);
			return;
		}

		// Libera endpoint de geração de API Key (POST /api-keys)
		String path = httpRequest.getRequestURI();
		if (path.startsWith("/api-keys") || path.startsWith("/swagger-ui") 
				|| path.startsWith("/v3/api-docs") || path.startsWith("/api-docs")
				|| path.startsWith("/h2-console")) {
			chain.doFilter(request, response);
			return;
		}

		String apiKey = httpRequest.getHeader(API_KEY_HEADER);

		if (apiKey == null || !apiKeyService.isValidKey(apiKey)) {
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write("{\"erro\": \"API Key invalida ou ausente. Envie o header X-API-Key.\"}");
			return;
		}

		chain.doFilter(request, response);
	}
}
