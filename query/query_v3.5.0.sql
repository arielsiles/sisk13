/** 24.06.2019 **/
ALTER TABLE transaccioncredito ADD COLUMN traspaso INT(1) AFTER tipo;
UPDATE transaccioncredito t SET t.`traspaso` = 0;

UPDATE _sequence s SET s.`seq_val` = 2 WHERE s.`seq_name` = 'CT';

/** 26.26.2019 **/
ALTER TABLE transaccioncredito ADD COLUMN diff DECIMAL(13,2) AFTER importe;
UPDATE transaccioncredito t SET t.`diff` = 0 WHERE t.`diff` IS NULL;