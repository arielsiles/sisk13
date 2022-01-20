-- 16.01.2022
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(443, 'ACCOUNTIG_PROUCER_PAYMENT', 6, 1, 'Functionality.production.accountingProducerPayment', 1);

-- Para fondo lechero/fcisc si se requiere
alter table ventadirecta add column tipoventa varchar(100);