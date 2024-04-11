-- 08.04.2024
delete from af_activos where idactivo >= 1;
delete from af_grupos where grupo >= 1;

insert into af_grupos(no_cia, grupo, descri, version) values ('01', 1, 'ACTIVOS TERDEMOL', 0);

insert into `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `util`, `nomutil`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`)
values('12302501','DEPRECIACION ACUMULADO DE INSTALACIONES','12000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'01',NULL,'E','S','N',NULL,'N',NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);

/*
-- Revisar cuenta raiz
update arcgms a set a.cta_raiz = '12000000'
where a.cuenta in ();
*/

-- UPDATE SUBGRUPOS FOR TERDEMOL
update af_subgrupos a set a.descri = 'EQUIPOS E INSTALACION DE LABORATORIO' where subgrupo = '4';

update af_subgrupos a set a.ctadavo = '12300301', a.cta_vo = '12300201', a.cta_alm = '12300201', a.ctagavo = '51000905' where a.subgrupo = 1;
update af_subgrupos a set a.ctadavo = '12300500', a.cta_vo = '12300400', a.cta_alm = '12300400', a.ctagavo = '51000901' where a.subgrupo = 3;
update af_subgrupos a set a.ctadavo = '12300901', a.cta_vo = '12300801', a.cta_alm = '12300801', a.ctagavo = '51000909' where a.subgrupo = 2;
update af_subgrupos a set a.ctadavo = '12301501', a.cta_vo = '12301401', a.cta_alm = '12301401', a.ctagavo = '51000911' where a.subgrupo = 4;
update af_subgrupos a set a.ctadavo = '12302501', a.cta_vo = '12302401', a.cta_alm = '12302401', a.ctagavo = '51000903' where a.subgrupo = 5;
update af_subgrupos a set a.ctadavo = '12302301', a.cta_vo = '12302201', a.cta_alm = '12302201', a.ctagavo = '51000912' where a.subgrupo = 7;
update af_subgrupos a set a.ctadavo = '12300701', a.cta_vo = '12300601', a.cta_alm = '12300601', a.ctagavo = '51000902' where a.subgrupo = 6;
update af_subgrupos a set a.ctadavo = '12301301', a.cta_vo = '12301201', a.cta_alm = '12301201', a.ctagavo = '51000904' where a.subgrupo = 8;
update af_subgrupos a set a.ctadavo = '12301101', a.cta_vo = '12301001', a.cta_alm = '12301001', a.ctagavo = '51000910' where a.subgrupo = 9;

-- 10.04.2024
alter table configuracion add column af_fin_oc varchar(5) after doc_oc_pago;
update configuracion set af_fin_oc = 'CD' where no_cia = '01';

-- Actualizar para Terdemol, para iniciar AF
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_1';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_2';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_3';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_4';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_5';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_6';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_7';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_8';
update gensecuencia g set g.valor = 0 where g.nombre = 'FIXEDASSET_ITEM_SEQUENCE_1_9';

UPDATE secuencia SET valor=(SELECT MAX(e.idgensecuencia)+1 FROM gensecuencia e) WHERE tabla='gensecuencia';

update configuracion set ctaprovaf = '21200101' where no_cia = '01';

