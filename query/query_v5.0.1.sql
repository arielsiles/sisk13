-- ----------------------
/** query for v5.0.1 **/
-- ----------------------

-- 02.07.2020
insert into secuencia values ('precioarticulo', 0);
update secuencia set valor = (select max(a.`idprecioarticulo`)+1 from precioarticulo a) where tabla = 'precioarticulo';