package com.example.biblioteca.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.domain.dto.UsuarioDTO;
import com.example.biblioteca.domain.enums.StatusLivro;
import com.example.biblioteca.repository.LivroRepository;
import com.example.biblioteca.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private LivroRepository livroRepository;

	public Usuario cadastrarUsuario(UsuarioDTO dto) {
		Usuario usuario = new Usuario();
		usuario.setNome(dto.getNome());
		usuario.setEmail(dto.getEmail());
		usuario.setTelefone(dto.getTelefone());
		return usuarioRepository.save(usuario);
	}

	public Usuario retornarUsuarioId(Integer id) {
		return usuarioRepository.findById(id).get();
	}

	public Usuario atualizarUsuario(Integer id, UsuarioDTO dto) {
		Usuario usuario = usuarioRepository.findById(id).get();
		usuario.setNome(dto.getNome());
		usuario.setEmail(dto.getEmail());
		usuario.setTelefone(dto.getTelefone());
		return usuarioRepository.save(usuario);
	}

	public void deletarUsuario(Integer id) {
		usuarioRepository.deleteById(id);
	}

	public Page<UsuarioDTO> retornarTodosOsUsuarios(Pageable pageable) {
		return usuarioRepository.findAll(pageable).map(u -> new UsuarioDTO(u));
	}

	public Page<UsuarioDTO> buscarPorNome(String nome, Pageable pageable) {
		return usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable).map(u -> new UsuarioDTO(u));
	}

	public Usuario emprestarLivro(Integer usuarioId, Integer livroId) {
		Usuario usuario = usuarioRepository.findById(usuarioId).get();
		Livro livro = livroRepository.findById(livroId).get();

		livro.setStatus(StatusLivro.EMPRESTADO);
		livroRepository.save(livro);

		if (usuario.getLivrosEmprestados() == null) {
			usuario.setLivrosEmprestados(new ArrayList<>());
		}
		usuario.getLivrosEmprestados().add(livro);
		return usuarioRepository.save(usuario);
	}

	public Usuario devolverLivro(Integer usuarioId, Integer livroId) {
		Usuario usuario = usuarioRepository.findById(usuarioId).get();
		Livro livro = livroRepository.findById(livroId).get();

		livro.setStatus(StatusLivro.DISPONIVEL);
		livroRepository.save(livro);

		usuario.getLivrosEmprestados().removeIf(l -> l.getId().equals(livroId));
		return usuarioRepository.save(usuario);
	}
}
