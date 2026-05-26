package com.example.biblioteca.config;

import java.io.IOException;
import java.time.LocalDateTime;
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

	private static final int MAX_READ_PER_MINUTE = 10;
	private static final int MAX_WRITE_PER_MINUTE = 5;

	private final ConcurrentHashMap<String, ClientRateInfo> readBuckets = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, ClientRateInfo> writeBuckets = new ConcurrentHashMap<>();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String path = httpRequest.getRequestURI();

		// Isentar swagger, h2, actuator
		if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
				|| path.startsWith("/api-docs") || path.startsWith("/h2-console")
				|| path.startsWith("/actuator") || path.startsWith("/index.html") || path.equals("/")) {
			chain.doFilter(request, response);
			return;
		}

		String clientIp = httpRequest.getRemoteAddr();
		String method = httpRequest.getMethod().toUpperCase();
		boolean isRead = "GET".equals(method);

		ConcurrentHashMap<String, ClientRateInfo> buckets = isRead ? readBuckets : writeBuckets;
		int maxRequests = isRead ? MAX_READ_PER_MINUTE : MAX_WRITE_PER_MINUTE;

		ClientRateInfo rateInfo = buckets.compute(clientIp, (key, existing) -> {
			long now = System.currentTimeMillis();
			if (existing == null || now - existing.windowStart > 60_000) {
				return new ClientRateInfo(now, new AtomicInteger(1));
			}
			existing.count.incrementAndGet();
			return existing;
		});

		int remaining = maxRequests - rateInfo.count.get();
		long secondsUntilReset = Math.max(1, 60 - (System.currentTimeMillis() - rateInfo.windowStart) / 1000);

		httpResponse.setHeader("X-Rate-Limit-Remaining", String.valueOf(Math.max(0, remaining)));

		if (remaining < 0) {
			httpResponse.setHeader("Retry-After", String.valueOf(secondsUntilReset));
			httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(secondsUntilReset));
			httpResponse.setStatus(429);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write(String.format(
					"{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Limite de requisicoes excedido. Tente novamente em %d segundo(s).\",\"path\":\"%s\"}",
					LocalDateTime.now(), secondsUntilReset, path));
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
