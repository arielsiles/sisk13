ALTER TABLE `documentocontable` ADD COLUMN tasas DECIMAL(12,2) AFTER fecha;
UPDATE documentocontable SET tasas = 0;

ALTER TABLE `documentocontable` ADD COLUMN descuentos DECIMAL(12,2) AFTER nombre;
UPDATE documentocontable SET descuentos = 0;

ALTER TABLE `documentocontable` ADD COLUMN nocf DECIMAL(12,2) AFTER tasas;
UPDATE documentocontable SET nocf = 0;