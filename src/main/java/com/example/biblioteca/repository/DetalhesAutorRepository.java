package com.example.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.biblioteca.domain.DetalhesAutor;

@Repository
public interface DetalhesAutorRepository extends JpaRepository<DetalhesAutor, Integer>{

}
