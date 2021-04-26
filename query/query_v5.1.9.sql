-- 25.04.2021
alter table tipomovimientoproductor add column tipo varchar(100) after nombre;
update tipomovimientoproductor set tipo = 'TACH' where idtipomovimientoproductor = 1;
update tipomovimientoproductor set tipo = 'CONC' where idtipomovimientoproductor = 2;
update tipomovimientoproductor set tipo = 'LACT' where idtipomovimientoproductor = 3;
update tipomovimientoproductor set tipo = 'VETE' where idtipomovimientoproductor = 4;
update tipomovimientoproductor set tipo = 'OEGR' where idtipomovimientoproductor = 5;
update tipomovimientoproductor set tipo = 'OING' where idtipomovimientoproductor = 6;
update tipomovimientoproductor set tipo = 'CBAN' where idtipomovimientoproductor = 7;
update tipomovimientoproductor set tipo = 'CRED' where idtipomovimientoproductor = 8;

insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
values(423, 'CREDITSALE_INVOICE', 1, 1, 'Functionality.customers.registerCreditSaleInvoice', 1);

insert into funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
values(424, 'CREDITSALE_REGISTER', 1, 1, 'Functionality.customers.registerCreditSale', 1);