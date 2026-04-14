package com.example.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.Autor;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Integer>{
	Page<Autor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
