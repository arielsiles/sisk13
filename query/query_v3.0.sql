/** 11.04.2019 Para Modulo de produccion 2 **/
delete from costosindirectos where idcostosindirectos = 0;
delete from productoprocesado where idproductoprocesado = 550;
delete from productoprocesado where idproductoprocesado = 552;
delete from productoprocesado where idproductoprocesado = 551;

alter table costosindirectos add foreign key (idperiodocostoindirecto) references periodocostoindirecto(idperiodocostoindirecto);
alter table ingredienteproduccion add foreign key (idcomposicionproducto) references composicionproducto(idcomposicionproducto);

update metaproductoproduccion m set m.`cod_art` = 155 where m.`idmetaproductoproduccion` = 161;
update metaproductoproduccion m set m.`cod_art` = 156 where m.`idmetaproductoproduccion` = 162;
update metaproductoproduccion m set m.`cod_art` = 158 where m.`idmetaproductoproduccion` = 164;
update metaproductoproduccion m set m.`cod_art` = 159 where m.`idmetaproductoproduccion` = 165;
update metaproductoproduccion m set m.`cod_art` = 161 where m.`idmetaproductoproduccion` = 170;
update metaproductoproduccion m set m.`cod_art` = 160 where m.`idmetaproductoproduccion` = 166;

delete from metaproductoproduccion where idmetaproductoproduccion = 560;
delete from productoprocesado where idproductoprocesado = 560;

alter table metaproductoproduccion modify column cod_art varchar(6) not null;
-- alter table inv_articulos add unique (cod_art);
alter table productoprocesado add foreign key (idproductoprocesado) references metaproductoproduccion(idmetaproductoproduccion);
alter table insumoproduccion add foreign key (idinsumoproduccion) references metaproductoproduccion(idmetaproductoproduccion);
alter table materialproduccion add foreign key (idmaterialproduccion) references metaproductoproduccion(idmetaproductoproduccion);
alter table composicionproducto add foreign key (idproductoprocesado) references productoprocesado(idproductoprocesado);
alter table ingredienteproduccion add foreign key (idmetaproductoproduccion) references metaproductoproduccion(idmetaproductoproduccion);

-- ALTER TABLE metaproductoproduccion ADD constraint FK_ART_METAPROD FOREIGN KEY (no_cia,cod_art) REFERENCES inv_articulos(no_cia,cod_art); ERROR
-- alter table metaproductoproduccion add constraint FK_INVARTICULOMETAPRODUCTO FOREIGN KEY (cod_art, no_cia) REFERENCES inv_articulos(cod_art, no_cia); ERROR

drop table productoproducido;
drop table insumosprod;
drop table produccion;
drop table tanqueprod;
drop table insumoformula;
drop table formulacion;

create table pr_formula (
	idformula bigint(20) not null,
	nombre varchar(100) not null,
	capacidad decimal(16, 2) not null,
	activo int(1),
	version bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idformula)
);

create table pr_insumoformula(
	idinsumoformula bigint(20) not null,
	cantidad decimal(20, 6) not null,
	cod_art varchar(6) not null,
	idformula bigint(20) not null,
	version bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idinsumoformula)
);

alter table pr_insumoformula add foreign key (idformula) references pr_formula(idformula);

create table pr_planprod (
	idplanprod bigint(20) not null,
	nombre varchar(100),
	fecha date not null,
	primary key (idplanprod)
);

create table pr_tanque(
	idtanque bigint(20) not null,
	nombre varchar(255) not null,
	capacidad int not null,
	idunidadmedida bigint(20) not null,
	primary key (idtanque)
);

alter table unidadmedidaproduccion add primary key(idunidadmedidaproduccion);
alter table tanque add foreign key(idunidadmedida) references unidadmedidaproduccion(idunidadmedidaproduccion);

create table produccion(
	idproduccion bigint(20) not null,
	codigo int,
	descripcion varchar(255),
	idformulacion bigint(20),
	idtanqueprod bigint(20),
	primary key (idproduccion)
);

alter table produccion add foreign key (idformulacion) references formulacion(idformulacion);
alter table produccion add foreign key (idtanqueprod)  references tanqueprod(idtanqueprod);

create table insumosprod (
	idinsumosprod bigint(20) not null,
	cod_art varchar(6),
	cantidad decimal(16, 6),
	costouni decimal(16, 6),
	idproduccion bigint(20) not null,
	primary key (idinsumosprod)
);

alter table insumosprod add foreign key (idproduccion) references produccion(idproduccion);

create table productoproducido(
	idproductoproducido bigint(20) not null,
	fecha date,
	cod_art varchar(6),
	cantidad decimal(16, 2),
	idproduccion bigint(20),
	primary key (idproductoproducido)
	
);

alter table productoproducido add foreign key (idproduccion) references produccion(idproduccion);





