	package com.example.biblioteca.domain.dto;

import java.util.List;

import com.example.biblioteca.domain.Autor;
import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.enums.StatusLivro;

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
public class LivroCadastroDTO {
	public LivroCadastroDTO(Livro l) {
		this.setNome(l.getNome());
		this.setDescricao(l.getDescricao());
		this.setISBN(l.getISBN());
		this.setNumPaginas(l.getNumPaginas());
	}
	
	@Schema(description = "Nome do livro")
	private String nome;
	
	@Schema(description = "Descrição do livro")
    private String descricao;
	
	@Schema(description = "ISBN do livro")
	private String ISBN;
	
	@Schema(description = "Número de páginas do livro")
	private Integer numPaginas;
	
	@Schema(description = "ID do gênero do livro")
	private Integer generoId;
	
	@Schema(description = "ID da editora do livro")
	private Integer editoraId;
	
	@Schema(description = "ID do idioma do livro")
	private Integer idiomaId;
	
	@Schema(description = "Status do livro")
	private StatusLivro status;
	
	@Schema(description = "IDs dos autores do livro")
	private List<Integer> autoresId;
}


