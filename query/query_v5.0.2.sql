-- ----------------------
/** query for v5.0.2 **/
-- ----------------------

-- 03.07.2020
alter table dosificacion add column activo int(1) after estado;
update dosificacion set activo = 0;
update dosificacion set activo = 1 where iddosificacion = 11;

alter table dosificacion add column version bigint(20) null;
alter table dosificacion add column idcompania bigint(20) null;

update dosificacion set version = 0;
update dosificacion set idcompania = 1;

-- 04.07.2020
insert into secuencia values ('movimiento', 0);
update secuencia set valor = (select max(a.idmovimiento)+1 from movimiento a) where tabla = 'movimiento';

alter table pedidos modify column TIENEFACTURA tinyint(1) default 1;
update pedidos p set p.`TIENEFACTURA` = 1 where p.`TIENEFACTURA` is null;