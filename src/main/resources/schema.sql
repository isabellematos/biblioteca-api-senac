/*CREATE TABLE detalhesAutor (
	id integer primary key not null,
	dataNascimento date not null,
	nacionalidade varchar(50) not null
);

CREATE TABLE autor (
	id integer primary key not null,
	nome varchar(100) not null,
	detalhesAutorId integer not null,
	
    foreign key (detalhesAutorId) references detalhesAutor(id)
);

CREATE TABLE editora (
	id integer primary key not null,
	nome varchar(100) not null
);

CREATE TABLE categoria (
	id integer primary key not null,
	nome varchar(100) not null
);

CREATE TABLE genero (
	id integer primary key not null,
	nome varchar(100) not null
);

CREATE TABLE livro (
	id integer primary key not null,
	nome varchar(500) not null,
	descricao varchar(500) not null,
	isbn varchar(20) unique not null,
	numPaginas integer not null,
	editoraId integer not null,
	generoId integer not null,
	
	foreign key (editoraId) references editora(id),
	foreign key (generoId) references genero(id)
);

CREATE TABLE livro_autor (
	livroId integer not null,
	autorId integer not null,
	
	foreign key (livroId) references livro(id),
	foreign key (autorId) references autor(id)
);*/
