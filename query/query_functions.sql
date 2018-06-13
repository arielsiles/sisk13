-- 1. 
DELIMITER $$
CREATE FUNCTION `getNextSeq`(sSeqName VARCHAR(50)) RETURNS VARCHAR(10) CHARSET utf8
BEGIN
    DECLARE nLast_val VARCHAR(10);
 
    SET nLast_val =  (SELECT seq_val
                          FROM _sequence
                          WHERE seq_name = sSeqName);
    IF nLast_val IS NULL THEN
        SET nLast_val = 1;
        INSERT INTO _sequence (seq_name,seq_val)
        VALUES (sSeqName,nLast_Val);
    ELSE
        SET nLast_val = nLast_val + 1;
        UPDATE _sequence SET seq_val = nLast_val
        WHERE seq_name = sSeqName;
    END IF;
    
    RETURN nLast_val;
END$$
DELIMITER ;

-- 2.
DELIMITER $$
CREATE FUNCTION `newId_sf_tmpdet`() RETURNS INT(11)
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

-- 3.
DELIMITER $$
CREATE FUNCTION `newId_sf_tmpenc`() RETURNS INT(11)
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

-- 4.
DELIMITER $$
CREATE FUNCTION `next_tmpenc`() RETURNS VARCHAR(10) CHARSET utf8
BEGIN
    DECLARE act_trans VARCHAR(10);
    SET act_trans = 0;
    SELECT IFNULL(MAX(no_trans),0) INTO act_trans FROM khipus.sf_tmpenc;
  RETURN act_trans+1;
    END
$$
DELIMITER ;


-- 5.
DELIMITER $$
CREATE FUNCTION `sigte_trans`() RETURNS VARCHAR(10) CHARSET utf8
BEGIN
    DECLARE act_trans VARCHAR(10);
    SET act_trans = 0;
    SELECT IFNULL(MAX(no_trans),0) INTO act_trans FROM khipus.inv_vales;
  RETURN act_trans+1;
    END
$$
DELIMITER ;

-- 6.
DELIMITER $$
CREATE FUNCTION `newId_inv_inventario_detalle`() RETURNS INT(11)
BEGIN
    DECLARE nLast_val INT;
    SET nLast_val =  (SELECT valor
                          FROM secuencia
                          WHERE tabla = 'inv_inventario_detalle');
        SET nLast_val = nLast_val + 1;
        UPDATE secuencia SET valor = nLast_val
        WHERE tabla = 'inv_inventario_detalle';
    RETURN nLast_val;
END $$
DELIMITER ;

