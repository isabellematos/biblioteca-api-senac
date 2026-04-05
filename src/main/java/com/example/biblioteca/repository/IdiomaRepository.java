package com.example.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.Idioma;

@Repository
public interface IdiomaRepository extends JpaRepository<Idioma, Integer>{
	Page<Idioma> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
