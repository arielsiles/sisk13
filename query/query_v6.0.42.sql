-- 03.12.2024
alter table acopiomp add column pesoprov decimal(12, 2) after codigo;
update acopiomp set pesoprov = 0 where pesoprov is null;

update acopiomp set pesoprov = pesoneto;
update acopiomp set pesoneto = pesobal;

insert into `_sequence` (seq_name, seq_val) values ('IMP', 0);
insert into `tipodoc` (IDTIPODOC, NOMBRE, DESCRIPCION) values (15, 'IMP', 'INGRESO DE MATERIA PRIMA');