/** 09.09.2019 **/
UPDATE arcgms a SET a.`exije_cc` = 'N';

/** 11.09.2019 **/
-- PARA F. CISC
DELETE FROM inv_subgrupos;
DELETE FROM inv_grupos;
DELETE FROM ventaarticulo;
DELETE FROM articulospromocion;
DELETE FROM promocion;
DELETE FROM inv_articulos;

DELETE FROM inv_almacenes;


UPDATE gensecuencia g SET g.`valor` = 0 WHERE g.`nombre` = 'WAREHOUSE_GROUP_SEQUENCE';
UPDATE gensecuencia g SET g.`valor` = 0 WHERE g.`nombre` = 'WAREHOUSE_PRODUCT_ITEM_SEQUENCE';

DELETE FROM gensecuencia WHERE idgensecuencia = 1;
DELETE FROM gensecuencia WHERE idgensecuencia = 11;

-- Asociar al GRUPO
-- 5451010000 Otros Ingresos Operativos Diversos

/** Ejecutado en F.Cisc **/

