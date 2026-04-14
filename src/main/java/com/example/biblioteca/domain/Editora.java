package com.example.biblioteca.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.biblioteca.domain.dto.EditoraDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity

@Table(name = "editora")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Editora {
	
	public Editora(EditoraDTO e) {
		this.setNome(e.getNome());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NotEmpty @NotNull
	private String nome;
	
	@OneToMany(mappedBy = "editora")
	private List<Livro> livros;

	public List<String> retornarLivros(Editora e) {
		List<String> nomes = new ArrayList<String>();
		
		for(Livro l : e.getLivros()) {
			nomes.add(l.getNome());
		}
		
		return nomes;
	}

	
	
}
