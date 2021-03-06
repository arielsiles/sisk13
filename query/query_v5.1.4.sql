-- v5.1.4
-- 23.01.2021
-- Para diferimiento
alter table credito add column montodif decimal(13,2) not null default 0 after importe; -- eje ilva
alter table credito add column cuotadif decimal(13,2) not null default 0 after cuota; -- eje ilva
alter table transaccioncredito add column cuotadif decimal(13,2)  default 0 after interes; -- eje ilva

-- Resticcion de funcionalidad Creditos
-- CREDIT_PAYMENT
-- Functionality.customers.creditPayment
insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
values(421, 'CREDIT_PAYMENT', 1, 3, 'Functionality.customers.creditPayment', 1);

-- CREDIT_GUARANTOR Functionality.customers.creditGuarantor
insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
values(422, 'CREDIT_GUARANTOR', 1, 15, 'Functionality.customers.creditGuarantor', 1);

-- 27.01.2021
alter table credito add column traspaso int(1) not null default 0 after entregado; -- eje ilva
alter table credito add column idcreditoorig bigint(20) after traspaso; -- eje ilva
alter table credito add foreign key (idcreditoorig) references credito(idcredito); -- eje ilva
