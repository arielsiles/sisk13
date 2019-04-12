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

CREATE TABLE formulacion (
	idformulacion BIGINT(20) NOT NULL,
	nombre VARCHAR(255) NOT NULL,
	capacidad DECIMAL(16,2) NOT NULL,
	PRIMARY KEY (idformulacion)
);

CREATE TABLE insumoformula(
	idinsumoformula BIGINT(20) NOT NULL,
	cantidad DECIMAL(20, 6) NOT NULL,
	cod_art VARCHAR(6) NOT NULL,
	PRIMARY KEY (idinsumoformula)
);














