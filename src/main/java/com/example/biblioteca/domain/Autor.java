package com.example.biblioteca.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.biblioteca.domain.dto.AutorDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "autor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Autor {
	
	public Autor(AutorDTO a) {
		this.setNome(a.getNome());
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NotEmpty @NotNull
	private String nome;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "detalhesAutorId", referencedColumnName = "id")
    @NotNull
	private DetalhesAutor detalhesAutor;
	
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "autores")
	private List<Livro> livros;

	public List<String> retornarLivros(Autor a) {
		List<String> nomes = new ArrayList<String>();
		
		for(Livro l : a.getLivros()) {
			nomes.add(l.getNome());
		}
		
		return nomes;
	}
}
