--Khipus MRP PROD-91
--Fecha de creacion: 15/05/2014
--descripcion : Implementar el reporte que muestre el total de insumos y materiales

--permiso reporte de solicitud de materiales e insumos por planificacion
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (389, 'PREPAREDELIVERY', NULL, 15,'menu.warehouse.prepareDelivery', 5); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (389, 50, 15, 1, 5);   
  
--select * from funcionalidad  
--select * from modulo;
--COMMIT
