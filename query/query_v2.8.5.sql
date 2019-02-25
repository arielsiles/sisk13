/** 24.02.2019 **/
-- inta
-- intb
ALTER TABLE tipocuenta ADD COLUMN intb DECIMAL(4,2) AFTER interes;
ALTER TABLE tipocuenta ADD COLUMN inta DECIMAL(4,2) AFTER interes;

ALTER TABLE tipocuenta DROP COLUMN interes;

/* Para fondo CISC */
UPDATE tipocuenta t SET t.`inta` = 2 WHERE t.`idtipocuenta` IN (1, 2);
UPDATE tipocuenta t SET t.`inta` = 5 WHERE t.`idtipocuenta` IN (3);
UPDATE tipocuenta t SET t.`intb` = 1.5 WHERE t.`idtipocuenta` IN (1, 2);
UPDATE tipocuenta t SET t.`intb` = 0 WHERE t.`idtipocuenta` IN (3);

/* Para fondo Lechero */
-- todo
