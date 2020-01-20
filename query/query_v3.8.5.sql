/** 19.01.2020 **/
alter table configuracion add column compania varchar(100) after subtitulo;
alter table configuracion add column sistema  varchar(100) after compania;
alter table configuracion add column lugar  varchar(100) after sistema;

-- FCISC
update configuracion c set c.`compania` = 'COOPERATIVA INTEGRAL DE SERVICIOS CBBA LTDA';
update configuracion c set c.`sistema` = 'SISTEMA INTEGRADO CONTABLE';
update configuracion c set c.`lugar` = 'PUNATA - COCHABAMBA';

-- FLEHC
update configuracion c set c.`compania` = 'INDUSTRIAS LACTEAS DEL VALLE ALTO';
update configuracion c set c.`sistema` = 'SISTEMA INTEGRADO CONTABLE';
update configuracion c set c.`lugar` = 'PUNATA - COCHABAMBA';
