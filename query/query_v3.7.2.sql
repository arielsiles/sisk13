/** 25.10.2019 **/
alter table credito add column fechavence date after ultimopago;

-- for FCisc
-- update transaccioncredito t set t.`id_tmpenc` = null where t.`idtransaccioncredito` = 280;
-- update credito c set c.`fechavence` = '2019-01-01';

delete from transaccioncredito where idcredito = 80;
delete from credito where idcredito = 80;
--