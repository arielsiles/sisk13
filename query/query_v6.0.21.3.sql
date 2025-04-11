-- 03.04.2025
alter table pedidos add column idmetodopago bigint after idmotivoanulacion;
alter table sin_metodopago add column activo int after descripcion;

update sin_metodopago set activo = 1 where activo is null;


-- falta llaves foraneas idmetodopago, idmotivoanulacion

-- aux
update personacliente p set p.CODMETODOPAGOSIN = 1 where p.IDPERSONACLIENTE > 0;
