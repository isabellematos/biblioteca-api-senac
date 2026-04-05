package com.example.biblioteca.domain.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

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
public class LivroDTO extends RepresentationModel<LivroDTO>{
	public LivroDTO(Livro l) {
		this.setId(l.getId());
		this.setNome(l.getNome());
		this.setDescricao(l.getDescricao());
		this.setISBN(l.getISBN());
		this.setGeneroNome(l.getGenero().getNome());
		this.setEditoraNome(l.getEditora().getNome());
		this.setIdiomaNome(l.getIdioma().getNome());
		this.setNumPaginas(l.getNumPaginas());
		this.setStatus(l.getStatus());
		
		if (l.getAutores() != null) {
			List<String> autores = new ArrayList<String>();

			for (Autor a : l.getAutores()) {
				autores.add(a.getNome());
			}

			this.setAutoresNomes(autores);
		}
	}
	
	@Schema(description = "ID do livro", hidden = true)
	private Integer id;
	
	@Schema(description = "Nome do livro")
	private String nome;
	
	@Schema(description = "Descrição do livro")
    private String descricao;
	
	@Schema(description = "ISBN do livro")
	private String ISBN;
	
	@Schema(description = "Número de páginas do livro")
	private Integer numPaginas;
	
	@Schema(description = "Gênero do livro")
	private String generoNome;
	
	@Schema(description = "Editora do livro")
	private String editoraNome;
	
	@Schema(description = "Idioma do livro")
	private String idiomaNome;
	
	@Schema(description = "Status do livro")
	private StatusLivro status;
	
	@Schema(description = "Autores do livro")
	private List<String> autoresNomes;
}


