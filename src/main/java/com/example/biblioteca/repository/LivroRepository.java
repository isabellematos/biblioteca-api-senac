package com.example.biblioteca.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.enums.StatusLivro;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Integer>{
	Page<Livro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
	Page<Livro> findByStatus(StatusLivro status, Pageable pageable);

	@Query("SELECT l FROM Livro l WHERE l.genero.nome = :generoNome")
	Page<Livro> findByGeneroNome(@Param("generoNome") String generoNome, Pageable pageable);
}
