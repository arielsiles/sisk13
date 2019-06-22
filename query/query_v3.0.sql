/** 11.04.2019 Para Modulo de produccion 2 **/
delete from costosindirectos where idcostosindirectos = 0;
delete from productoprocesado where idproductoprocesado = 550;
delete from productoprocesado where idproductoprocesado = 552;
delete from productoprocesado where idproductoprocesado = 551;

alter table costosindirectos add foreign key (idperiodocostoindirecto) references periodocostoindirecto(idperiodocostoindirecto);
alter table ingredienteproduccion add foreign key (idcomposicionproducto) references composicionproducto(idcomposicionproducto);

-- delete from costosindirectos;

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

-- ALTER TABLE metaproductoproduccion ADD constraint FK_ART_METAPROD FOREIGN KEY (no_cia,cod_art) REFERENCES inv_articulos(no_cia,cod_art); -- ERROR
-- alter table metaproductoproduccion add constraint FK_INVARTICULOMETAPRODUCTO FOREIGN KEY (cod_art, no_cia) REFERENCES inv_articulos(cod_art, no_cia); -- ERROR

drop table productoproducido;
drop table insumosprod;
drop table produccion;
drop table tanqueprod;
drop table insumoformula;
drop table formulacion;

drop table pr_insumoformula;
drop table pr_producido;
drop table pr_insumo;
drop table pr_produccion;
drop table pr_formula;
drop table pr_plan;
drop table pr_tanque;
drop table pr_producto;

create table pr_formula (
	idformula bigint(20) not null,
	nombre varchar(100) not null,
	capacidad decimal(16, 2) not null,
	activo int(1),
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,	
	primary key (idformula)
);

create table pr_insumoformula(
	idinsumoformula bigint(20) not null,
	cantidad decimal(20, 6) not null,
	cod_art varchar(6) not null,
	idformula bigint(20) not null,
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idinsumoformula)
);

alter table pr_insumoformula add foreign key (idformula) references pr_formula(idformula);

create table pr_plan (
	idplan bigint(20) not null,
	nombre varchar(100),
	fecha date not null unique,
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idplan)
);

create table pr_tanque(
	idtanque bigint(20) not null,
	nombre varchar(255) not null,
	capacidad decimal(16, 2) not null,
	codmed varchar(6),
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idtanque)
);


create table pr_produccion(
	idproduccion bigint(20) not null,
	codigo int,
	descripcion varchar(255),
	idformula bigint(20),
	idtanque bigint(20),
	idplan bigint(20),
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idproduccion)
);

alter table pr_produccion add foreign key (idformula) references pr_formula(idformula);
alter table pr_produccion add foreign key (idtanque)  references pr_tanque(idtanque);
alter table pr_produccion add foreign key (idplan) references pr_plan(idplan);

create table pr_insumo (
	idinsumo bigint(20) not null,
	cod_art varchar(6),
	cantidad decimal(16, 6),
	costouni decimal(16, 6),
	idproduccion bigint(20) not null,
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idinsumo)
);

alter table pr_insumo add foreign key (idproduccion) references pr_produccion(idproduccion);

create table pr_producto(
	idproducto bigint(20) not null,
	cod_art varchar(6),
	cantidad decimal(16, 2),
	idproduccion bigint(20),
	idplan bigint(20),
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idproducto)	
);
	
alter table pr_producto add foreign key (idproduccion) references pr_produccion(idproduccion);
alter table pr_producto add foreign key (idproduccion) references pr_produccion(idproduccion);
alter table pr_producto add foreign key (idplan) references pr_plan(idplan);

-- 
/** 19.04.2019 **/

insert into modulo(idmodulo, nombrerecurso, idcompania) values(10, 'production', 1);
insert into modulocompania values(1, 10, 1);
insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) values(251, 'PRODUCTIONPLAN', 10, 15, 'Functionality.production.productionPlan', 1);

-- INSERT INTO pr_tanque VALUES(1, "TANQUE 1 -  4000 LT", 4000, 'LT', 0, 1);
-- INSERT INTO pr_tanque VALUES(2, "TANQUE 2 -  6000 LT", 6000, 'LT', 0, 1);
-- INSERT INTO pr_tanque VALUES(3, "TANQUE 3 -  8000 LT", 8000, 'LT', 0, 1);

insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) values(252, 'PRODUCTION', 10, 15, 'Functionality.production', 1);

/** 23.04.2019 **/
alter table pr_produccion add column estado varchar(5) after codigo;
insert into gensecuencia values(18, 'PRODUCTION_CODE', 100, 1);


create table pr_categoria (
	idcategoria bigint(20) not null,
	nombre varchar(100),
	VERSION bigint(20) not null,
	idcompania bigint(20) not null,
	primary key (idcategoria)
);

alter table pr_formula add column idcategoria bigint(20) after activo;
ALTER TABLE pr_formula ADD FOREIGN KEY (idcategoria) REFERENCES pr_categoria(idcategoria);

INSERT INTO pr_categoria VALUES(1, 'LECHE NATURAL', 0, 1);
INSERT INTO pr_categoria VALUES(2, 'LECHE SABORIZADA', 0, 1);
INSERT INTO pr_categoria VALUES(3, 'YOGURT', 0, 1);
INSERT INTO pr_categoria VALUES(4, 'JUGO LACTEO', 0, 1);
INSERT INTO pr_categoria VALUES(5, 'QUESO', 0, 1);

/** 24.04.2019 **/
ALTER TABLE pr_insumo ADD COLUMN tipo VARCHAR(15) AFTER costouni;

/** 26.04.2019 **/
ALTER TABLE pr_insumo ADD COLUMN idinsumoformula BIGINT(20) AFTER tipo;
ALTER TABLE pr_insumo ADD COLUMN idproducto BIGINT(20) AFTER idproduccion;

ALTER TABLE pr_insumo ADD FOREIGN KEY (idinsumoformula) REFERENCES pr_insumoformula(idinsumoformula);
ALTER TABLE pr_insumo ADD FOREIGN KEY (idproducto) REFERENCES pr_producto(idproducto);

/** 29.04.2019 **/
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) 
VALUES(253, 'FORMULATION', 10, 15, 'Functionality.production.formulation', 1);

/** 01.05.2019 **/
ALTER TABLE pr_formula ADD COLUMN estado VARCHAR(3) AFTER nombre;

/** 02.05.2019 **/
ALTER TABLE pr_formula MODIFY COLUMN capacidad DECIMAL(16,2) NULL;
ALTER TABLE pr_insumoformula ADD COLUMN defecto INT(1) AFTER cod_art;

UPDATE pr_insumoformula p SET p.`defecto` = 0;
UPDATE pr_insumoformula p SET p.`defecto` = 1 WHERE p.`cod_art` = 1;

/** 03.05.2019 **/
CREATE TABLE pr_material(
	idmaterial BIGINT(20) NOT NULL,
	cod_art VARCHAR(6) NOT NULL,
	cod_art_mat VARCHAR(6) NOT NULL,
	descri VARCHAR(100),
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idmaterial)
);

ALTER TABLE pr_material ADD FOREIGN KEY (cod_art) REFERENCES inv_articulos(cod_art);
ALTER TABLE pr_material ADD FOREIGN KEY (cod_art_mat) REFERENCES inv_articulos(cod_art);

/** 04.05.2019 **/
ALTER TABLE pr_material ADD COLUMN flag_cant INT(1) AFTER descri;


