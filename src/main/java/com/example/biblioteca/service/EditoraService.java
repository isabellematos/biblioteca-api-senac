package com.example.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Editora;
import com.example.biblioteca.domain.dto.EditoraDTO;
import com.example.biblioteca.repository.EditoraRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class EditoraService {

	@Autowired
	private EditoraRepository editoraRepository;
	
	public Editora cadastrarEditora(EditoraDTO e) {
		Editora editora = new Editora(e);
		return editoraRepository.save(editora);
	}
	
	public Editora atualizarEditora(Integer id, EditoraDTO e) {
		Editora editora = editoraRepository.findById(id).get();
		editora.setNome(e.getNome());
		editoraRepository.save(editora);
		
		return editora;
	}

	public Page<EditoraDTO> retornarTodosAsEditoras(Pageable pageable) {
		return editoraRepository.findAll(pageable)
				.map(e -> new EditoraDTO(e.getId(), e.getNome(), e.retornarLivros(e)));
	}

	public Editora retornarEditoraId(Integer id) {
		return editoraRepository.findById(id).get();
	}

	public void deletarEditoraId(Integer id) {
		editoraRepository.deleteById(id);
	}

	public Page<EditoraDTO> buscarPorNome(String nome, Pageable pageable) {
		return editoraRepository.findByNomeContainingIgnoreCase(nome, pageable)
				.map(e -> new EditoraDTO(e));
	}
}
