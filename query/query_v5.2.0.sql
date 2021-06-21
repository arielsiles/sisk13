-- 27.04.2021
-- alter table categoriacliente add column idtipocaja bigint(20)after tipo;
-- alter table categoriacliente add foreign key (idtipocaja) references tipocaja (idtipocaja);

ALTER TABLE tipocaja ADD COLUMN idcategoriacliente BIGINT(20)AFTER no_cia; -- ejec ilva

-- Configuraciones para release
-- 0. Crear Almacen Agencia y asignar cuenta contable (1510110203 - Productos Agencia Central)

INSERT INTO `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`) 
VALUES('1510110203','Productos Terminados - Agencia Central','1500000000',NULL,NULL,'I','7','2','','0','01',NULL,'A','S',NULL,NULL,'0.00','P','0.00','0.000000','0.000000','0.000000','0.000000',NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S','0.00','0.00');

-- 1. Crear Categorias Cliente con los productos y precios para fijar como los mas vendidos
-- Crear productos para Agencia Central
-- 

UPDATE tipocaja t SET t.`idcategoriacliente` = 1 WHERE idtipocaja = 1;
UPDATE tipocaja t SET t.`idcategoriacliente` = XX WHERE idtipocaja = 3; -- Para Agencia Central, categoria creada para productos agencia

INSERT INTO tipopedido VALUES (8, 'TRANSFERENCIA', 'TRANSFER');

UPDATE inv_tipodocs i SET i.`cod_doc` = 'RPT', i.`desc_def` = 'RECEPCION POR TRANSFERENCIA', i.`descri` = 'RECEPCION P.T.', i.`tipo_vale` = 'R' WHERE i.`cod_doc` = 'TPT';

-- 1.1. Crear un tipo de caja para las diferentes areas de ventas, y configurar cuentas contables
-- 	Ventas planta, agencia, veterinaria, comercial
-- 1.1. Crear cajas y asignar usuarios (Alejandra, Delina)
-- 1.2. En categoriacliente fijar tipo segun corresponde:
--	FACTORY_LIST *
--	AGENCY_LIST *
--	VET_LIST

-- 2. Relacionar en tipo de caja (tipocaja) con la categoriacliente q corresponde


--
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(425, 'CASHSALE_INVOICE', 1, 1, 'Functionality.customers.registerCashSaleInvoice', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(426, 'CASHSALE_REGISTER', 1, 1, 'Functionality.customers.registerCashSale', 1);


