-- ----------------------
/** query for v5.0.0 **/
-- ----------------------
/** 12.05.2020 **/
create table tipoventa (
	idtipoventa bigint(20) not null,
	nombre varchar(100),
	tipo varchar(100),
	version bigint(20),
	idcompania bigint(20),
	primary key (idtipoventa)
);

alter table tipoventa add foreign key (idcompania) references compania (idcompania);

alter table pedidos add column idtipoventa bigint(20) after IDCLIENTE;
alter table pedidos add foreign key (idtipoventa) references tipoventa (idtipoventa);

/** 15.05.2020 **/
alter table inv_articulos add column fix int(1) after vendible;
alter table inv_articulos add column sigla varchar(14) after fix;
alter table inv_articulos add column pos int(2) after sigla;

/** 16.05.2020 **/
insert into funcionalidad values (261, 'PRINTINVOICE', null, 1, 15, 'menu.customers.order.printInvoice', 1);
