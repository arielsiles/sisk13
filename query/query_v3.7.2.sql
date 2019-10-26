/** 25.10.2019 **/
alter table credito add column fechavence date after ultimopago;

-- for FCisc
-- update transaccioncredito t set t.`id_tmpenc` = null where t.`idtransaccioncredito` = 280;