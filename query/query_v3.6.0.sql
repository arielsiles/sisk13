/** 25.07.2019 **/
create table pr_prodafectado(
	idprodafectado bigint(20) not null,
	idinsumo bigint(20) not null,
	idproducto bigint(20) not null,
	version bigint(20) not null,
	idcompania bigint(20) not null,
	primary key(idprodafectado)	
);

alter table pr_prodafectado add foreign key (idinsumo) references pr_insumo (idinsumo);
alter table pr_prodafectado add foreign key (idproducto) references pr_producto (idproducto);
alter table pr_prodafectado add foreign key (idcompania) references compania (idcompania);

alter table sf_tmpenc add column ndoc varchar(20) null;