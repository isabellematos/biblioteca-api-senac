package com.example.biblioteca.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.biblioteca.exception.ConflictException;

@RestControllerAdvice
public class ExceptionConfig {

	private Map<String, Object> buildError(HttpStatus status, String message, String path) {
		Map<String, Object> erro = new HashMap<>();
		erro.put("timestamp", LocalDateTime.now().toString());
		erro.put("status", status.value());
		erro.put("error", status.getReasonPhrase());
		erro.put("message", message);
		erro.put("path", path);
		return erro;
	}

	private String getPath(WebRequest request) {
		String desc = request.getDescription(false);
		return desc.replace("uri=", "");
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, Object>> notFound(NoSuchElementException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(buildError(HttpStatus.NOT_FOUND, "Recurso nao encontrado.", getPath(request)));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, Object> erro = buildError(HttpStatus.BAD_REQUEST,
				"Erro de validacao nos campos da requisicao.", getPath(request));
		List<Map<String, Object>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> {
					Map<String, Object> f = new HashMap<>();
					f.put("field", e.getField());
					f.put("rejectedValue", e.getRejectedValue());
					f.put("message", e.getDefaultMessage());
					return f;
				}).collect(Collectors.toList());
		erro.put("fieldErrors", fieldErrors);
		return ResponseEntity.badRequest().body(erro);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> badJson(HttpMessageNotReadableException ex, WebRequest request) {
		return ResponseEntity.badRequest()
				.body(buildError(HttpStatus.BAD_REQUEST,
						"Corpo da requisicao invalido. Verifique o formato JSON.", getPath(request)));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> illegalArgument(IllegalArgumentException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(buildError(HttpStatus.UNPROCESSABLE_ENTITY,
						ex.getMessage() != null ? ex.getMessage() : "Violacao de regra de negocio.", getPath(request)));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<Map<String, Object>> conflict(ConflictException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(buildError(HttpStatus.CONFLICT, ex.getMessage(), getPath(request)));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> dataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(buildError(HttpStatus.CONFLICT,
						"Registro duplicado ou violacao de integridade.", getPath(request)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> general(Exception ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(buildError(HttpStatus.INTERNAL_SERVER_ERROR,
						"Erro interno inesperado.", getPath(request)));
	}
}
