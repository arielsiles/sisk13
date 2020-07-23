-- ----------------------
/** query for v5.0.4 **/
-- ----------------------

-- 21.07.2020

alter table configuracion add column ctaP_debFisIva varchar(20) after ctaprovaf;
update configuracion set ctaP_debFisIva = '2420410200' where no_cia = '01';

alter table configuracion add column ctaP_itxpagar varchar(20) after ctaP_debFisIva;
update configuracion set ctaP_itxpagar = '2420410100' where no_cia = '01';

alter table configuracion add column ctaI_ventapri varchar(20) after ctaP_itxpagar;
update configuracion set ctaI_ventapri = '5420110201' where no_cia = '01';

alter table configuracion add column ctaI_ventasec varchar(20) after ctaI_ventapri;
update configuracion set ctaI_ventasec = '5420110100' where no_cia = '01';

alter table configuracion add column ctaG_it varchar(20) after ctaprovaf;
update configuracion set ctaG_it = '4470510700' where no_cia = '01';

-- 22.07.2020
update secuencia set valor = (select max(e.idpersonacliente)+1 from personacliente e) where tabla = 'personacliente';
update secuencia set valor = (select max(e.idmovimiento)+1000 from movimiento e) where tabla = 'movimiento';