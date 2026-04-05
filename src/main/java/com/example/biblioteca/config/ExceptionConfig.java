package com.example.biblioteca.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionConfig {

	private Map<String, Object> buildError(HttpStatus status, String mensagem) {
		Map<String, Object> erro = new HashMap<>();
		erro.put("timestamp", LocalDateTime.now().toString());
		erro.put("status", status.value());
		erro.put("erro", status.getReasonPhrase());
		erro.put("mensagem", mensagem);
		return erro;
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, Object>> noSuchElement(NoSuchElementException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(buildError(HttpStatus.NOT_FOUND, "Recurso não encontrado"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> validationException(MethodArgumentNotValidException ex) {
		Map<String, Object> erro = buildError(HttpStatus.BAD_REQUEST, "Erro de validação");
		Map<String, String> campos = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
		erro.put("campos", campos);
		return ResponseEntity.badRequest().body(erro);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> illegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
				.body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage() != null ? ex.getMessage() : "Argumento inválido"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> generalException(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor"));
	}
}
