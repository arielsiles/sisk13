-- ----------------------
/** query for v5.0.3 **/
-- ----------------------

-- 14.07.2020
create table garante (
	idgarante bigint(20) not null,
	idsocio bigint(20) not null,
	primary key (idgarante)
);

alter table garante add foreign key (idsocio) references socio (idsocio);