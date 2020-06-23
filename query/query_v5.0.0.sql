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

/** 11.06.2020 **/
alter table personacliente modify column ESPERSONA int(1);
alter table personacliente modify column con_factura int(11) not null default 1;

select max(a.`idpersonacliente`)+1 from personacliente a;
insert into secuencia values ('personacliente', (select max(idpersonacliente)+1 from personacliente));

/** 17.06.2020 **/
alter table categoriacliente add primary key (idcategoriacliente);

create table precioarticulo (
	idprecioarticulo 	bigint(20) not null,
	cod_art 		varchar(6) not null,
	precio 			decimal(12, 2) not null,
	idcategoriacliente 	bigint(20) not null,
	no_cia 			varchar(2) not null,
	version 		bigint(20) not null,
	idcompania 		bigint(20) not null,
	primary key (idprecioarticulo)
);

alter table precioarticulo add foreign key (idcategoriacliente) references categoriacliente (idcategoriacliente);
alter table precioarticulo add foreign key (idcompania) references compania (idcompania);

alter table personacliente add column idcategoriacliente bigint(20) after idtipocliente;
alter table personacliente add foreign key (idcategoriacliente) references categoriacliente (idcategoriacliente);

/** 18.06.2020 **/
-- Configurando articulos para ventas, fijando precio por defecto
-- Productos comercial
update inv_articulos set 	fix = 1, sigla = 	'CHI 160'	, pos = 	3	, precio_venta=	1.30	, vendible = 'S'	 where cod_art = 	667	;
update inv_articulos set 	fix = 1, sigla = 	'CHI 1L'	, pos = 	2	, precio_venta=	7	, vendible = 'S'	 where cod_art = 	119	;
update inv_articulos set 	fix = 1, sigla = 	'ILFDU 120'	, pos = 	4	, precio_venta=	0.42	, vendible = 'S'	 where cod_art = 	589	;
update inv_articulos set 	fix = 1, sigla = 	'ILFMA 120'	, pos = 	5	, precio_venta=	0.42	, vendible = 'S'	 where cod_art = 	588	;
update inv_articulos set 	fix = 1, sigla = 	'ILFNA 120'	, pos = 	6	, precio_venta=	0.42	, vendible = 'S'	 where cod_art = 	606	;
update inv_articulos set 	fix = 1, sigla = 	'ILFPI 120'	, pos = 	7	, precio_venta=	0.42	, vendible = 'S'	 where cod_art = 	594	;
update inv_articulos set 	fix = 1, sigla = 	'LUHT 950'	, pos = 	1	, precio_venta=	5.5	, vendible = 'S'	 where cod_art = 	118	;
update inv_articulos set 	fix = 1, sigla = 	'YFR 2L'	, pos = 	8	, precio_venta=	14.5	, vendible = 'S'	 where cod_art = 	131	;
update inv_articulos set 	fix = 1, sigla = 	'YDU 2L'	, pos = 	9	, precio_venta=	14.5	, vendible = 'S'	 where cod_art = 	130	;
update inv_articulos set 	fix = 1, sigla = 	'YCO 2L'	, pos = 	10	, precio_venta=	14.5	, vendible = 'S'	 where cod_art = 	129	;
update inv_articulos set 	fix = 1, sigla = 	'YMO 2L'	, pos = 	11	, precio_venta=	14.5	, vendible = 'S'	 where cod_art = 	132	;
update inv_articulos set 	fix = 1, sigla = 	'YCH 2L'	, pos = 	12	, precio_venta=	14.5	, vendible = 'S'	 where cod_art = 	128	;
update inv_articulos set 	fix = 1, sigla = 	'YFR 80ML'	, pos = 	13	, precio_venta=	0.55	, vendible = 'S'	 where cod_art = 	143	;
update inv_articulos set 	fix = 1, sigla = 	'FRUTA 1L'	, pos = 	14	, precio_venta=	12.5	, vendible = 'S'	 where cod_art = 	808	;
update inv_articulos set 	fix = 1, sigla = 	'FRUTA 750'	, pos = 	15	, precio_venta=	11.5	, vendible = 'S'	 where cod_art = 	135	;

update inv_articulos set precio_venta =	35, vendible = 'S'	 where cod_art = 148;
update inv_articulos set precio_venta =	30, vendible = 'S'	 where cod_art = 150;
update inv_articulos set precio_venta =	30, vendible = 'S'	 where cod_art = 151;


-- Productos categoria A
insert into precioarticulo values (	1	,	118	,	5	,	1	, '01', 0, 1);
insert into precioarticulo values (	2	,	119	,	6.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	3	,	667	,	1.05	,	1	, '01', 0, 1);
insert into precioarticulo values (	4	,	589	,	0.38	,	1	, '01', 0, 1);
insert into precioarticulo values (	5	,	588	,	0.38	,	1	, '01', 0, 1);
insert into precioarticulo values (	6	,	606	,	0.38	,	1	, '01', 0, 1);
insert into precioarticulo values (	7	,	594	,	0.38	,	1	, '01', 0, 1);
insert into precioarticulo values (	8	,	131	,	13.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	9	,	130	,	13.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	10	,	129	,	13.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	11	,	132	,	13.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	12	,	128	,	13.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	13	,	138	,	0.46	,	1	, '01', 0, 1);
insert into precioarticulo values (	14	,	808	,	11.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	15	,	135	,	10.5	,	1	, '01', 0, 1);
insert into precioarticulo values (	16	,	148	,	35	,	1	, '01', 0, 1);
insert into precioarticulo values (	17	,	151	,	30	,	1	, '01', 0, 1);
insert into precioarticulo values (	18	,	150	,	30	,	1	, '01', 0, 1);

-- Productos categoria B
insert into precioarticulo values (	19	,	118	,	5.5	,	2	, '01', 0, 1);
insert into precioarticulo values (	20	,	119	,	7	,	2	, '01', 0, 1);


-- 19.06.2020
create table distribuidor (
	iddistribuidor 		bigint(20) not null,
	descripcion 		varchar(100) null,
	idcompania 		bigint(20) not null,
	primary key (iddistribuidor)
);

alter table distribuidor add foreign key (iddistribuidor) references persona (idpersona);
alter table distribuidor add foreign key (idcompania) references compania (idcompania);

insert into funcionalidad values (263, 'DISTRIBUTOR', null, 1, 15, 'Functionality.customers.distributor', 1);





