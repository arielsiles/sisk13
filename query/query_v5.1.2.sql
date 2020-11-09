-- Query v5.1.2

alter table caja add primary key (idcaja);
alter table tipocaja add primary key (idtipocaja);


alter table cajausuario modify column 

alter table cajausuario add foreign key (idcaja) references caja (idcaja);
alter table cajausuario add foreign key (idusuario) references usuario (idusuario);


-- 17.10.2020
insert into funcionalidad values (267, 'NULLIFYSALE', null, 1, 1, 'menu.customers.configuration.nullifySale', 1); -- Por confirmar

-- 23.10.2020
-- Verificar AF pendientes
-- Configurar cuenta en tabla configuración CTAPROVOBU

insert into af_tipomovs values('01', 'MEJ', 'MEJORA', 'MEJ', 'VIG', 0);
insert into af_tipomovs values('01', 'TRA', 'TRANSFERENCIA', 'TRA', 'VIG', 0);

update af_subgrupos a set a.`ctamej` = a.`ctadavo`;

-- 05.11.2020
alter table tipocaja add column cuentaxcobrar varchar(20) after cuentaingreso; 
update tipocaja set cuentaxcobrar = '1421010100' where idtipocaja = 1; -- Para venta de lacteos
update tipocaja set cuentaxcobrar = '1421010200' where idtipocaja = 2; -- Para venta de veterinaria

insert into funcionalidad values (268, 'ACCOUNTINGCREDITSALE', null, 1, 1, 'Functionality.customers.accountingCreditSales', 1);

-- 07.11.2020
alter table configuracion add column ctacomision varchar(20) after distparam;
update configuracion c set c.`ctacomision` = '4470210401' where c.`no_cia` = '01';

alter table configuracion add column iva_tax decimal (5, 2) after iue;
alter table configuracion add column net_val decimal (5, 2) after iva_tax;
alter table configuracion add column it_tax decimal (5, 2) after net_val;

update configuracion c set c.`iva_tax` = 0.13 where c.`no_cia` = '01';
update configuracion c set c.`net_val` = 0.87 where c.`no_cia` = '01';
update configuracion c set c.`it_tax` = 0.03 where c.`no_cia` = '01';

-- 09.11.2020
-- SOLO Para ILVA
update af_activos a set a.`estado` = 'TDP' where a.`estado` = 'BAJ';


select *
from af_activos a 
update af_activos a set a.`estado` = 'TDP' 
where a.`estado` = 'BAJ';










