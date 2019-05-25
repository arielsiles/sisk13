/** 17.05.2019 **/
UPDATE SECUENCIA SET VALOR=(SELECT MAX(E.`idcostosindirectosconf`)+1 FROM costosindirectosconf E) WHERE TABLA='COSTOSINDIRECTOSCONF';
UPDATE SECUENCIA SET VALOR=(SELECT MAX(z.`idcostosindirectos`)+1 FROM costosindirectos z) WHERE TABLA='COSTOSINDIRECTOS';
UPDATE SECUENCIA SET VALOR=(SELECT MAX(z.`idperiodocostoindirecto`)+1 FROM periodocostoindirecto z) WHERE TABLA='PERIODOCOSTOINDIRECTO';

/** 21.05.2019 **/
ALTER TABLE pr_insumoformula ADD COLUMN idform BIGINT(20) NULL AFTER idformula;
ALTER TABLE pr_insumoformula ADD FOREIGN KEY (idform) REFERENCES pr_formula(idformula);
ALTER TABLE pr_formula ADD COLUMN totaleq DECIMAL(16,2) NOT NULL AFTER estado;

/** 22.05.2019 **/
ALTER TABLE pr_produccion ADD COLUMN costototal DECIMAL(16,2) DEFAULT 0 AFTER estado;
ALTER TABLE pr_produccion ADD COLUMN totalmp DECIMAL(16,2) DEFAULT 0 AFTER costototal;

/** 24.05.2019 **/
ALTER TABLE pr_producto ADD COLUMN costo DECIMAL(16,2) DEFAULT 0 AFTER cantidad;

ALTER TABLE inv_articulos ADD COLUMN med_pr VARCHAR(6) AFTER cod_med;
ALTER TABLE inv_articulos ADD COLUMN cant_pr DECIMAL(16, 2) AFTER med_pr;
