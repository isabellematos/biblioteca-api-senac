package com.example.biblioteca.domain;

import java.time.LocalDateTime;

import com.example.biblioteca.domain.dto.DetalhesAutorDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "detalhes_autor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DetalhesAutor {
	
	public DetalhesAutor(DetalhesAutorDTO da) {
		this.setNacionalidade(da.getNacionalidade());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NotEmpty @NotNull
	private String nacionalidade;
}
