package com.example.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.Genero;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Integer>{
	Page<Genero> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
