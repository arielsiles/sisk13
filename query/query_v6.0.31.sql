-- 08.08.2024

-- Actualizando movimiento de cuentas de un nivel 4 al nivel 5
-- 1110010000 CAJA MONEDA NACIONAL
select * from sf_tmpdet d
where d.cuenta = '1110010000';

update sf_tmpdet set cuenta = '1110010100' where cuenta = '1110010000';

-- Actualizando movimiento de cuentas de un nivel 4 al nivel 5
-- 2110010000 CUENTAS POR PAGAR
select * from sf_tmpdet d
where d.cuenta = '2110010000';

update sf_tmpdet set cuenta = '2110010100' where cuenta = '2110010000';

-- Insertando cuenta nivel 5
-- 2160020100 IVA CREDITO FISCAL POR RECAUDAR
insert into `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `cta_fe`, `tipo_gasto`, `activa`, `util`, `nomutil`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`) values('2160020100','IVA CREDITO FISCAL POR RECAUDAR','2100000000','2160000000',NULL,NULL,'5',NULL,NULL,NULL,'01',NULL,'P',NULL,NULL,'S','N',NULL,NULL,NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,'N',NULL,'S',NULL,NULL);

-- Actualizando movimiento de cuentas de un nivel 4 al nivel 5
-- 2160020000 IVA CREDITO FISCAL POR RECAUDAR
select * from sf_tmpdet d
where d.cuenta = '2160020000';

update sf_tmpdet set cuenta = '2160020100' where cuenta = '2160020000';
update arcgms set ind_mov = 'N' where cuenta = '2160020000';
update sf_tmpdet set cuenta = '5210010300' where id_tmpdet = 10889;

--
select * from sf_tmpdet d
where d.cuenta = '5330100000';

update sf_tmpdet set cuenta = '5330100100' where cuenta = '5330100000';
update sf_tmpdet set cuenta = '5330010300' where id_tmpdet = 15612;