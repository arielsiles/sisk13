--Khipus MRP PROD-79: Test
--Diego H. Loza Fernandez
--Fecha de creacion: 27/12/2013
alter table costosindirectosconf  add  (ESTADO VARCHAR(255) NULL);
alter table ordenproduccion drop constraint UQ_ORDENPRODUCCIONFECHA;
drop index UQ_ORDENPRODUCCIONFECHA;
--COMMIT
