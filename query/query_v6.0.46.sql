-- 29.01.2025
alter table com_encoc add column idtmpenc bigint after no_orden;

alter table inv_destino add column activo int after nombre;

update inv_destino set activo = 1;