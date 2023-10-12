-- 09.10.2023

INSERT INTO `inv_tipodocs` (`no_cia`, `cod_doc`, `ctracuentamn`, `desc_def`, `restriccioncampo`, `descri`, `estado`, `version`, `tipo_vale`)
VALUES('01','BPT',NULL,'BAJA POR VENTA DE PRODUCTO TERMINADO',NULL,'BAJA POR VENTAS P.T.','VIG','0','BV');

ALTER TABLE inv_tipodocs ADD COLUMN ctacosto VARCHAR(20) NULL AFTER descri;

UPDATE inv_tipodocs i SET i.`ctacosto` = '4420110201' WHERE i.`cod_doc` = 'BPT';
