-- 03.02.2025
alter table pagoordencompra add column prov_aux varchar(6) after nombrebeneficiario;
alter table pagoordencompra add column cuentarendir varchar(20) after prov_aux;

alter table sf_tmpenc add column created_at datetime;
alter table sf_tmpenc add column created_by varchar(100);
alter table sf_tmpenc add column updated_at datetime;
alter table sf_tmpenc add column updated_by varchar(100);

alter table com_encoc add column created_at datetime;
alter table com_encoc add column created_by varchar(100);
alter table com_encoc add column updated_at datetime;
alter table com_encoc add column updated_by varchar(100);

