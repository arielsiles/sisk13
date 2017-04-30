--Fecha de creacion: 08/07/2014
--descripcion : CRUD costos indirectos
--Permiso: permiso para configuracion costo indirecto
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
    VALUES (398, 'INDIRECTCOSTCONFIGURATION', NULL, 15,'menu.production.configuration.indirectCostconfiguration', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (398, 50, 15, 1, 6);   
--select * from funcionalidad;  
--select * from modulo;
--COMMIT
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
    VALUES (404, 'INDIRECTCOSTS', NULL, 15,'menu.production.configuration.indirectCosts', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (404, 50, 15, 1, 6);   
--
alter table costosindirectosconf add (PREDEFINIDO NUMBER(1) NULL);
alter table periodocostoindirecto modify MES NUMBER(2,0) not null unique;
  