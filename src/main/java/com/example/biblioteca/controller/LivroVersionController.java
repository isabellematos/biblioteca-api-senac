package com.example.biblioteca.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.biblioteca.domain.dto.LivroDTO;
import com.example.biblioteca.service.LivroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/livro/versioned")
@Tag(name = "Livro (Versionado)", description = "Endpoints com versionamento via header X-API-Version")
public class LivroVersionController {

	@Autowired
	private LivroService livroService;

	@Operation(summary = "Encontra livro por ID (versionado via header X-API-Version)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Livro encontrado"),
			@ApiResponse(responseCode = "400", description = "Versão inválida"),
			@ApiResponse(responseCode = "404", description = "Livro não encontrado"),
	})
	@GetMapping("/{id}")
	public ResponseEntity<?> retornarLivroId(
			@Parameter(description = "ID do livro") @PathVariable("id") Integer id,
			@Parameter(description = "Versão da API (1 ou 2)") @RequestHeader(value = "X-API-Version", defaultValue = "1") String version) {

		LivroDTO livroDTO = livroService.retornarLivroId(id);

		if ("2".equals(version)) {
			// V2: retorna formato resumido com metadados extras
			Map<String, Object> response = new HashMap<>();
			response.put("id", livroDTO.getId());
			response.put("titulo", livroDTO.getNome());
			response.put("autor", livroDTO.getAutoresNomes());
			response.put("status", livroDTO.getStatus());
			response.put("paginas", livroDTO.getNumPaginas());
			response.put("versao", "v2");
			return ResponseEntity.ok(response);
		}

		// V1: retorna formato completo com HATEOAS
		EntityModel<LivroDTO> resource = EntityModel.of(livroDTO);
		resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LivroVersionController.class).retornarLivroId(id, "1")).withSelfRel());
		return ResponseEntity.ok(resource);
	}

	@Operation(summary = "Lista todos os livros (versionado via header X-API-Version)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista retornada"),
			@ApiResponse(responseCode = "400", description = "Versão inválida"),
	})
	@GetMapping("/all")
	public ResponseEntity<Page<LivroDTO>> retornarTodosOsLivros(
			@ParameterObject Pageable pageable,
			@Parameter(description = "Versão da API (1 ou 2)") @RequestHeader(value = "X-API-Version", defaultValue = "1") String version) {
		return ResponseEntity.ok(livroService.retornarTodosOsLivros(pageable));
	}
}
