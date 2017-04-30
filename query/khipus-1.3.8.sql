--Khipus KHIPUS-71
--Fecha de creacion: 05/06/2014
--descripcion : Aumentar los combos al reporte de pedidos
alter table USER01_DAF.paquetes add (nombrecorto varchar(25) null);
--PERMISO CAMBIAR EL ESTADO DE LA ORDEN
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (392, 'ADDPRODUCTOREPROCCED', NULL, 15,'BaseProduct.action.addProductPermisochange', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (392, 50, 15, 1, 6);   
--select * from funcionalidad  
--select * from modulo;
--ALTER TABLE WISE.COM_ENCOC ADD (CONFACTURA	VARCHAR(50) NULL);
--COMMIT