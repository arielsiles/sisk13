UPDATE ventadirecta v SET v.`IDMOVIMIENTO` = NULL;
UPDATE pedidos p SET p.`IDMOVIMIENTO` = NULL;
UPDATE sf_tmpdet d SET d.`idmovimiento` = NULL;

DELETE FROM movimiento;
DELETE FROM articulos_pedido;
DELETE FROM ventadirecta;
DELETE FROM pedidos;
DELETE FROM sf_tmpdet;
DELETE FROM pago;

DELETE FROM documentocompra;
DELETE FROM documentocontable;
DELETE FROM inv_movdet;
DELETE FROM inv_mov;
DELETE FROM inv_vales;
DELETE FROM sf_tmpenc;


DELETE FROM com_detoc;
DELETE FROM com_encoc;
DELETE FROM com_af_detoc;

-- eliminando planillas generadas
DELETE FROM reportecontrol 		 	;
DELETE FROM `planillafiscalporcategoria` 	;
DELETE FROM `planillatributariaporcategoria` 	;
DELETE FROM `planillaadministrativos` 		;
DELETE FROM `planillagenerada` 			;

DELETE FROM `gestionplanilla`;
DELETE FROM `planillafiscal`;
DELETE FROM `planillatributaria`;
DELETE FROM ciclogeneracionplanilla;

DELETE FROM `movimientosueldo`;
DELETE FROM `movimientosalarioproductor`;
DELETE FROM `movimientosalariogab`;

DELETE FROM `acopiomateriaprima`;
DELETE FROM `sesionacopio`;
DELETE FROM `registroacopio`;
DELETE FROM `planillaacopio`;

DELETE FROM `descuentproductmateriaprima`;
DELETE FROM `registropagomateriaprima`;
DELETE FROM `planillapagomateriaprima`;

DELETE FROM `notarechazomateriaprima`;

DELETE FROM `transaccioncredito`;
DELETE FROM `credito`;
DELETE FROM `cuenta`;

DELETE FROM bonoconseguido;






