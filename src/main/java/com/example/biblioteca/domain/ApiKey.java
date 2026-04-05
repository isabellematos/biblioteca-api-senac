package com.example.biblioteca.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "api_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(unique = true, nullable = false)
	private String chave;

	@Column(nullable = false)
	private String descricao;

	@Column(nullable = false)
	private Boolean ativa = true;

	@Column(nullable = false)
	private LocalDateTime criadaEm = LocalDateTime.now();
}
