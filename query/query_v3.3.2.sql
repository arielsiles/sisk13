/** 17.05.2019 **/
UPDATE SECUENCIA SET VALOR=(SELECT MAX(E.`idcostosindirectosconf`)+1 FROM costosindirectosconf E) WHERE TABLA='COSTOSINDIRECTOSCONF';
UPDATE SECUENCIA SET VALOR=(SELECT MAX(z.`idcostosindirectos`)+1 FROM costosindirectos z) WHERE TABLA='COSTOSINDIRECTOS';
UPDATE SECUENCIA SET VALOR=(SELECT MAX(z.`idperiodocostoindirecto`)+1 FROM periodocostoindirecto z) WHERE TABLA='PERIODOCOSTOINDIRECTO';

/** 21.05.2019 **/
ALTER TABLE pr_insumoformula ADD COLUMN idform BIGINT(20) NULL AFTER idformula;
ALTER TABLE pr_insumoformula ADD FOREIGN KEY (idform) REFERENCES pr_formula(idformula);
