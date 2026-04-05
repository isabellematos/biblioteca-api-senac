package com.example.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Genero;
import com.example.biblioteca.domain.Idioma;
import com.example.biblioteca.domain.dto.GeneroDTO;
import com.example.biblioteca.domain.dto.IdiomaDTO;
import com.example.biblioteca.repository.IdiomaRepository;

@Service
public class IdiomaService {
	
	@Autowired
	private IdiomaRepository idiomaRepository;

	public Idioma retornaridiomaId(Integer id) {
		return idiomaRepository.findById(id).get();
	}

	public Idioma cadastrarIdioma(IdiomaDTO i) {
		Idioma idioma = new Idioma(i);
		return idiomaRepository.save(idioma);
	}

	public Idioma atualizarIdioma(Integer id, IdiomaDTO i) {
		Idioma idioma = idiomaRepository.findById(id).get();
		idioma.setNome(i.getNome());
		idiomaRepository.save(idioma);
		
		return idioma;
	}

	public void deletarIdiomaId(Integer id) {
		idiomaRepository.deleteById(id);
		
	}

	public Page<IdiomaDTO> retornarTodosOsIdiomas(Pageable pageable) {
		return idiomaRepository.findAll(pageable)
				.map(i -> new IdiomaDTO(i.getId(), i.getNome(), i.retornarLivros(i)));
	}

	public Page<IdiomaDTO> buscarPorNome(String nome, Pageable pageable) {
		return idiomaRepository.findByNomeContainingIgnoreCase(nome, pageable)
				.map(i -> new IdiomaDTO(i));
	}
}
