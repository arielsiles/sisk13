--Khipus KHIPUS-84
--Fecha de creacion: 16/07/2014
--descripcion : Liquidar ordenes de conpra parcialmente
alter table DOCUMENTOCOMPRA add (ESTADOPAGO number(1) default 0);
--commit
--Fecha de creacion: 25/07/2014
--descripcion: Producir un producto en base a otro producuto
/*
Pasa lo siguiente en produccion con la Leche UHT
Realizan una produccion de leche UHT 950 en un tanque y sacan por ejempo 8000 un., resulta que 2000 es subsidio con otro material de envasado y 6000 es el envasado comun
En el sistema lo registran directamente los 6000 con el envasado normal
y no estamos estamos controlando el stock del subsidio
como hariamos esto
lo mismo pasa con el Yog sachet frutilla 80cc
envasan una parte Yog sachet frutilla 120cc que es el subsidio
pero esto si lo registran separado
exactamente que la leche UHT pasa con el Yogurt frutado 1L envasan subsidio y el normal
*/
alter table ORDENPRODUCCION MODIFY (IDCOMPOSICIONPRODUCTO NUMBER(24,0) NULL);

CREATE TABLE PRODUCTOORDEN(
   IDPRODUCTOORDEN NUMBER(24,0) NOT NULL
  ,IDORDENPRODUCCION NUMBER(24,0) NOT NULL  
  ,NOMBREPRODUCTO VARCHAR2(100 BYTE) NOT NULL
  ,IDPRODUCTOPROCESADO NUMBER(24,0) NOT NULL  
  ,CONSTRAINT PK_PRODUCTOORDEN PRIMARY KEY(IDPRODUCTOORDEN)
);
--DROP TABLE PRODUCTOORDEN

--COMMIT

alter table PRODUCTOORDEN add constraint fk_PRODORD_PROPROC FOREIGN KEY (IDPRODUCTOPROCESADO) REFERENCES PRODUCTOPROCESADO(IDPRODUCTOPROCESADO);
alter table PRODUCTOORDEN add constraint fk_PRODORD_ORDPROD FOREIGN KEY (IDORDENPRODUCCION) REFERENCES ORDENPRODUCCION(IDORDENPRODUCCION);

alter table ORDENPRODUCCION ADD (PRODUCTOPADRE NUMBER(24,0) NULL);
alter table ORDENPRODUCCION ADD (COSTINSUMOPRINCIPAL NUMBER(16,2) NULL);
alter table ORDENPRODUCCION add constraint fk_PRODUCTO_PADRE FOREIGN KEY (PRODUCTOPADRE) REFERENCES ORDENPRODUCCION(IDORDENPRODUCCION);

--permiso para crear un producto en base a otro
INSERT  INTO FUNCIONALIDAD (IDFUNCIONALIDAD,CODIGO,DESCRIPCION,PERMISO,NOMBRERECURSO,IDMODULO)
  VALUES (399, 'CREATEPRODUCTPORTION', NULL, 15,'productionOrderForPlanning.orderProduction.createProductoPortion', 6); 
INSERT  INTO DERECHOACCESO (IDFUNCIONALIDAD,IDROL,PERMISO,IDCOMPANIA,IDMODULO)
  VALUES (399, 50, 15, 1, 6);   
-- select * from funcionalidad  
--COMMIT
