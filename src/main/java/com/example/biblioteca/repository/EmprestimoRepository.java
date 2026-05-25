package com.example.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.domain.enums.StatusEmprestimo;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {
	Page<Emprestimo> findByUsuarioId(Integer usuarioId, Pageable pageable);
	Page<Emprestimo> findByStatus(StatusEmprestimo status, Pageable pageable);
}
