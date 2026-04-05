package com.example.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Genero;
import com.example.biblioteca.domain.dto.GeneroDTO;
import com.example.biblioteca.repository.GeneroRepository;

@Service
public class GeneroService {
	
	@Autowired
	private GeneroRepository generoRepository;

	public Genero retornargeneroId(Integer id) {
		return generoRepository.findById(id).get();
	}

	public Genero cadastrarGenero(GeneroDTO g) {
		Genero genero = new Genero(g);
		return generoRepository.save(genero);
	}

	public Genero atualizarGenero(Integer id, GeneroDTO g) {
		Genero genero = generoRepository.findById(id).get();
		genero.setNome(g.getNome());
		generoRepository.save(genero);
		
		return genero;
	}

	public void deletarGeneroId(Integer id) {
		generoRepository.deleteById(id);
		
	}

	public Page<GeneroDTO> retornarTodosOsGeneros(Pageable pageable) {
		return generoRepository.findAll(pageable)
				.map(g -> new GeneroDTO(g.getId(), g.getNome(), g.retornarLivros(g)));
	}

	public Page<GeneroDTO> buscarPorNome(String nome, Pageable pageable) {
		return generoRepository.findByNomeContainingIgnoreCase(nome, pageable)
				.map(g -> new GeneroDTO(g));
	}
}
