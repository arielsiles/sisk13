-- 12.02.2025
update unidadorganizacional set codigocencos = '0111' where numerocompania = '01';
update inv_destino set activo = 0 where activo = 1;

-- añadir columna tipo_area a la tabla inv_destino
alter table inv_destino add column tipo_area varchar(100) after activo;

-- añadir columna idproceso en inv_vales despues de iddestino
alter table inv_vales add column idproceso bigint(20) after iddestino;

-- añadir llave foranea tabla xpr_proceso columna idproceso en inv_vales
alter table inv_vales add foreign key (idproceso) references xpr_proceso(idproceso);

-- add column cod_prod varchar 6 in inv_vales table after idproceso
alter table inv_vales add column cod_prod varchar(6) after idproceso;

-- SOLO TERDEMOL
update inv_almacenes set tipo = 'FINISHED_GOODS' where cod_alm = 3;
--
--
-- Añadir columna idlinea bigint(20) en xpr_produccion despues de idproceso, asi como tambien añadir llave foranea con la tabla xpr_linea
alter table xpr_produccion add column idlinea bigint(20) after idproceso;
alter table xpr_produccion add foreign key (idlinea) references xpr_linea(idlinea);