--Khipus KHIPUS-84
--Fecha de creacion: 10/06/2014
--descripcion : Implementar el reporte de leche cruda
--Permiso: Reporte de balance de produccion
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (397, 'PRODUCTDELIVERYBYDATE', NULL, 15,'menu.warehouse.productDeliveryByDate', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (397, 50, 15, 1, 6);   
--select * from funcionalidad;  
--select * from modulo;
--COMMIT