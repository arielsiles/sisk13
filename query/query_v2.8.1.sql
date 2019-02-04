/** 31.01.2019 **/
ALTER TABLE tipocuenta ADD COLUMN ctap_mv VARCHAR(20) AFTER ctap_me;

INSERT INTO `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`) 
VALUES('1110110101','Caja General CISC M.N.',NULL,NULL,NULL,'6',NULL,NULL,'0','01',NULL,NULL,'S',NULL,NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);

/** 02.02.2019 **/
UPDATE tipocuenta t SET t.`ctap_mn` = '2120110200', t.`ctap_me` = '2120120100', t.`ctap_mv` = '2120130200'
WHERE t.`idtipocuenta` = 1;


UPDATE transaccioncredito SET fechatransaccion = 	"	2018-08-28	"	 WHERE idtransaccioncredito = 	401	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-09-21	"	 WHERE idtransaccioncredito = 	556	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-10-02	"	 WHERE idtransaccioncredito = 	555	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-08-29	"	 WHERE idtransaccioncredito = 	557	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-09-27	"	 WHERE idtransaccioncredito = 	559	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-09-25	"	 WHERE idtransaccioncredito = 	560	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-06-12	"	 WHERE idtransaccioncredito = 	561	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-06-19	"	 WHERE idtransaccioncredito = 	563	;
UPDATE transaccioncredito SET fechatransaccion = 	"	2018-06-26	"	 WHERE idtransaccioncredito = 	830	;
UPDATE credito SET fechaconcesion = 	"	2017-06-22	"	 WHERE idcredito = 	171	;

-- select c.`idcredito`, c.`estado`, c.`codigoant`, c.`fechaconcesion`, c.`importe`, c.`ultimopago`, t.`idtransaccioncredito`, t.`fechatransaccion`
-- from credito c
-- 1. Actualiza ultimopago con fecha de concesion
UPDATE credito c 
JOIN transaccioncredito t ON c.`idcredito` = t.`idcredito`
SET c.`ultimopago` = t.`fechatransaccion`
WHERE t.`tipo` = 'EGR'
;

-- 2. Actualiza ultimopago con ultima fecha de transaccion

INSERT INTO _sequence VALUES ('RI', 134);
INSERT INTO tipodoc VALUES(7, 'RI', 'RECIBO DE INGRESO');

SELECT *
FROM sf_tmpenc e
UPDATE sf_tmpenc e SET e.`tipo_doc` = 'RI'
WHERE e.`tipo_doc` = 'CI'
;

/** 03.02.2019 **/
-- Lecheria
-- Prestamos Amortizables CIS M.N.
-- 5150510600	Intereses Prestamos Amortizables Ejec.MN
UPDATE tipocredito t SET t.`ictaeje` = '5150510600' WHERE idtipocredito = 1;
-- Prestamos Amortizables M.E. BID
-- 5150520600	Intereses Prestamos Amort.Ejec. ME BID
UPDATE tipocredito t SET t.`ictaeje` = '5150520600' WHERE idtipocredito = 2;
