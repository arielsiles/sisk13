--Khipus KHIPUS-50
--Regularizar los asientos contables del modulo de produccion
--Diego H. Loza Fernandez
--Fecha de creacion: 07/03/2014
--permiso para generar todos los asientos de ordenes del dia
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (367, 'GENERATEALLACCOUNTINGENTRIES', NULL, 15,'ProductionPlanning.generateAccountingByProductionDay', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (367, 50, 15, 1, 6);     
--select * from funcionalidad;  
--select * from modulo
--COMMIT

alter table ordenproduccion add (NO_VALE	VARCHAR2(20 BYTE) null);
alter table productobase add (NO_VALE	VARCHAR2(20 BYTE));

/*select * from wise.INV_VALES
where ;*/