package com.example.biblioteca.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.biblioteca.domain.dto.LivroCadastroDTO;
import com.example.biblioteca.domain.dto.LivroDTO;
import com.example.biblioteca.domain.enums.StatusLivro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity

@Table(name = "livro")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Livro {
	
	public Livro (LivroCadastroDTO l) {
		this.nome = l.getNome();
		this.descricao = l.getDescricao();
		this.ISBN = l.getISBN();
		this.numPaginas = l.getNumPaginas();
	}
	
	public Livro(LivroDTO l) {
		this.nome = l.getNome();
		this.descricao = l.getDescricao();
		this.ISBN = l.getISBN();
		this.numPaginas = l.getNumPaginas();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NotEmpty @NotNull
	private String nome;
	
	@NotEmpty @NotNull
	private String descricao;
	
	@Column(unique = true)
	private String ISBN;
	
	@NotNull @Positive
	private Integer numPaginas;
	
	@ManyToOne
	@JoinColumn(name = "genero_id")
	@NotNull
	private Genero genero;
	
	@ManyToOne
	@JoinColumn(name = "editora_id")
	@NotNull
	private Editora editora;
	
	@ManyToOne
	@JoinColumn(name = "idioma_id")
	@NotNull
	private Idioma idioma;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private StatusLivro status = StatusLivro.DISPONIVEL;
	
	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
    	      name = "livro_autor", 
    	      joinColumns = @JoinColumn(name = "livroid"), 
    	      inverseJoinColumns = @JoinColumn(name = "autorId"))
	private List<Autor> autores;

	public List<String> retornarAutores(Livro l) {
		List<String> autores = new ArrayList<String>();
		
		for(Autor a : l.getAutores()) {
			autores.add(a.getNome());
		}
		
		return autores;
	}
}


