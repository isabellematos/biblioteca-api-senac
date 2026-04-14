package com.example.biblioteca.domain.dto;

import java.time.LocalDateTime;

import com.example.biblioteca.domain.DetalhesAutor;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DetalhesAutorDTO {
	
	public DetalhesAutorDTO(DetalhesAutor da) {
		this.nacionalidade = da.getNacionalidade();
	}
	
	@Schema(description = "Nacionalidade do autor")
	@NotEmpty @NotNull
	private String nacionalidade;
}
