-- version: 1.8.0
-- ----------------------

-- Funcion newId_sf_tmpenc
DELIMITER $$
CREATE FUNCTION `newId_sf_tmpenc`() RETURNS INT
BEGIN
    DECLARE nLast_val INT;
    SET nLast_val =  (SELECT valor
                          FROM secuencia
                          WHERE tabla = 'sf_tmpenc');
        SET nLast_val = nLast_val + 1;
        UPDATE secuencia SET valor = nLast_val
        WHERE tabla = 'sf_tmpenc';
    RETURN nLast_val;
END$$
DELIMITER ;

-- Funcion newId_sf_tmpdet
DELIMITER $$
CREATE FUNCTION `newId_sf_tmpdet`() RETURNS INT
BEGIN
    DECLARE nLast_val INT;
    SET nLast_val =  (SELECT valor
                          FROM secuencia
                          WHERE tabla = 'sf_tmpdet');
        SET nLast_val = nLast_val + 1;
        UPDATE secuencia SET valor = nLast_val
        WHERE tabla = 'sf_tmpdet';
    RETURN nLast_val;
END$$
DELIMITER ;


/** UNION datos de tablas SF_TMP, SFTMP **/
ALTER TABLE sftmpdet DROP FOREIGN KEY FKTMPENC;

UPDATE sftmpdet
SET id_tmpenc = id_tmpenc + 1490;

UPDATE sftmpenc
SET id_tmpenc = id_tmpenc + 1490;

UPDATE sftmpdet
SET id_tmpdet = id_tmpdet + 2960;


INSERT INTO sf_tmpenc
SELECT *
FROM sftmpenc;

INSERT INTO sf_tmpdet
SELECT *
FROM sftmpdet;

-- alter table sftmpdet ADD CONSTRAINT FKTMPENC FOREIGN KEY (id_tmpenc) references sftmpenc(id_tmpenc);

/** Add IDTMPENC to table DocumentoCompra **/
ALTER TABLE documentocompra ADD idtmpenc BIGINT(20);


/** DEBE is null, set 0 **/
UPDATE sf_tmpdet SET debe = 0
WHERE debe IS NULL

/** DEBE is null, set 0 **/
UPDATE sf_tmpdet SET haber = 0
WHERE haber IS NULL
;

/** 

Actualizar tabla SECUENCIA sf_tmpenc
Actualizar tabla SECUENCIA sf_tmpdet
**/
UPDATE secuencia SET valor = (SELECT MAX(id_tmpenc) FROM sf_tmpenc)
WHERE tabla = 'sf_tmpenc';

UPDATE secuencia SET valor = (SELECT MAX(id_tmpdet) FROM sf_tmpdet)
WHERE tabla = 'sf_tmpdet';

/**
Actualizar tabla _SEQUENCE: TR, CI, CB, NE
Actualizar tabla _SEQUENCE: ASIENTO (no_trans)

**/

UPDATE sf_tmpenc SET no_doc = no_trans
WHERE tipo_doc = 'CB'
;

UPDATE sf_tmpenc SET tipo_doc = 'CP'
WHERE tipo_doc = 'CHQ'
-- LUEGO TODOS LOS CP 

/** Insertando valor para la secuencia del documento TR **/
INSERT  INTO `_sequence`(`seq_name`,`seq_val`) VALUES ('TR', 197);

/** Actualizando sucursal **/
INSERT  INTO `sucursal`(`IDSUCURSAL`,`NOMBRE`) VALUES (1, 'ILVA');

UPDATE dosificacion
SET IDSUCURSAL = 1
WHERE IDDOSIFICACION = 1
;

UPDATE usuario
SET IDSUCURSAL = 1
WHERE usuario = 'root'
;


-- 

SELECT no_trans, COUNT(no_trans) 
FROM sf_tmpenc
GROUP BY no_trans
;

SELECT * FROM sf_tmpenc
-- update sf_tmpenc set no_doc = no_trans
WHERE tipo_doc = 'CP'
OR tipo_doc = 'CHQ'
;

SELECT * FROM sf_tmpenc
UPDATE sf_tmpenc SET tipo_doc = 'CP'
WHERE tipo_doc = 'CHQ'

