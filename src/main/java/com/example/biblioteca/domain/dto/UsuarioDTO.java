package com.example.biblioteca.domain.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsuarioDTO extends RepresentationModel<UsuarioDTO> {

	public UsuarioDTO(Usuario u) {
		this.id = u.getId();
		this.nome = u.getNome();
		this.email = u.getEmail();
		this.telefone = u.getTelefone();

		if (u.getLivrosEmprestados() != null) {
			List<String> nomes = new ArrayList<>();
			for (Livro l : u.getLivrosEmprestados()) {
				nomes.add(l.getNome());
			}
			this.livrosEmprestados = nomes;
		}
	}

	@Schema(description = "ID do usuario", hidden = true)
	private Integer id;

	@Schema(description = "Nome do usuario")
	@NotEmpty @NotNull
	private String nome;

	@Schema(description = "Email do usuario")
	@NotEmpty @NotNull @Email
	private String email;

	@Schema(description = "Telefone do usuario")
	@NotEmpty @NotNull
	private String telefone;

	@Schema(description = "Livros emprestados pelo usuario", hidden = true)
	private List<String> livrosEmprestados;
}
