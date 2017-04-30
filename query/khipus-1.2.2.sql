--Khipus MRP PROD-78: Generar vales de ingreso para insumos, materiales y ordenes
--Diego H. Loza Fernandez
--Fecha de creacion: 12/12/2013
alter table ordenproduccion add (COSTOUNITARIO	NUMBER(16,6)	null);
--commit
