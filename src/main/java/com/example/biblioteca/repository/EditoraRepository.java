package com.example.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.Editora;

@Repository
public interface EditoraRepository extends JpaRepository<Editora, Integer> {
	Page<Editora> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
