package com.example.biblioteca.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmprestimoCadastroDTO {

	@Schema(description = "ID do usuario que esta emprestando")
	@NotNull
	private Integer usuarioId;

	@Schema(description = "ID do livro a ser emprestado")
	@NotNull
	private Integer livroId;

	@Schema(description = "Dias para devolucao (padrao 14)")
	private Integer diasParaDevolucao = 14;
}
