/** 27.03.2019 **/
CREATE TABLE inv_periodo (
	id_inv_periodo BIGINT(20) NOT NULL,
	cod_art VARCHAR(6) NOT NULL,
	saldofis DECIMAL(12,2) NOT NULL,
	saldoval DECIMAL(12,2) NOT NULL,
	costouni DECIMAL(16,6) NOT NULL,
	mes INT(11) NOT NULL,
	gestion INT(11) NOT NULL,
	cod_alm VARCHAR(6) NOT NULL,
	VERSION BIGINT(20) NULL,
	PRIMARY KEY (id_inv_periodo)
);


SELECT e.`id_tmpenc`, e.`no_trans`, d.`id_tmpdet`, e.`fecha`, e.`tipo_doc`, e.`no_doc`, e.`cod_prov`, d.`cuenta`, a.`descri`, d.`debe`, d.`haber`, 
e.`estado`, d.`cod_prov`, d.`cod_art`, d.`cant_art`, e.`glosa`
FROM sf_tmpdet d
LEFT JOIN sf_tmpenc e ON d.`id_tmpenc` = e.`id_tmpenc`
LEFT JOIN arcgms a    ON d.`cuenta` = a.`cuenta`
WHERE d.`cuenta` = 1510110201
AND e.`fecha` BETWEEN '2019-01-01' AND '2019-01-31'
AND e.`estado` <> 'ANL'
;


SELECT d.`cod_art` , d.`debe`, d.`haber`, d.`cant_art`, IF(d.`debe`>0, d.`cant_art`, 0) AS entrada, IF(d.`haber`>0, d.`cant_art`, 0) AS salida
FROM sf_tmpdet d
LEFT JOIN sf_tmpenc e ON d.`id_tmpenc` = e.`id_tmpenc`
WHERE d.`cuenta` = 1510110201
AND e.`fecha` BETWEEN '2019-01-01' AND '2019-01-31'
AND e.`estado` <> 'ANL'
;

SELECT d.`cod_art`, a.`descri`, SUM(d.`debe`) AS debe, SUM(d.`haber`) AS haber, SUM(d.`cant_art`) AS cant, SUM(IF(d.`debe`>0, d.`cant_art`, 0)) AS cant_e, SUM(IF(d.`haber`>0, d.`cant_art`, 0)) AS cant_s
FROM sf_tmpdet d
LEFT JOIN sf_tmpenc e ON d.`id_tmpenc` = e.`id_tmpenc`
LEFT JOIN inv_articulos a ON d.`cod_art` = a.`cod_art`
WHERE d.`cuenta` = 1510110201
AND e.`fecha` BETWEEN '2019-01-01' AND '2019-01-31'
AND e.`estado` <> 'ANL'
GROUP BY d.`cod_art`, a.`descri`
;
