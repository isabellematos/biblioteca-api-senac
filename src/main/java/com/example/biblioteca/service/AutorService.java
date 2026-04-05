package com.example.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Autor;
import com.example.biblioteca.domain.DetalhesAutor;
import com.example.biblioteca.domain.dto.AutorDTO;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.repository.DetalhesAutorRepository;

@Service
public class AutorService {

	@Autowired
	private AutorRepository autorRepository;
	
	@Autowired
	private DetalhesAutorRepository detalhesAutorRepository;
	
	public Autor cadastrarAutor(AutorDTO a) {
		DetalhesAutor da = new DetalhesAutor(a.getDetalhesAutor());
		DetalhesAutor daCad = detalhesAutorRepository.save(da);
		
		Autor autor = new Autor(a);
		autor.setDetalhesAutor(daCad);
		return autorRepository.save(autor);
	}

	public Autor atualizarAutor(Integer id, AutorDTO a) {
		
		Autor autor = autorRepository.findById(id).get();
		DetalhesAutor da = detalhesAutorRepository.findById(autor.getDetalhesAutor().getId()).get();
		
		da.setNacionalidade(a.getDetalhesAutor().getNacionalidade());
		
		DetalhesAutor daCad = detalhesAutorRepository.save(da);
		
		autor.setNome(a.getNome());
		autor.setDetalhesAutor(daCad);
		return autorRepository.save(autor);
	}

	public void deletarAutorId(Integer id) {
		autorRepository.deleteById(id);
	}

	public Page<AutorDTO> retornarTodosOsAutores(Pageable pageable) {
		return autorRepository.findAll(pageable)
				.map(a -> new AutorDTO(a.getId(), a.getNome(), a.retornarLivros(a)));
	}

	public Autor retornarAutorId(Integer id) {
		return autorRepository.findById(id).get();
	}

	public Page<AutorDTO> buscarPorNome(String nome, Pageable pageable) {
		return autorRepository.findByNomeContainingIgnoreCase(nome, pageable)
				.map(a -> new AutorDTO(a));
	}
}
