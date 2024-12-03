-- 03.12.2024
alter table acopiomp add column pesoprov decimal(12, 2) after codigo;
update acopiomp set pesoprov = 0 where pesoprov is null;