-- ----------------------
/** query for v5.0.5 **/
-- ----------------------

-- 24.07.2020
alter table pedidos add column MONTODIST decimal(16,2) after TOTALIMPORTE;
alter table configuracion add column distparam decimal(5,2) after ctaI_ventasec;
update configuracion set distparam = 0.20 where no_cia = '01';