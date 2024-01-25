-- 25.01.2024
alter table configuracion add column cxp_provmn varchar(20);

-- To Terdemol
update configuracion set cxp_provmn = '21200101' where no_cia = '01';

-- To ILVA
update configuracion set cxp_provmn = '2420910300' where no_cia = '01';