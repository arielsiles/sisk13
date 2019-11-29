/** 25.10.2019 **/
alter table credito add column fechavence date after ultimopago;

-- for FCisc
-- update transaccioncredito t set t.`id_tmpenc` = null where t.`idtransaccioncredito` = 280;

-- update credito c set c.`fechavence` = '2019-01-01';
--

/** 30.10.2019 **/
insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) values(256, 'CUSTOMERPROCESS', 1, 1, 'Functionality.customers.process', 1);

alter table configuracion add column i_pvig_pf_mn varchar(20);
update configuracion c set c.`i_pvig_pf_mn` = '5130410100';

/** 01/11/2019 **/
alter table cuenta add column fechavence date after fechaapertura;
-- Para F.Cisc
delete from cuenta where nocuenta = '1000000153';

