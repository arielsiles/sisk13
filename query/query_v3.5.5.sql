/** 13.07.2019 **/
alter table pr_material add column tipo varchar(15) null after flag_cant;
update pr_material p set p.`tipo` = 'MATERIAL';

alter table pr_material add column vol1 decimal(10, 2) null after tipo;
alter table pr_material add column peso1 decimal(10, 2) null after vol1;