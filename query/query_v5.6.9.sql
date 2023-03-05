-- 02.03.2023
alter table personacliente add column ctaregula varchar(20) after TIPO_PERSONA;

-- super emapa
update personacliente set ctaregula = '2810210100' where IDPERSONACLIENTE in (1480,782,1400,2505,781,2454,2453,1530);
-- YPFB
update personacliente set ctaregula = '2810210200' where IDPERSONACLIENTE in (726);
-- EMBOL
update personacliente set ctaregula = '2810210300' where IDPERSONACLIENTE in (1444);

insert into `tipopedido` (`IDTIPOPEDIDO`, `NOMBRE`, `tipo`) values('9','REGULARIZAR','REGULARIZE');