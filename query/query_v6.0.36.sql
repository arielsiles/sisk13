-- 17.09.2024
alter table xpr_produccion add column fechainicio timestamp after descripcion;
alter table xpr_produccion add column fechafin timestamp after fechainicio;

