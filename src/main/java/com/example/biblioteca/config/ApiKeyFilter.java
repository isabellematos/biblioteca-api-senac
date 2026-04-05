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

		String path = httpRequest.getRequestURI();

		// Só exige API Key para requisições DELETE
		if (!"DELETE".equalsIgnoreCase(httpRequest.getMethod())) {
			chain.doFilter(request, response);
			return;
		}

		String apiKey = httpRequest.getHeader(API_KEY_HEADER);

		if (apiKey == null || !apiKeyService.isValidKey(apiKey)) {
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write("{\"erro\": \"API Key inválida ou ausente\"}");
			return;
		}

		chain.doFilter(request, response);
	}
}
