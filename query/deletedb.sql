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

-- 
-- 26.11.2021 Preparando ambiente pruebas para SFE
DELETE FROM movimiento WHERE FECHA_FACTURA < '2021-10-01';
DELETE FROM sf_tmpdet;
DELETE FROM articulos_pedido WHERE IDVENTADIRECTA IS NOT NULL;
DELETE FROM ventadirecta;

UPDATE pedidos p SET p.`id_tmpenc` = NULL;

DELETE FROM pago;

DELETE FROM `cxp_lcompras`;
DELETE FROM `documentocompra`;
DELETE FROM `documentocontable`;
UPDATE inv_vales i SET i.`idtmpenc` = NULL;

DELETE FROM sf_tmpenc;

DELETE FROM ventaarticulo;
DELETE FROM ventacliente;


DELETE FROM pr_insumo;
DELETE FROM pr_producto;
DELETE FROM pr_produccion;
DELETE FROM pr_plan;
DELETE FROM pr_material;


DELETE FROM inv_articulos
WHERE cod_alm NOT IN (2);









