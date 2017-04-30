--Khipus KHIPUS-50
--Regularizar los asientos contables del modulo de produccion
--Diego H. Loza Fernandez
--Fecha de creacion: 25/03/2014
alter table productobase add (NO_TRANS	VARCHAR2(10 BYTE) null);
--commit