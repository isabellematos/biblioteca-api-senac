package com.example.biblioteca.domain.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import com.example.biblioteca.domain.Genero;
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
public class GeneroDTO extends RepresentationModel<GeneroDTO>{
	
	public GeneroDTO(Genero genero) {
		this.setId(genero.getId());
		this.setNome(genero.getNome());
		
		if (genero.getLivros() != null) {
			List<String> nomes = new ArrayList<String>();

			for (Livro l : genero.getLivros()) {
				nomes.add(l.getNome());
			}

			this.setLivrosNome(nomes);
		}
	}
	
	public GeneroDTO(String nome, List<String> livros) {
		this.setNome(nome);
		this.setLivrosNome(livros);
	}

	@Schema(description = "ID do autor", hidden = true)
	private Integer id;
	
	@Schema(description = "Nome do gênero")
	@NotEmpty @NotNull
	private String nome;
	
	@Schema(required = false, hidden = true)
	private List<String> livrosNome;
	
}
