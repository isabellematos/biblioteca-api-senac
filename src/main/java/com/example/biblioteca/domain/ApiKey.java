package com.example.biblioteca.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.biblioteca.domain.enums.AccessLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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

	@Column(name = "key_value", unique = true, nullable = false, length = 64)
	private String keyValue;

	@Column(nullable = false, length = 100)
	private String owner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccessLevel accessLevel = AccessLevel.WRITE;

	@Column(nullable = false)
	private Boolean active = true;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (this.keyValue == null) {
			this.keyValue = UUID.randomUUID().toString();
		}
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}
