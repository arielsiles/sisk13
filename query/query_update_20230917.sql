/** Actualizando datos iniciales Terdemol **/
UPDATE sf_modulos SET mes_proce = '2023-09-01' WHERE modulo = 'INV';
UPDATE sf_modulos SET mes_proce = '2023-09-01' WHERE modulo = 'AF';

UPDATE cg_cencos SET descri = 'CC Terdemol' WHERE cod_cc = '0111';
UPDATE cg_grucc SET descri = 'GCC 01' WHERE gru_cc = '01';

INSERT INTO `inv_inventario`
SELECT i.`cod_art`, '01' AS no_cia, i.`cod_alm`, '0' AS saldo_uni, '0' AS version
FROM inv_articulos i
WHERE i.`cod_art` NOT IN (1,2,3,4,5,6,7)
;

SET @folio = 7;
INSERT INTO inv_inventario_detalle
SELECT (@folio := @folio + 1) AS id_inv_det, '01' AS no_cia, '0111' AS cod_cc, i.`cod_art`, '0' AS cantidad, '0' AS version, i.`cod_alm`, 2 AS idunidadnegocio
FROM inv_articulos i
WHERE i.`cod_art` NOT IN (1,2,3,4,5,6,7)
;

SET @folio = 7;
INSERT INTO inv_inicio
SELECT (@folio := @folio + 1) AS idinvinicio, i.`cod_art`, i.descri AS nombre, '0' AS cantidad, NULL AS und, i.`cod_alm`, '0' AS costo_uni, '2023' AS gestion, '01' AS no_cia, NULL AS cantx
FROM inv_articulos i
WHERE i.`cod_art` NOT IN (1,2,3,4,5,6,7)
;



