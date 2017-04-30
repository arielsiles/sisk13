/** FROM TMPENC **/
SELECT e.`id_tmpenc`, e.`estado`, e.`tipo_doc`, e.`no_doc`, e.`fecha`, e.`cod_prov`, d.`id_tmpdet`, d.`cuenta`, d.`debe`, d.`haber`, d.`cod_prov`, d.`id_tmpenc`
FROM sf_tmpenc e
JOIN sf_tmpdet d ON e.`id_tmpenc` = d.`id_tmpenc`
WHERE d.`cuenta` = '2420910300'
-- AND d.`cod_prov` = 5
AND e.`estado` <> 'ANL'
AND d.`cod_prov` IS NULL
;

/** FROM TMPDET **/
SELECT e.`id_tmpenc`, e.`tipo_doc`, e.`no_doc`, e.`fecha`, e.`cod_prov`, d.`id_tmpdet`, d.`cuenta`, d.`debe`, d.`haber`, d.`cod_prov`, d.`id_tmpenc`
FROM sf_tmpdet d
JOIN sf_tmpenc e ON d.`id_tmpenc` = e.`id_tmpenc`
-- join sf_entidades en on d.`cod_prov` = en.`cod_enti`
WHERE d.`cuenta` = '2420910300'
-- and e.`cod_prov` = 115
AND e.`estado` <> 'ANL'
AND d.`cod_prov` IS NULL
;

/** Actualiza COD_PROV en SF_TMPDET de la cuentas Acreedores%Bienes%Servicios **/
UPDATE sf_tmpdet d
JOIN sf_tmpenc e ON d.`id_tmpenc` = e.`id_tmpenc`
-- join sf_entidades en on d.`cod_prov` = en.`cod_enti`
SET d.`cod_prov` = e.`cod_prov`
WHERE d.`cuenta` = '2420910300'
-- AND e.`cod_prov` = 116
AND d.`cod_prov` IS NULL
;



