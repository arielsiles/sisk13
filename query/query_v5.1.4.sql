-- v5.1.4
-- 23.01.2021
-- Para diferimiento
alter table credito add column montodif decimal(13,2) not null default 0 after importe;
alter table credito add column cuotadif decimal(13,2) not null default 0 after cuota;

alter table transaccioncredito add column cuotadif decimal(13,2)  default 0 after interes;

-- Resticcion de funcionalidad Creditos
-- CREDIT_PAYMENT
-- Functionality.customers.creditPayment
insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) 
values(421, 'CREDIT_PAYMENT', 1, 3, 'Functionality.customers.creditPayment', 1);

-- CREDIT_GUARANTOR Functionality.customers.creditGuarantor
insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania) 
values(422, 'CREDIT_GUARANTOR', 1, 15, 'Functionality.customers.creditGuarantor', 1);