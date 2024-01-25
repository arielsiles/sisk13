-- 25.01.2024
alter table configuracion add column cxp_provmn varchar(20);

-- To Terdemol
update configuracion set cxp_provmn = '21200101' where no_cia = '01';