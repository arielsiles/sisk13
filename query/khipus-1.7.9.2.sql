-- 09072015: Ariel
-- -------------------------------------------
-- 
UPDATE configuracion SET CTAIVACREFIMN = '1420710000';
UPDATE configuracion SET CTATRANSALMMN = '1580110300';
UPDATE configuracion SET CTAIVACREFITRMN = '4470510700';

-- INSERT
-- 1580110300 MERCADERIAS EN TRANSITO
INSERT INTO `arcgms` (`cuenta`, `descri`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`) VALUES('1580110300','MERCADERIAS EN TRANSITO','I','6','6','','0','01',NULL,'A','S',NULL,NULL,'0.00','P','0.00','0.000000','0.000000','0.000000','0.000000',NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S','0.00','0.00');

DELETE FROM sf_tmpdet;
DELETE FROM sf_tmpenc;

ALTER TABLE sf_tmpdet ADD id_tmpdet BIGINT(20) NOT NULL FIRST;
ALTER TABLE sf_tmpdet ADD CONSTRAINT PK_TMPDET PRIMARY KEY (id_tmpdet);

ALTER TABLE sf_tmpdet ADD id_tmpenc BIGINT(20) NOT NULL;
ALTER TABLE sf_tmpdet ADD CONSTRAINT FK_TMPENC FOREIGN KEY (id_tmpenc) REFERENCES sf_tmpenc (id_tmpenc);

INSERT INTO secuencia (tabla, valor) VALUES ('sf_tmpdet', 1);

DELETE FROM arcgms
WHERE cuenta = '2420900000'
AND descri LIKE '%Acreedores por compra bienes y servicios%';

-- -------------------------------------------