package com.example.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

	@Bean
	@Order(0)
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();

		// Origens permitidas — em producao substitua pelo dominio real do seu frontend
		config.addAllowedOriginPattern("*");

		// Metodos permitidos
		config.addAllowedMethod("GET");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("PATCH");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("OPTIONS");

		// Headers que o cliente pode enviar
		config.addAllowedHeader("Content-Type");
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("X-API-Key");
		config.addAllowedHeader("Idempotency-Key");
		config.addAllowedHeader("Authorization");
		config.addAllowedHeader("X-API-Version");

		// Headers que o cliente pode ler na resposta
		config.addExposedHeader("X-Rate-Limit-Remaining");
		config.addExposedHeader("X-Rate-Limit-Retry-After-Seconds");
		config.addExposedHeader("Retry-After");
		config.addExposedHeader("Location");

		// SEM allowCredentials — esta API usa X-API-Key no header, nao cookies
		// allowCredentials(true) seria inseguro com allowedOriginPattern("*")

		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsFilter(source);
	}
}
