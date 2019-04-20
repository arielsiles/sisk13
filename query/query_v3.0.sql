/** 11.04.2019 Para Modulo de produccion 2 **/
DELETE FROM costosindirectos WHERE idcostosindirectos = 0;
DELETE FROM productoprocesado WHERE idproductoprocesado = 550;
DELETE FROM productoprocesado WHERE idproductoprocesado = 552;
DELETE FROM productoprocesado WHERE idproductoprocesado = 551;

ALTER TABLE costosindirectos ADD FOREIGN KEY (idperiodocostoindirecto) REFERENCES periodocostoindirecto(idperiodocostoindirecto);
ALTER TABLE ingredienteproduccion ADD FOREIGN KEY (idcomposicionproducto) REFERENCES composicionproducto(idcomposicionproducto);

UPDATE metaproductoproduccion m SET m.`cod_art` = 155 WHERE m.`idmetaproductoproduccion` = 161;
UPDATE metaproductoproduccion m SET m.`cod_art` = 156 WHERE m.`idmetaproductoproduccion` = 162;
UPDATE metaproductoproduccion m SET m.`cod_art` = 158 WHERE m.`idmetaproductoproduccion` = 164;
UPDATE metaproductoproduccion m SET m.`cod_art` = 159 WHERE m.`idmetaproductoproduccion` = 165;
UPDATE metaproductoproduccion m SET m.`cod_art` = 161 WHERE m.`idmetaproductoproduccion` = 170;
UPDATE metaproductoproduccion m SET m.`cod_art` = 160 WHERE m.`idmetaproductoproduccion` = 166;

DELETE FROM metaproductoproduccion WHERE idmetaproductoproduccion = 560;
DELETE FROM productoprocesado WHERE idproductoprocesado = 560;

ALTER TABLE metaproductoproduccion MODIFY COLUMN cod_art VARCHAR(6) NOT NULL;
-- alter table inv_articulos add unique (cod_art);
ALTER TABLE productoprocesado ADD FOREIGN KEY (idproductoprocesado) REFERENCES metaproductoproduccion(idmetaproductoproduccion);
ALTER TABLE insumoproduccion ADD FOREIGN KEY (idinsumoproduccion) REFERENCES metaproductoproduccion(idmetaproductoproduccion);
ALTER TABLE materialproduccion ADD FOREIGN KEY (idmaterialproduccion) REFERENCES metaproductoproduccion(idmetaproductoproduccion);
ALTER TABLE composicionproducto ADD FOREIGN KEY (idproductoprocesado) REFERENCES productoprocesado(idproductoprocesado);
ALTER TABLE ingredienteproduccion ADD FOREIGN KEY (idmetaproductoproduccion) REFERENCES metaproductoproduccion(idmetaproductoproduccion);

-- ALTER TABLE metaproductoproduccion ADD constraint FK_ART_METAPROD FOREIGN KEY (no_cia,cod_art) REFERENCES inv_articulos(no_cia,cod_art); ERROR
-- alter table metaproductoproduccion add constraint FK_INVARTICULOMETAPRODUCTO FOREIGN KEY (cod_art, no_cia) REFERENCES inv_articulos(cod_art, no_cia); ERROR

DROP TABLE productoproducido;
DROP TABLE insumosprod;
DROP TABLE produccion;
DROP TABLE tanqueprod;
DROP TABLE insumoformula;
DROP TABLE formulacion;


DROP TABLE pr_insumoformula;
DROP TABLE pr_producido;
DROP TABLE pr_insumo;
DROP TABLE pr_produccion;
DROP TABLE pr_formula;
DROP TABLE pr_plan;
DROP TABLE pr_tanque;


CREATE TABLE pr_formula (
	idformula BIGINT(20) NOT NULL,
	nombre VARCHAR(100) NOT NULL,
	capacidad DECIMAL(16, 2) NOT NULL,
	activo INT(1),
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,	
	PRIMARY KEY (idformula)
);

CREATE TABLE pr_insumoformula(
	idinsumoformula BIGINT(20) NOT NULL,
	cantidad DECIMAL(20, 6) NOT NULL,
	cod_art VARCHAR(6) NOT NULL,
	idformula BIGINT(20) NOT NULL,
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idinsumoformula)
);

ALTER TABLE pr_insumoformula ADD FOREIGN KEY (idformula) REFERENCES pr_formula(idformula);

CREATE TABLE pr_plan (
	idplan BIGINT(20) NOT NULL,
	nombre VARCHAR(100),
	fecha DATE NOT NULL,
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idplan)
);

CREATE TABLE pr_tanque(
	idtanque BIGINT(20) NOT NULL,
	nombre VARCHAR(255) NOT NULL,
	capacidad DECIMAL(16, 2) NOT NULL,
	codmed VARCHAR(6),
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idtanque)
);


CREATE TABLE pr_produccion(
	idproduccion BIGINT(20) NOT NULL,
	codigo INT,
	descripcion VARCHAR(255),
	idformula BIGINT(20),
	idtanque BIGINT(20),
	idplan BIGINT(20),
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idproduccion)
);

ALTER TABLE pr_produccion ADD FOREIGN KEY (idformula) REFERENCES pr_formula(idformula);
ALTER TABLE pr_produccion ADD FOREIGN KEY (idtanque)  REFERENCES pr_tanque(idtanque);
ALTER TABLE pr_produccion ADD FOREIGN KEY (idplan) REFERENCES pr_plan(idplan);

CREATE TABLE pr_insumo (
	idinsumo BIGINT(20) NOT NULL,
	cod_art VARCHAR(6),
	cantidad DECIMAL(16, 6),
	costouni DECIMAL(16, 6),
	idproduccion BIGINT(20) NOT NULL,
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idinsumo)
);

ALTER TABLE pr_insumo ADD FOREIGN KEY (idproduccion) REFERENCES pr_produccion(idproduccion);

CREATE TABLE pr_producido(
	idproductoprod BIGINT(20) NOT NULL,
	fecha DATE,
	cod_art VARCHAR(6),
	cantidad DECIMAL(16, 2),
	idproduccion BIGINT(20),
	VERSION BIGINT(20) NOT NULL,
	idcompania BIGINT(20) NOT NULL,
	PRIMARY KEY (idproductoprod)	
);

ALTER TABLE pr_producido ADD FOREIGN KEY (idproduccion) REFERENCES pr_produccion(idproduccion);

-- 
/** 19.04.2019 **/

INSERT INTO modulo(idmodulo, nombrerecurso, idcompania) VALUES(10, 'production', 1);
INSERT INTO modulocompania VALUES(1, 10, 1);
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) VALUES(251, 'PRODUCTIONPLAN', 10, 15, 'Functionality.production.productionPlan', 1);
INSERT INTO pr_tanque VALUES(1, "TAMQUE 1 -  4000 LT", 4000, 'LT', 0, 1);
INSERT INTO pr_tanque VALUES(2, "TAMQUE 2 -  6000 LT", 6000, 'LT', 0, 1);
INSERT INTO pr_tanque VALUES(3, "TAMQUE 3 -  8000 LT", 8000, 'LT', 0, 1);


ALTER TABLE pr_plan ADD UNIQUE (fecha);


