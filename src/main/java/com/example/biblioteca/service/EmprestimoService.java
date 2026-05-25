package com.example.biblioteca.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.domain.dto.EmprestimoCadastroDTO;
import com.example.biblioteca.domain.dto.EmprestimoDTO;
import com.example.biblioteca.domain.enums.StatusEmprestimo;
import com.example.biblioteca.domain.enums.StatusLivro;
import com.example.biblioteca.exception.ConflictException;
import com.example.biblioteca.repository.EmprestimoRepository;
import com.example.biblioteca.repository.LivroRepository;
import com.example.biblioteca.repository.UsuarioRepository;

@Service
public class EmprestimoService {

	@Autowired
	private EmprestimoRepository emprestimoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private LivroRepository livroRepository;

	public Emprestimo realizarEmprestimo(EmprestimoCadastroDTO dto) {
		Usuario usuario = usuarioRepository.findById(dto.getUsuarioId()).get();
		Livro livro = livroRepository.findById(dto.getLivroId()).get();

		if (livro.getStatus() != StatusLivro.DISPONIVEL) {
			throw new ConflictException("Livro nao esta disponivel para emprestimo. Status atual: " + livro.getStatus());
		}

		livro.setStatus(StatusLivro.EMPRESTADO);
		livroRepository.save(livro);

		Emprestimo emprestimo = new Emprestimo();
		emprestimo.setUsuario(usuario);
		emprestimo.setLivro(livro);
		emprestimo.setDataEmprestimo(LocalDate.now());
		emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(dto.getDiasParaDevolucao() != null ? dto.getDiasParaDevolucao() : 14));
		emprestimo.setStatus(StatusEmprestimo.ATIVO);

		return emprestimoRepository.save(emprestimo);
	}

	public Emprestimo realizarDevolucao(Integer emprestimoId) {
		Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId).get();

		emprestimo.setDataDevolucaoReal(LocalDate.now());
		emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);

		Livro livro = emprestimo.getLivro();
		livro.setStatus(StatusLivro.DISPONIVEL);
		livroRepository.save(livro);

		return emprestimoRepository.save(emprestimo);
	}

	public EmprestimoDTO retornarEmprestimoId(Integer id) {
		return new EmprestimoDTO(emprestimoRepository.findById(id).get());
	}

	public Page<EmprestimoDTO> retornarTodosOsEmprestimos(Pageable pageable) {
		return emprestimoRepository.findAll(pageable).map(e -> new EmprestimoDTO(e));
	}

	public Page<EmprestimoDTO> buscarPorUsuario(Integer usuarioId, Pageable pageable) {
		return emprestimoRepository.findByUsuarioId(usuarioId, pageable).map(e -> new EmprestimoDTO(e));
	}

	public Page<EmprestimoDTO> buscarPorStatus(StatusEmprestimo status, Pageable pageable) {
		return emprestimoRepository.findByStatus(status, pageable).map(e -> new EmprestimoDTO(e));
	}

	public void deletarEmprestimo(Integer id) {
		emprestimoRepository.deleteById(id);
	}
}
