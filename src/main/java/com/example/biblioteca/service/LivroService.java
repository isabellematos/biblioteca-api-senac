package com.example.biblioteca.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Autor;
import com.example.biblioteca.domain.Editora;
import com.example.biblioteca.domain.Genero;
import com.example.biblioteca.domain.Idioma;
import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.dto.LivroCadastroDTO;
import com.example.biblioteca.domain.dto.LivroDTO;
import com.example.biblioteca.domain.enums.StatusLivro;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.repository.EditoraRepository;
import com.example.biblioteca.repository.GeneroRepository;
import com.example.biblioteca.repository.IdiomaRepository;
import com.example.biblioteca.repository.LivroRepository;

@Service
public class LivroService {
	
	@Autowired
	private LivroRepository livroRepository;
	
	@Autowired
	private AutorRepository autorRepository;
	
	@Autowired
	private GeneroRepository generoRepository;
	
	@Autowired
	private EditoraRepository editoraRepository;
	
	@Autowired
	private IdiomaRepository idiomaRepository;
	

	public LivroDTO retornarLivroId(Integer id) {
		Livro l = livroRepository.findById(id).get();
		return new LivroDTO(l);
	}

	public Livro cadastrarLivro(LivroCadastroDTO l) {
		
		Editora e = editoraRepository.findById(l.getEditoraId()).get();
		Genero g = generoRepository.findById(l.getGeneroId()).get();
		Idioma i = idiomaRepository.findById(l.getIdiomaId()).get();
		
		List<Autor> autores = new ArrayList<Autor>();
		
		for(Integer id : l.getAutoresId()) {
			autores.add(autorRepository.findById(id).get());
		}
		
		Livro livro = new Livro(l);
		livro.setEditora(e);
		livro.setGenero(g);
		livro.setIdioma(i);
		livro.setAutores(autores);
		if (l.getStatus() != null) {
			livro.setStatus(l.getStatus());
		}
		return livroRepository.save(livro);
	}

	public Livro atualizarLivro(Integer id, LivroCadastroDTO l) {
		
		Editora e = editoraRepository.findById(l.getEditoraId()).get();
		Genero g = generoRepository.findById(l.getGeneroId()).get();
		Idioma i = idiomaRepository.findById(l.getIdiomaId()).get();
		
		List<Autor> autores = new ArrayList<Autor>();
		
		for(Integer idAutores : l.getAutoresId()) {
			autores.add(autorRepository.findById(idAutores).get());
		}
		
		Livro livro = livroRepository.findById(id).get();

		livro.setEditora(e);
		livro.setGenero(g);
		livro.setIdioma(i);
		livro.setAutores(autores);
		return livroRepository.save(livro);	
	}

	public void deletarLivroId(Integer id) {
		livroRepository.deleteById(id);
	}

	public Page<LivroDTO> retornarTodosOsLivros(Pageable pageable) {
		return livroRepository.findAll(pageable)
				.map(l -> new LivroDTO(l));
	}

	public Page<LivroDTO> buscarPorNome(String nome, Pageable pageable) {
		return livroRepository.findByNomeContainingIgnoreCase(nome, pageable)
				.map(l -> new LivroDTO(l));
	}

	public Page<LivroDTO> buscarPorStatus(StatusLivro status, Pageable pageable) {
		return livroRepository.findByStatus(status, pageable)
				.map(l -> new LivroDTO(l));
	}
}
