--Khipus MRP PROD-73: Aumentar el listado de horas trabajadas al reporte general de produccion.
--Diego H. Loza Fernandez
--Fecha de creacion: 25/11/2013

ALTER TABLE tarjetatiempoempleado ADD (COSTOPORHORA	number(16,2) null);
