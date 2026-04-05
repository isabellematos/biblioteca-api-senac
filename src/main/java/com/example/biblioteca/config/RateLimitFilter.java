package com.example.biblioteca.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class RateLimitFilter implements Filter {

	private static final int MAX_REQUESTS_PER_MINUTE = 60;
	private final ConcurrentHashMap<String, ClientRateInfo> clients = new ConcurrentHashMap<>();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String clientIp = httpRequest.getRemoteAddr();

		ClientRateInfo rateInfo = clients.compute(clientIp, (key, existing) -> {
			long now = System.currentTimeMillis();
			if (existing == null || now - existing.windowStart > 60_000) {
				return new ClientRateInfo(now, new AtomicInteger(1));
			}
			existing.count.incrementAndGet();
			return existing;
		});

		int remaining = MAX_REQUESTS_PER_MINUTE - rateInfo.count.get();

		httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
		httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));

		if (remaining < 0) {
			httpResponse.setStatus(429);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write("{\"erro\": \"Limite de requisições excedido. Tente novamente em breve.\"}");
			return;
		}

		chain.doFilter(request, response);
	}

	private static class ClientRateInfo {
		final long windowStart;
		final AtomicInteger count;

		ClientRateInfo(long windowStart, AtomicInteger count) {
			this.windowStart = windowStart;
			this.count = count;
		}
	}
}
