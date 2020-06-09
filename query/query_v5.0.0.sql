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

alter table pedidos add column tipoventa varchar(100) after idcliente;


alter table tipopedido add column tipo varchar(100);
update tipopedido set tipo = 'NORMAL' where IDTIPOPEDIDO = 1;
update tipopedido set tipo = 'TASTING' where IDTIPOPEDIDO = 2;
update tipopedido set tipo = 'REFRESHMENT' where IDTIPOPEDIDO = 3;
update tipopedido set tipo = 'REPLACEMENT' where IDTIPOPEDIDO = 4;
update tipopedido set tipo = 'MILK' where IDTIPOPEDIDO = 5;
update tipopedido set tipo = 'VETERINARY' where IDTIPOPEDIDO = 6;

/** 15.05.2020 **/
alter table inv_articulos add column fix int(1) after vendible;
alter table inv_articulos add column sigla varchar(14) after fix;
alter table inv_articulos add column pos int(2) after sigla;

/** 16.05.2020 **/
insert into funcionalidad values (261, 'PRINTINVOICE', null, 1, 15, 'menu.customers.order.printInvoice', 1);

/** 18.05.2020 **/
select max(a.`IDARTICULOSPEDIDO`)+100 from articulos_pedido a;
select max(a.`IDPEDIDOS`)+100 from pedidos a;

insert into secuencia values ('articulos_pedido', 400000); -- verificar secuencia
insert into secuencia values ('pedidos', 50000);	   -- verificar secuencia

/** 08.06.2020 **/
insert into funcionalidad values (262, 'CLIENT', null, 1, 15, 'Functionality.customers.client', 1);







