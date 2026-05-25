package com.example.biblioteca.domain;

import java.time.LocalDate;

import com.example.biblioteca.domain.enums.StatusEmprestimo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "emprestimo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Emprestimo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "usuario_id")
	@NotNull
	private Usuario usuario;

	@ManyToOne
	@JoinColumn(name = "livro_id")
	@NotNull
	private Livro livro;

	@NotNull
	private LocalDate dataEmprestimo;

	@NotNull
	private LocalDate dataDevolucaoPrevista;

	private LocalDate dataDevolucaoReal;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private StatusEmprestimo status = StatusEmprestimo.ATIVO;
}
