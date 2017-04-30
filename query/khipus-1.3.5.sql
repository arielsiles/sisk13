--Khipus MRP PROD-91
--Fecha de creacion: 15/05/2014
--descripcion : Implementar el reporte que muestre el total de insumos y materiales
--permiso reporte de solicitud de materiales e insumos por orden
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (387, 'GENERATEREQUESTBYORDER', NULL, 15,'ProductionPlanning.generateOrderDocumnt', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (387, 50, 15, 1, 6);  
--permiso reporte de solicitud de materiales e insumos por planificacion
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (388, 'GENERATEREQUESTBYPLANNIG', NULL, 15,'ProductionPlanning.generateReportRequestInputAndMaterialByPlanning', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (388, 50, 15, 1, 6);   
  
--select * from funcionalidad  
--select * from modulo;
--COMMIT
--Khipus MRP PROD-92
--Fecha de creacion: 20/05/2014
--descripcion : Agregar la opcion de elegir y registrar una cuenta contable para el registro del almacen
ALTER TABLE WISE.INV_ALMACENES 
ADD (CUENTA VARCHAR2(20));

/*ALTER TABLE WISE.INV_ALMACENES 
ADD CONSTRAINT fk_ARCGMS_INV_ALMACENES FOREIGN KEY (CUENTA) REFERENCES WISE.ARCGMS(CUENTA);
*/
--COMMIT