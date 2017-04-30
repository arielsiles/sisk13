--Khipus MRP PROD-69: Arreglar el control de los insumos que no deben ser tomados en cuenta.
--Diego H. Loza Fernandez
--Fecha de creacion: 12/11/2013
Create table ESTADOARTICULO (
	IDESTADOARTICULO NUMBER(24,0) NOT NULL ,
  COD_ART	VARCHAR2(6 BYTE) NOT NULL,
  NO_CIA	VARCHAR2(2 BYTE) NOT NULL,
  ESTADO VARCHAR2(255) NULL,
  DESCRIPCION VARCHAR2(255) NULL,
  IDCOMPANIA NUMBER(24,0) NOT NULL ,
 CONSTRAINT pk_ESTADOARTICULO primary key (IDESTADOARTICULO) 
);
-- DROP TABLE ESTADOARTICULO;
ALTER TABLE ESTADOARTICULO
ADD CONSTRAINT fk_INV_ARTICULOS_ESTADO FOREIGN KEY (NO_CIA,COD_ART) REFERENCES WISE.INV_ARTICULOS(NO_CIA,COD_ART);
--
Insert into ESTADOARTICULO (IDESTADOARTICULO,COD_ART,NO_CIA,ESTADO,DESCRIPCION,IDCOMPANIA) 
values ('1','93','01','NOVERIFICABLE','ESTE ARTICULO NO DEBE SER BERIFICADO','1');  
--delete estadoarticulo;  
--COMMIT
--

/*

SELECT pe.nombres, pe.apellidopaterno ,pe.apellidomaterno,pm.fechaexpiralicenciaimpuesto FROM productormateriaprima PM
INNER JOIN persona PE
ON pm.idproductormateriaprima = pe.idpersona
WHERE pm.fechaexpiralicenciaimpuesto <> to_date('31/12/2013','DD/MM/YYYY');
*/

--SELECT * FROM PERSONA;
--COMMIT                     
--backup de la tabla
create table productormateriaprima_tmp as 
--select * from productormateriaprima;
--crear los nuevos cambios
alter table PRODUCTORMATERIAPRIMA add (
                                        LICENCIAIMPUESTOS2011 VARCHAR2(200 BYTE) NULL,
                                        FECHAINIIMPUESTO2011 DATE NULL,
                                        FECHAFINIMPUESTO2011 DATE NULL
                                      );
--copiar a los campos auxiliare
UPDATE PRODUCTORMATERIAPRIMA PM
SET  LICENCIAIMPUESTOS2011 = pm.licenciaimpuestos
    ,FECHAINIIMPUESTO2011 = pm.fechainicialicenciaimpuesto
    ,FECHAFINIMPUESTO2011 = pm.fechaexpiralicenciaimpuesto;
--cambiar la fecha al 31 de octubre    
UPDATE PRODUCTORMATERIAPRIMA PM
SET  pm.fechafinimpuesto2011 = to_date('31/10/2013','DD/MM/YYYY')
WHERE pm.fechaexpiralicenciaimpuesto = to_date('31/12/2013','DD/MM/YYYY');    
--borrar los campos anteriores
UPDATE PRODUCTORMATERIAPRIMA PM
SET  pm.licenciaimpuestos = null
    ,pm.fechainicialicenciaimpuesto = null
    ,pm.fechaexpiralicenciaimpuesto = null;
--commit    
--select * from PRODUCTORMATERIAPRIMA;
