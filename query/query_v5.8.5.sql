-- 29.11.2023
alter table acopiomp add column observacion varchar(1000) after estado;
alter table acopiomp add column formulario varchar(50) after boleta;
alter table acopiomp add column chofer varchar(100) after estado;