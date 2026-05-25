package com.example.biblioteca.domain.dto;

import java.time.LocalDate;

import org.springframework.hateoas.RepresentationModel;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.domain.enums.StatusEmprestimo;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class EmprestimoDTO extends RepresentationModel<EmprestimoDTO> {

	public EmprestimoDTO(Emprestimo e) {
		this.id = e.getId();
		this.usuarioNome = e.getUsuario().getNome();
		this.usuarioId = e.getUsuario().getId();
		this.livroNome = e.getLivro().getNome();
		this.livroId = e.getLivro().getId();
		this.dataEmprestimo = e.getDataEmprestimo();
		this.dataDevolucaoPrevista = e.getDataDevolucaoPrevista();
		this.dataDevolucaoReal = e.getDataDevolucaoReal();
		this.status = e.getStatus();
	}

	@Schema(description = "ID do emprestimo", hidden = true)
	private Integer id;

	@Schema(description = "Nome do usuario")
	private String usuarioNome;

	@Schema(description = "ID do usuario")
	private Integer usuarioId;

	@Schema(description = "Nome do livro")
	private String livroNome;

	@Schema(description = "ID do livro")
	private Integer livroId;

	@Schema(description = "Data do emprestimo")
	private LocalDate dataEmprestimo;

	@Schema(description = "Data prevista para devolucao")
	private LocalDate dataDevolucaoPrevista;

	@Schema(description = "Data real da devolucao")
	private LocalDate dataDevolucaoReal;

	@Schema(description = "Status do emprestimo")
	private StatusEmprestimo status;
}
