package com.example.biblioteca.domain.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import com.example.biblioteca.domain.Editora;
import com.example.biblioteca.domain.Idioma;
import com.example.biblioteca.domain.Livro;

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
public class IdiomaDTO extends RepresentationModel<IdiomaDTO>{
	
	public IdiomaDTO(Idioma idioma) {
		this.setId(idioma.getId());
		this.setNome(idioma.getNome());
		
		if (idioma.getLivros() != null) {
			List<String> nomes = new ArrayList<String>();

			for (Livro l : idioma.getLivros()) {
				nomes.add(l.getNome());
			}

			this.setLivrosNome(nomes);
		}
	}
	
	public IdiomaDTO(String nome, List<String> livros) {
		this.setNome(nome);
		this.setLivrosNome(livros);
	}
	
	@Schema(description = "ID do idioma", hidden = true)

	private Integer id;

	@Schema(description = "Nome do idioma")
	@NotEmpty @NotNull
	private String nome;
	
	@Schema(description = "Livros do idioma", hidden = true)

	private List<String> livrosNome;
	
	public List<String> retornarAutores(Editora e){
		List<String> nomes = new ArrayList<String>();
		
		for(Livro l : e.getLivros()) {
			nomes.add(l.getNome());
		}
		
		return nomes;
	}
}
