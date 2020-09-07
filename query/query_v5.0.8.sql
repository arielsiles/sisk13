-- ----------------------
/** query for v5.0.8 **/
-- ----------------------

-- 01.08.2020
-- Actualizando precios de venta de productos veterinarios en inv_articulos
update ventaarticulo v
join inv_articulos a on v.`cod_art` = a.`cod_art`
set a.`precio_venta` = v.`PRECIO`
where a.`cod_alm` = 5
;

alter table tipocaja add column cuentacaja varchar(20) after nombre;
alter table tipocaja add column cuentaingreso varchar(20) after cuentacaja;
alter table tipocaja add column no_cia varchar(2) after cuentaingreso;

update tipocaja set cuentacaja = '1110110100', cuentaingreso = '5420110201', no_cia = '01' where idtipocaja = 1;
update tipocaja set cuentacaja = '1110110300', cuentaingreso = '5420110100', no_cia = '01' where idtipocaja = 2;

-- usuario cisc
update usuario u set u.`clave` = '45e0c1953ab77904731fb68ab35a55db23b04cf5', u.`usuario` = 'cisc', u.`usuariofinanzas` = 1 where u.`idusuario` = 408;
update pedidos p set p.`IDUSUARIO` = 408 where p.`IDUSUARIO` = 5;
update ventadirecta v set v.`IDUSUARIO` = 408 where v.`IDUSUARIO` = 5;
delete from usuario where IDUSUARIO = 5;
