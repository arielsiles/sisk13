-- 10.03.2024
alter table acopiomp add foreign key (idzonaproductiva) references zonaproductiva (idzonaproductiva);

alter table zonaproductiva add column idciudad bigint(20) after numero;
alter table zonaproductiva add foreign key (idciudad) references ciudad (idciudad);
--
-- 12.03.2024
alter table metaproductoproduccion add column regalia decimal(12,2) after precio;

alter table configuracion add column cxp_iva varchar(20);
alter table configuracion add column cxp_regalia varchar(20);
alter table configuracion add column cxp_cns varchar(20);
alter table configuracion add column ret_cns decimal(5, 3);


update configuracion set cxp_iva        = '11400101' where no_cia = '01';
update configuracion set cxp_regalia    = '21300704' where no_cia = '01';
update configuracion set cxp_cns        = '21300705' where no_cia = '01';
update configuracion set ret_cns        = 0.018 where no_cia = '01';

alter table zonaproductiva add column tienecns int(1) after numero;
update zonaproductiva set tienecns = 0 where tienecns is null;

alter table acopiomp add column tienefac int(1) after boleta;
update acopiomp set tienefac = 0 where tienefac is null;

alter table acopiomp drop column tieneiva;

update acopiomp a set a.estado = 'CONTA' where a.estado = 'APR';

-- Para Terdemol
update zonaproductiva set idciudad = 4 where idciudad is null;

update zonaproductiva set tienecns = 1 where nombre = 'CHAUPISUYU';

-- AUX
select *
from acopiomp a where a.fecha between '2023-09-01' and '2023-10-10'
and a.idproductormateriaprima = 41; -- Pedro Cuba

update acopiomp a set a.estado = 'APR'
where a.fecha between '2023-09-01' and '2023-10-10';

update acopiomp a set a.tienefac = 0
where a.fecha between '2023-09-01' and '2023-10-10'
and a.idproductormateriaprima = 39; -- Feliciano

update acopiomp a set a.tienefac = 0
where a.fecha between '2023-09-01' and '2023-10-10'
and a.idproductormateriaprima = 41; -- Pedro Cuba


-- FELICIANO LIZARAZU REQUE - CHAUPISUYU 13, 15, 16, 17 Sep
-- PEDRO CUBA PEREZ - TAPACARI, LUYULUYUNI, CHAGUERY