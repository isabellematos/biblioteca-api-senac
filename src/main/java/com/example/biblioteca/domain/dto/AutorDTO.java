package com.example.biblioteca.domain.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import com.example.biblioteca.domain.Autor;
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
public class AutorDTO extends RepresentationModel<AutorDTO> {
	public AutorDTO(Autor a) {
		this.setId(a.getId());
		this.setNome(a.getNome());
		this.setDetalhesAutor(new DetalhesAutorDTO(a.getDetalhesAutor()));
		
		if(a.getLivros() != null) {
			List<String> nomes = new ArrayList<String>();
			
			for(Livro l : a.getLivros()) {
				nomes.add(l.getNome());
			}
			
			this.setLivrosNome(nomes);
		}
		
	}

	public AutorDTO(Integer id, String nome, List<String> retornarLivros) {
		this.setId(id);
		this.setNome(nome);
		this.setLivrosNome(retornarLivros);
	}

	@Schema(description = "ID do autor", hidden = true)
	private Integer id;

	@Schema(description = "Nome do autor")
	@NotEmpty @NotNull 
	private String nome;
	
	@Schema(description = "Detalhes sobre o autor")
	@NotNull
	private DetalhesAutorDTO detalhesAutor;
	
	@Schema(required = false, hidden = true)
	private List<String> livrosNome;
}
