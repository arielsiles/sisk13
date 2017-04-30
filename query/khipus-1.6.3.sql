-- Actualizando columna CONTROL_VALORADO
UPDATE inv_articulos
SET control_valorado = 'S'
WHERE control_valorado IS NULL;

-- Actualizando columna SALDO_MON
UPDATE inv_articulos
SET saldo_mon = 0
WHERE saldo_mon IS NULL;

-- Insertando algunos articulos a INV_INVENTARIO
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('129','01','2','0.00','0');
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('130','01','2','0.00','0');
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('131','01','2','0.00','0');
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('132','01','2','0.00','0');
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('133','01','2','0.00','0');
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('143','01','2','0.00','0');
INSERT INTO `inv_inventario` (`cod_art`, `no_cia`, `cod_alm`, `saldo_uni`, `version`) VALUES('118','01','2','0.00','0');