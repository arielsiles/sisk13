-- delete FROM `arcgms`;
-- Almacenes
UPDATE `inv_almacenes` a SET a.`cuenta` = '11300701' WHERE a.`cod_alm` = 1;
UPDATE `inv_almacenes` a SET a.`cuenta` = '11300601' WHERE a.`cod_alm` = 2;
UPDATE `inv_almacenes` a SET a.`cuenta` = '11300101' WHERE a.`cod_alm` = 3;

-- inv_grupos
UPDATE `inv_grupos` a SET a.`cuenta_inv` = '11300601' WHERE a.`cod_gru` = 2;
UPDATE `inv_grupos` a SET a.`cuenta_inv` = '11300701' WHERE a.`cod_gru` = 4;
UPDATE `inv_grupos` a SET a.`cuenta_inv` = '11300301' WHERE a.`cod_gru` = 5;
UPDATE `inv_grupos` a SET a.`cuenta_inv` = '11300101' WHERE a.`cod_gru` = 6;

-- Alter `inv_subgrupos`
ALTER TABLE `inv_subgrupos` ADD COLUMN cta_gasto VARCHAR(20) AFTER estado;

UPDATE inv_subgrupos SET cta_gasto = 	'56000201'	 WHERE cod_gru = 	2	 AND cod_sub = 	1	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000202'	 WHERE cod_gru = 	2	 AND cod_sub = 	2	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000203'	 WHERE cod_gru = 	2	 AND cod_sub = 	3	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000204'	 WHERE cod_gru = 	2	 AND cod_sub = 	4	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000205'	 WHERE cod_gru = 	2	 AND cod_sub = 	5	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000206'	 WHERE cod_gru = 	2	 AND cod_sub = 	6	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000207'	 WHERE cod_gru = 	2	 AND cod_sub = 	7	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000208'	 WHERE cod_gru = 	2	 AND cod_sub = 	8	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000209'	 WHERE cod_gru = 	2	 AND cod_sub = 	9	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000210'	 WHERE cod_gru = 	2	 AND cod_sub = 	10	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000211'	 WHERE cod_gru = 	2	 AND cod_sub = 	11	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000212'	 WHERE cod_gru = 	2	 AND cod_sub = 	12	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000213'	 WHERE cod_gru = 	2	 AND cod_sub = 	13	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000214'	 WHERE cod_gru = 	2	 AND cod_sub = 	14	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000215'	 WHERE cod_gru = 	2	 AND cod_sub = 	15	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000216'	 WHERE cod_gru = 	4	 AND cod_sub = 	1	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000217'	 WHERE cod_gru = 	4	 AND cod_sub = 	2	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000218'	 WHERE cod_gru = 	4	 AND cod_sub = 	3	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000219'	 WHERE cod_gru = 	4	 AND cod_sub = 	4	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000220'	 WHERE cod_gru = 	4	 AND cod_sub = 	5	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000221'	 WHERE cod_gru = 	4	 AND cod_sub = 	6	;
UPDATE inv_subgrupos SET cta_gasto = 	'56000108'	 WHERE cod_gru = 	5	 AND cod_sub = 	1	;

--
SELECT i.`cod_art`, i.`descri`, i.`cuenta_art`, i.`cod_gru`, i.`cod_sub`, s.`descri`, s.`cta_gasto`
FROM inv_articulos i
LEFT JOIN inv_subgrupos s ON i.`cod_sub` = s.`cod_sub`
WHERE i.`cod_gru` = s.`cod_gru`
;

-- Actualizando `inv_articulos`
UPDATE inv_articulos i
LEFT JOIN inv_subgrupos s ON i.`cod_sub` = s.`cod_sub`
SET i.`cuenta_art` = s.`cta_gasto`
WHERE i.`cod_gru` = s.`cod_gru`
;

UPDATE `inv_almacenes` i SET i.`ctacosto` = '51000801' WHERE i.`cod_alm` = 3;

-- Actualizando Vales
UPDATE `inv_movdet` i
LEFT JOIN inv_articulos a ON i.`cod_art` = a.`cod_art`
SET i.`cuenta_art` = a.`cuenta_art`
WHERE i.`cod_art` = a.`cod_art`
;

UPDATE configuracion SET 		ctaaitb = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaantprovme = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaantprovmn = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctadiftipcam = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctadeptrame = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctadeptramn = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaafet = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaivacrefime = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaprovobu = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaalmme = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctatransalmme = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		pagoctabcomn = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctatransalm1mn = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctatransalm2mn = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaalmmn = 	'11100101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		cajagral1mn = 	'11100101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ct_cajaveter = 	'11100101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ct_cajaahorro = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaivacrefimn = 	'11400101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaAlmPT = 	'11300101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaAlmPTAG = 	'11300101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaAlmPV = 	'11300101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctatransalmmn = 	'11300401'	 WHERE no_cia = '01';
UPDATE configuracion SET 		iue_ret = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		it_ret = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaP_itxpagar = 	'21300501'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaP_debFisIva = 	'21300401'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaprovaf = 	'21200301'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaCostPT = 	'51000801'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaCostPV = 	'51000801'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaMermaBaj = 	'56000602'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaReproc = 	'56000603'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctacomision = 	'52000300'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaProm = 	'52000300'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaMerma = 	'52000901'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaivacrefitrmn = 	'52000902'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaG_it = 	'52000903'	 WHERE no_cia = '01';
UPDATE configuracion SET 		i_pvig_pf_mn = 	'10000000'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaI_ventasec = 	'41000101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		ctaI_ventapri = 	'41000101'	 WHERE no_cia = '01';
UPDATE configuracion SET 		pagoctabcome = 	'1'	 WHERE no_cia = '01';

-- Actualizando cuentas proveedores
UPDATE `cxp_proveedores` SET `ctaxpagar` = '21200101';


SELECT DISTINCT s.`cuenta`
FROM `sf_tmpdet` s
;

-- Actualizando cuentas de asientos contables
UPDATE sf_tmpdet SET cuenta = 	'53000700'	 WHERE cuenta = 	'4470610210'	;
UPDATE sf_tmpdet SET cuenta = 	'11400101'	 WHERE cuenta = 	'1420710000'	;
UPDATE sf_tmpdet SET cuenta = 	'21200101'	 WHERE cuenta = 	'2420910300'	;
UPDATE sf_tmpdet SET cuenta = 	'53001600'	 WHERE cuenta = 	'4520100000'	;
UPDATE sf_tmpdet SET cuenta = 	'11200402'	 WHERE cuenta = 	'1420310100'	;
UPDATE sf_tmpdet SET cuenta = 	'11300601'	 WHERE cuenta = 	'1580110200'	;
UPDATE sf_tmpdet SET cuenta = 	'11100101'	 WHERE cuenta = 	'1110110100'	;
UPDATE sf_tmpdet SET cuenta = 	'11300701'	 WHERE cuenta = 	'1580110400'	;
UPDATE sf_tmpdet SET cuenta = 	'11100301'	 WHERE cuenta = 	'1130110401'	;


UPDATE arcgms SET ind_mov = 'S';
UPDATE arcgms SET exije_cc = 'N';

-- Actualizando cta bancos
UPDATE `ck_ctas_bco` SET cuenta = '11100301' WHERE cuenta = '1130110401';





