
CREATE TABLE _sequence
(
	seq_name VARCHAR(50) NOT NULL PRIMARY KEY,
	seq_val INT UNSIGNED NOT NULL
);
--select getNextSeq('VALE') 
DELIMITER //
DROP FUNCTION IF EXISTS getNextSeq//
 
CREATE FUNCTION getNextSeq(sSeqName VARCHAR(50)) RETURNS VARCHAR(10)
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
END//
