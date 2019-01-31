/** 31.01.2019 **/
ALTER TABLE tipocuenta ADD COLUMN ctap_mv VARCHAR(20) AFTER ctap_me;

INSERT INTO `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`) 
VALUES('1110110101','Caja General CISC M.N.',NULL,NULL,NULL,'6',NULL,NULL,'0','01',NULL,NULL,'S',NULL,NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);