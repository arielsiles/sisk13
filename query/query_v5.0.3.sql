-- ----------------------
/** query for v5.0.3 **/
-- ----------------------

-- 14.07.2020
create table garante (
	idgarante bigint(20) not null,
	idsocio bigint(20) not null,
	descripcion varchar(100),
	version bigint(20),
	idcompania bigint(20),
	primary key (idgarante)
);

alter table garante add foreign key (idsocio) references socio (idsocio);
alter table garante add foreign key (idcompania) references compania (idcompania);

create table documentogarante(
	iddocumentogarante bigint(20) not null,
	descripcion varchar(255),
	idgarante bigint(20) not null,
	version bigint(20),
	idcompania bigint(20),
	primary key (iddocumentogarante)
);

alter table documentogarante add foreign key (idgarante) references garante (idgarante);
alter table documentogarante add foreign key (idcompania) references compania (idcompania);

