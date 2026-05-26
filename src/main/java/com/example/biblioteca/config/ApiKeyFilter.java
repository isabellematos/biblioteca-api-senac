package com.example.biblioteca.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.biblioteca.domain.ApiKey;
import com.example.biblioteca.domain.enums.AccessLevel;
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
		String method = httpRequest.getMethod().toUpperCase();

		// Rotas publicas
		if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
				|| path.startsWith("/api-docs") || path.startsWith("/h2-console")
				|| path.startsWith("/index.html") || path.equals("/")) {
			chain.doFilter(request, response);
			return;
		}

		// GETs sao publicos
		if ("GET".equals(method) || "OPTIONS".equals(method)) {
			chain.doFilter(request, response);
			return;
		}

		// POST em /api-keys e publico (gerar chave)
		if ("POST".equals(method) && path.equals("/api-keys")) {
			chain.doFilter(request, response);
			return;
		}

		// Demais metodos exigem X-API-Key
		String keyValue = httpRequest.getHeader(API_KEY_HEADER);

		if (keyValue == null || keyValue.isBlank()) {
			sendError(httpResponse, 401, "Unauthorized",
					"Header X-API-Key ausente. Gere uma chave em POST /api-keys.", path);
			return;
		}

		Optional<ApiKey> optKey = apiKeyService.findByKeyValue(keyValue);
		if (optKey.isEmpty()) {
			sendError(httpResponse, 401, "Unauthorized",
					"Chave de API invalida ou revogada.", path);
			return;
		}

		ApiKey apiKey = optKey.get();
		AccessLevel level = apiKey.getAccessLevel();

		// READ so pode GET (ja liberado acima), entao se chegou aqui com READ e escrita
		if (level == AccessLevel.READ) {
			sendError(httpResponse, 403, "Forbidden",
					"Nivel de acesso insuficiente. Chave READ permite apenas leitura (GET).", path);
			return;
		}

		// DELETE em /api-keys/{id} exige ADMIN
		if ("DELETE".equals(method) && path.matches("/api-keys/\\d+")) {
			if (level != AccessLevel.ADMIN) {
				sendError(httpResponse, 403, "Forbidden",
						"Nivel de acesso insuficiente. Apenas chaves ADMIN podem revogar outras chaves.", path);
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private void sendError(HttpServletResponse response, int status, String error, String message, String path)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.getWriter().write(String.format(
				"{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
				LocalDateTime.now(), status, error, message, path));
	}
}
