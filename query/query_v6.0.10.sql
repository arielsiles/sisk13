-- 08.04.2024
-- Solo Para Terdemol Inicio de AF
delete from af_activos where idactivo >= 1;
delete from af_grupos where grupo >= 1;
insert into af_grupos(no_cia, grupo, descri, version) values ('01', 1, 'ACTIVOS TERDEMOL', 0);
-- End
/*
-- Revisar cuenta raiz
update arcgms a set a.cta_raiz = '12000000'
where a.cuenta in ();
*/

-- Solo para TERDEMOL
-- UPDATE SUBGRUPOS FOR TERDEMOL
update af_subgrupos a set a.descri = 'EQUIPOS E INSTALACION DE LABORATORIO' where subgrupo = '4';

update af_subgrupos a set a.ctadavo = '12300301', a.cta_vo = '12300201', a.ctamej = '12300201', a.cta_alm = '12300201', a.ctagavo = '51000905' where a.subgrupo = 1;
update af_subgrupos a set a.ctadavo = '12300500', a.cta_vo = '12300400', a.ctamej = '12300400', a.cta_alm = '12300400', a.ctagavo = '51000901' where a.subgrupo = 3;
update af_subgrupos a set a.ctadavo = '12300901', a.cta_vo = '12300801', a.ctamej = '12300801', a.cta_alm = '12300801', a.ctagavo = '51000909' where a.subgrupo = 2;
update af_subgrupos a set a.ctadavo = '12301501', a.cta_vo = '12301401', a.ctamej = '12301401', a.cta_alm = '12301401', a.ctagavo = '51000911' where a.subgrupo = 4;
update af_subgrupos a set a.ctadavo = '12302501', a.cta_vo = '12302401', a.ctamej = '12302401', a.cta_alm = '12302401', a.ctagavo = '51000903' where a.subgrupo = 5;
update af_subgrupos a set a.ctadavo = '12302301', a.cta_vo = '12302201', a.ctamej = '12302201', a.cta_alm = '12302201', a.ctagavo = '51000912' where a.subgrupo = 7;
update af_subgrupos a set a.ctadavo = '12300701', a.cta_vo = '12300601', a.ctamej = '12300601', a.cta_alm = '12300601', a.ctagavo = '51000902' where a.subgrupo = 6;
update af_subgrupos a set a.ctadavo = '12301301', a.cta_vo = '12301201', a.ctamej = '12301201', a.cta_alm = '12301201', a.ctagavo = '51000904' where a.subgrupo = 8;
update af_subgrupos a set a.ctadavo = '12301101', a.cta_vo = '12301001', a.ctamej = '12301001', a.cta_alm = '12301001', a.ctagavo = '51000910' where a.subgrupo = 9;
-- End

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
update unidadnegocio set sigla = 'OC' where sigla = 'CISC';

-- 14.04.2024
alter table xpr_maquina add column idactivo bigint(20) after nombre;

alter table af_activos add primary key (idactivo);

alter table xpr_maquina add foreign key (idactivo) references af_activos(idactivo);

UPDATE secuencia SET valor=(SELECT MAX(e.idmaquina)+1 FROM xpr_maquina e) WHERE tabla='xpr_maquina';