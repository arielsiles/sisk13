-- Fusion de cambios de estructura v6.0.17 -> v6.0.125 (FCISC). Solo DDL + catalogo de funcionalidad. Requiere MySQL 8.

-- ---------------------------------------------------------------------------
-- Helpers idempotentes. Se crean al inicio y se eliminan al final.
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS mg_add_col;
DROP PROCEDURE IF EXISTS mg_drop_col;
DROP PROCEDURE IF EXISTS mg_mod_col;
DROP PROCEDURE IF EXISTS mg_chg_col;
DROP PROCEDURE IF EXISTS mg_add_key;
DROP PROCEDURE IF EXISTS mg_drop_key;
DROP PROCEDURE IF EXISTS mg_add_fk;
DROP PROCEDURE IF EXISTS mg_drop_fk_col;
DROP PROCEDURE IF EXISTS mg_set_pk;
DROP PROCEDURE IF EXISTS mg_null_fk;
DROP PROCEDURE IF EXISTS mg_func_add;
DROP PROCEDURE IF EXISTS mg_func_del;

DELIMITER $$

-- Suelta la FK que exista sobre (tabla, columna), sin depender de su nombre.
CREATE PROCEDURE mg_drop_fk_col(IN t VARCHAR(64), IN c VARCHAR(64))
BEGIN
    DECLARE v_fk VARCHAR(64) DEFAULT NULL;
    SET v_fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c
                   AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
    IF v_fk IS NOT NULL THEN
        SET @mg_a = CONCAT('ALTER TABLE `', t, '` DROP FOREIGN KEY `', v_fk, '`');
        PREPARE mg_sa FROM @mg_a; EXECUTE mg_sa; DEALLOCATE PREPARE mg_sa;
    END IF;
END$$

CREATE PROCEDURE mg_add_col(IN t VARCHAR(64), IN c VARCHAR(64), IN d TEXT)
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t) = 1
       AND (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) = 0 THEN
        SET @mg_b = CONCAT('ALTER TABLE `', t, '` ADD COLUMN ', d);
        PREPARE mg_sb FROM @mg_b; EXECUTE mg_sb; DEALLOCATE PREPARE mg_sb;
    END IF;
END$$

CREATE PROCEDURE mg_drop_col(IN t VARCHAR(64), IN c VARCHAR(64))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) = 1 THEN
        CALL mg_drop_fk_col(t, c);
        SET @mg_c = CONCAT('ALTER TABLE `', t, '` DROP COLUMN `', c, '`');
        PREPARE mg_sc FROM @mg_c; EXECUTE mg_sc; DEALLOCATE PREPARE mg_sc;
    END IF;
END$$

CREATE PROCEDURE mg_mod_col(IN t VARCHAR(64), IN c VARCHAR(64), IN d TEXT)
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) = 1 THEN
        SET @mg_d = CONCAT('ALTER TABLE `', t, '` MODIFY COLUMN ', d);
        PREPARE mg_sd FROM @mg_d; EXECUTE mg_sd; DEALLOCATE PREPARE mg_sd;
    END IF;
END$$

-- Renombra o normaliza mayusculas. Se salta si el nombre destino ya existe con esa grafia exacta.
CREATE PROCEDURE mg_chg_col(IN t VARCHAR(64), IN o VARCHAR(64), IN n VARCHAR(64), IN d TEXT)
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = o) = 1
       AND (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t
               AND CAST(COLUMN_NAME AS BINARY) = CAST(n AS BINARY)) = 0 THEN
        SET @mg_e = CONCAT('ALTER TABLE `', t, '` CHANGE `', o, '` `', n, '` ', d);
        PREPARE mg_se FROM @mg_e; EXECUTE mg_se; DEALLOCATE PREPARE mg_se;
    END IF;
END$$

CREATE PROCEDURE mg_add_key(IN t VARCHAR(64), IN k VARCHAR(64), IN d TEXT)
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t) = 1
       AND (SELECT COUNT(*) FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND INDEX_NAME = k) = 0 THEN
        SET @mg_f = CONCAT('ALTER TABLE `', t, '` ADD ', d);
        PREPARE mg_sf FROM @mg_f; EXECUTE mg_sf; DEALLOCATE PREPARE mg_sf;
    END IF;
END$$

CREATE PROCEDURE mg_drop_key(IN t VARCHAR(64), IN k VARCHAR(64))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND INDEX_NAME = k) > 0 THEN
        SET @mg_g = CONCAT('ALTER TABLE `', t, '` DROP INDEX `', k, '`');
        PREPARE mg_sg FROM @mg_g; EXECUTE mg_sg; DEALLOCATE PREPARE mg_sg;
    END IF;
END$$

CREATE PROCEDURE mg_add_fk(IN t VARCHAR(64), IN f VARCHAR(64), IN d TEXT)
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t) = 1
       AND (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t
               AND CONSTRAINT_NAME = f AND CONSTRAINT_TYPE = 'FOREIGN KEY') = 0 THEN
        SET @mg_h = CONCAT('ALTER TABLE `', t, '` ADD CONSTRAINT `', f, '` ', d);
        PREPARE mg_sh FROM @mg_h; EXECUTE mg_sh; DEALLOCATE PREPARE mg_sh;
    END IF;
END$$

CREATE PROCEDURE mg_set_pk(IN t VARCHAR(64), IN want TEXT, IN newpk TEXT)
BEGIN
    DECLARE v_cur TEXT DEFAULT NULL;
    SET v_cur = (SELECT GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ',')
                   FROM information_schema.STATISTICS
                  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND INDEX_NAME = 'PRIMARY');
    IF v_cur IS NOT NULL AND LOWER(v_cur) <> LOWER(want) THEN
        SET @mg_i = CONCAT('ALTER TABLE `', t, '` DROP PRIMARY KEY, ADD PRIMARY KEY (', newpk, ')');
        PREPARE mg_si FROM @mg_i; EXECUTE mg_si; DEALLOCATE PREPARE mg_si;
    END IF;
END$$

-- Pone en NULL los valores que no existen en la tabla referenciada, para que la FK
-- se pueda crear. Es el mismo criterio del UPDATE de rescate de query_v6.0.113,
-- generalizado a todas las columnas con FK. Tambien limpia cadenas vacias, que
-- tampoco satisfacen una FK.
CREATE PROCEDURE mg_null_fk(IN t VARCHAR(64), IN c VARCHAR(64), IN rt VARCHAR(64), IN rc VARCHAR(64))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) = 1 THEN
        SET @mg_j = CONCAT('UPDATE `', t, '` SET `', c, '` = NULL WHERE `', c, '` IS NOT NULL',
                           ' AND NOT EXISTS (SELECT 1 FROM `', rt, '` mgr WHERE mgr.`', rc, '` = `', t, '`.`', c, '`)');
        PREPARE mg_sj FROM @mg_j; EXECUTE mg_sj; DEALLOCATE PREPARE mg_sj;
    END IF;
END$$

-- Alta de funcionalidad con id = MAX+1. No hace nada si el codigo ya existe.
CREATE PROCEDURE mg_func_add(IN p_cod VARCHAR(40), IN p_desc LONGTEXT, IN p_mod BIGINT,
                             IN p_perm TINYINT, IN p_res VARCHAR(100), IN p_cia BIGINT)
BEGIN
    DECLARE v_id BIGINT;
    IF (SELECT COUNT(*) FROM funcionalidad WHERE codigo = p_cod) = 0 THEN
        SET v_id = (SELECT COALESCE(MAX(idfuncionalidad), 0) + 1 FROM funcionalidad);
        INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
        VALUES (v_id, p_cod, p_desc, p_mod, p_perm, p_res, p_cia);
    END IF;
END$$

-- Baja de funcionalidad y de sus grants por rol.
CREATE PROCEDURE mg_func_del(IN p_cod VARCHAR(40))
BEGIN
    DELETE FROM derechoacceso
     WHERE idfuncionalidad IN (SELECT idfuncionalidad FROM funcionalidad WHERE codigo = p_cod);
    DELETE FROM funcionalidad WHERE codigo = p_cod;
END$$

DELIMITER ;


-- ===========================================================================
-- v6.0.17_terdemol
-- ===========================================================================
CALL mg_add_col('arcgms',    'tipo_gasto', '`tipo_gasto` varchar(50) AFTER tipo');
CALL mg_add_col('inv_vales', 'cta_gasto',  '`cta_gasto` varchar(20) AFTER estado');


-- ===========================================================================
-- v6.0.21.1
-- ===========================================================================
CALL mg_add_col('configuracion', 'url_reversion_cancel_bill', '`url_reversion_cancel_bill` VARCHAR(300) AFTER url_ping');
CALL mg_func_add('REVERSION_CANCEL_BILL_SFE', NULL, 1, 1, 'Functionality.customers.reversionCancelBillSFE', 1);


-- ===========================================================================
-- v6.0.21.2
-- ===========================================================================
CREATE TABLE IF NOT EXISTS sin_tipopuntoventa (
    idtipopuntoventa    bigint PRIMARY KEY,
    codigo_clasificador int,
    descripcion         VARCHAR(255) NOT NULL
);

CALL mg_add_col('configuracion', 'url_point_of_sale_types', '`url_point_of_sale_types` VARCHAR(300) AFTER url_reversion_cancel_bill');
CALL mg_add_col('configuracion', 'url_register_pos',        '`url_register_pos` VARCHAR(300) AFTER url_point_of_sale_types');
CALL mg_add_col('configuracion', 'url_close_pos',           '`url_close_pos` VARCHAR(300) AFTER url_register_pos');
CALL mg_add_col('sucursal',      'tipo_pos',                '`tipo_pos` int AFTER docsector');
CALL mg_add_col('sucursal',      'desc_tipopos',            '`desc_tipopos` varchar(100) AFTER tipo_pos');
CALL mg_add_col('sucursal',      'pos_activo',              '`pos_activo` int AFTER tipo_pos');
CALL mg_add_col('pedidos',       'idmotivoanulacion',       '`idmotivoanulacion` bigint AFTER idmovimiento');
CALL mg_add_col('pedidos',       'envio',                   '`envio` int AFTER idmotivoanulacion');

-- La FK original era anonima (dosificacion_ibfk_N). Se le da nombre para poder verificarla.
CALL mg_add_fk('dosificacion', 'fk_dosificacion_sucursal', 'FOREIGN KEY (idsucursal) REFERENCES sucursal (idsucursal)');

CALL mg_func_add('SYNC_BILLING_SFE',    NULL, 2, 1, 'Functionality.admin.syncCatalogsBillingSFE', 1);
CALL mg_func_add('DIRECTINVOICECANCEL', NULL, 1, 1, 'Functionality.customers.directInvoiceCancel', 1);


-- ===========================================================================
-- v6.0.21.3
-- ===========================================================================
CALL mg_add_col('pedidos',        'idmetodopago', '`idmetodopago` bigint AFTER idmotivoanulacion');
CALL mg_add_col('sin_metodopago', 'activo',       '`activo` int AFTER descripcion');


-- ===========================================================================
-- v6.0.21_terdemol
-- ===========================================================================
CALL mg_func_add('WAREHOUSEVOUCHER_DESTINATION_REPORT', NULL, 5, 1, 'Reports.kardex.movement.warehouseVoucherDestinationReport', 1);


-- ===========================================================================
-- v6.0.23_terdemol
-- ===========================================================================
CALL mg_add_col('inv_grupos', 'cta_baja', '`cta_baja` varchar(31) AFTER cta_gasto');
CALL mg_add_col('inv_vales',  'baja',     '`baja` int AFTER cta_gasto');


-- ===========================================================================
-- v6.0.26_terdemol
-- ===========================================================================
CALL mg_func_add('WAREHOUSE_INPUTS_OUTPUTS_REPORT', NULL, 5, 1, 'menu.warehouse.report.warehouseReportInputsOutputs', 1);


-- ===========================================================================
-- v6.0.28
-- ===========================================================================
CALL mg_add_col('impuestoproductor', 'created_at', '`created_at` DATETIME NULL');
CALL mg_add_col('impuestoproductor', 'created_by', '`created_by` VARCHAR(100) NULL');
CALL mg_add_col('impuestoproductor', 'updated_at', '`updated_at` DATETIME NULL');
CALL mg_add_col('impuestoproductor', 'updated_by', '`updated_by` VARCHAR(100) NULL');

-- [!] La FK falla si impuestoproductor tiene idproductormateriaprima huerfanos.
--     Verificado en khipus local: 0 huerfanos.
CALL mg_add_fk('impuestoproductor', 'fk_impuestoproductor_productormp',
               'FOREIGN KEY (idproductormateriaprima) REFERENCES productormateriaprima (idproductormateriaprima)');


-- ===========================================================================
-- v6.0.29
-- ===========================================================================
CALL mg_add_col('sin_actividad', 'activo', '`activo` TINYINT(1)');


-- ===========================================================================
-- v6.0.30_terdemol
-- ===========================================================================
CALL mg_add_col('arcgms', 'cta_fe', '`cta_fe` varchar(2) AFTER tipo');


-- ===========================================================================
-- v6.0.36_terdemol
-- ===========================================================================
CALL mg_add_col('xpr_produccion', 'fechainicio', '`fechainicio` timestamp NULL DEFAULT NULL AFTER descripcion');
CALL mg_add_col('xpr_produccion', 'fechafin',    '`fechafin` timestamp NULL DEFAULT NULL AFTER fechainicio');


-- ===========================================================================
-- v6.0.40
-- ===========================================================================
-- [!] Ambas FK fallan si hay huerfanos. Verificado en khipus local: 0 huerfanos.
--     El script original limpiaba antes con DELETE (excluido de esta fusion).
CALL mg_add_fk('descuentproductmateriaprima', 'fk_descuentproductmp_productormateriaprima',
               'FOREIGN KEY (idproductormateriaprima) REFERENCES productormateriaprima (idproductormateriaprima)');
CALL mg_add_fk('movimientosalarioproductor', 'fk_movsalarioproductor_productormateriaprima',
               'FOREIGN KEY (idproductormateriaprima) REFERENCES productormateriaprima (idproductormateriaprima)');


-- ===========================================================================
-- v6.0.42_terdemol
-- ===========================================================================
CALL mg_add_col('acopiomp', 'pesoprov', '`pesoprov` decimal(12,2) AFTER codigo');


-- ===========================================================================
-- v6.0.43_terdemol
-- ===========================================================================
CALL mg_func_add('SUBGROUP_INVENTORY_REPORT', NULL, 5, 1, 'menu.warehouse.report.inventorySubGroupReport', 1);


-- ===========================================================================
-- v6.0.44_terdemol
-- ===========================================================================
CALL mg_func_add('GROUPED_GENERAL_INVENTORY_REPORT', NULL, 5, 1, 'menu.warehouse.report.inventoryGroupedReport', 1);


-- ===========================================================================
-- v6.0.46_terdemol
-- ===========================================================================
CALL mg_add_col('com_encoc',   'idtmpenc', '`idtmpenc` bigint AFTER no_orden');
CALL mg_add_col('inv_destino', 'activo',   '`activo` int AFTER nombre');


-- ===========================================================================
-- v6.0.47
-- ===========================================================================
CREATE TABLE IF NOT EXISTS restriccion_acopio_productor (
    idrestriccion_acopio_productor BIGINT        NOT NULL,
    idproductormateriaprima        BIGINT        NOT NULL,
    cupolitrosdia                  DECIMAL(16,2) NOT NULL,
    precioexcedentehabil           DECIMAL(9,2)  NOT NULL,
    precioexcedentedomingo         DECIMAL(9,2)  NOT NULL,
    fechaini                       DATE          NOT NULL,
    fechafin                       DATE          NOT NULL,
    estado                         VARCHAR(10)   NOT NULL,
    PRIMARY KEY (idrestriccion_acopio_productor),
    CONSTRAINT fk_restacopio_productor
        FOREIGN KEY (idproductormateriaprima)
        REFERENCES productormateriaprima (idproductormateriaprima)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- El modulo se hereda de RESERVPRODUCERMILK, igual que en el script original.
SET @mg_mod_rpm = (SELECT idmodulo FROM funcionalidad WHERE codigo = 'RESERVPRODUCERMILK' LIMIT 1);

CALL mg_func_add('PRODUCERCOLLECTIONRESTRICTION',
                 'Restriccion de Acopio por Productor (cupo/excedente)',
                 @mg_mod_rpm, 15, 'Functionality.production.producerCollectionRestriction', 1);

CALL mg_add_col('planillapagomateriaprima', 'tipoplanilla', '`tipoplanilla` VARCHAR(20) NOT NULL DEFAULT ''NORMAL''');
CALL mg_add_col('planillapagomateriaprima', 'tipodia',      '`tipodia` VARCHAR(10) NOT NULL DEFAULT ''NINGUNO''');

CREATE TABLE IF NOT EXISTS precio_acopio_leche (
    idprecio_acopio_leche  BIGINT        NOT NULL,
    preciohabil            DECIMAL(9,2)  NOT NULL,
    preciodomingo          DECIMAL(9,2)  NOT NULL,
    precioexcedentehabil   DECIMAL(9,2)  NOT NULL,
    precioexcedentedomingo DECIMAL(9,2)  NOT NULL,
    fechaini               DATE          NOT NULL,
    fechafin               DATE          NOT NULL,
    estado                 VARCHAR(10)   NOT NULL,
    PRIMARY KEY (idprecio_acopio_leche)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('MILKPRICECONFIG',
                 'Precios de Acopio de Leche (habil/domingo/excedente, con vigencia)',
                 @mg_mod_rpm, 15, 'Functionality.production.milkPriceConfig', 1);

CALL mg_add_col('movimientosalarioproductor', 'saldo', '`saldo` DECIMAL(16,2) NOT NULL DEFAULT 0');

CREATE TABLE IF NOT EXISTS aplicacion_descuento_productor (
    idaplicacion_descuento_productor BIGINT        NOT NULL,
    idmovimientosalarioproductor     BIGINT        NOT NULL,
    idregistropagomateriaprima       BIGINT        NOT NULL,
    montoaplicado                    DECIMAL(16,2) NOT NULL,
    fecha                            DATE          NOT NULL,
    idcompania                       BIGINT        NOT NULL,
    PRIMARY KEY (idaplicacion_descuento_productor),
    CONSTRAINT fk_aplicdesc_movimiento
        FOREIGN KEY (idmovimientosalarioproductor)
        REFERENCES movimientosalarioproductor (idmovimientosalarioproductor),
    CONSTRAINT fk_aplicdesc_registro
        FOREIGN KEY (idregistropagomateriaprima)
        REFERENCES registropagomateriaprima (idregistropagomateriaprima)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_add_col('planillapagomateriaprima', 'idcomprobante', '`idcomprobante` BIGINT NULL');


-- ===========================================================================
-- v6.0.47_terdemol
-- ===========================================================================
CALL mg_add_col('configuracion', 'res_perdida',  '`res_perdida` varchar(20)');
CALL mg_add_col('configuracion', 'res_utilidad', '`res_utilidad` varchar(20)');


-- ===========================================================================
-- v6.0.48
-- ===========================================================================
CALL mg_func_add('RAWMATERIALPAYROLL',         'Acopio - Generar planillas (acceso a la lista/pantalla)', @mg_mod_rpm, 15, 'Functionality.production.rawMaterialPayRoll',          1);
CALL mg_func_add('RAWMATERIALPAYROLLGENERATE', 'Acopio - Generar planilla de pago',                       @mg_mod_rpm, 15, 'Functionality.production.rawMaterialPayRoll.generate', 1);
CALL mg_func_add('RAWMATERIALPAYROLLAPPROVE',  'Acopio - Aprobar planilla',                               @mg_mod_rpm, 15, 'Functionality.production.rawMaterialPayRoll.approve',  1);
CALL mg_func_add('RAWMATERIALPAYROLLACCOUNT',  'Acopio - Contabilizar planilla',                          @mg_mod_rpm, 15, 'Functionality.production.rawMaterialPayRoll.account',  1);
CALL mg_func_add('RAWMATERIALPAYROLLREVERT',   'Acopio - Revertir planilla',                              @mg_mod_rpm, 15, 'Functionality.production.rawMaterialPayRoll.revert',   1);
CALL mg_func_add('RAWMATERIALPAYROLLDELETE',   'Acopio - Borrar planillas',                               @mg_mod_rpm, 15, 'Functionality.production.rawMaterialPayRoll.delete',   1);


-- ===========================================================================
-- v6.0.48_terdemol
-- ===========================================================================
CALL mg_add_col('pagoordencompra', 'prov_aux',     '`prov_aux` varchar(6) AFTER nombrebeneficiario');
CALL mg_add_col('pagoordencompra', 'cuentarendir', '`cuentarendir` varchar(20) AFTER prov_aux');
CALL mg_add_col('sf_tmpenc',       'created_at',   '`created_at` datetime');
CALL mg_add_col('sf_tmpenc',       'created_by',   '`created_by` varchar(100)');
CALL mg_add_col('sf_tmpenc',       'updated_at',   '`updated_at` datetime');
CALL mg_add_col('sf_tmpenc',       'updated_by',   '`updated_by` varchar(100)');
CALL mg_add_col('com_encoc',       'created_at',   '`created_at` datetime');
CALL mg_add_col('com_encoc',       'created_by',   '`created_by` varchar(100)');
CALL mg_add_col('com_encoc',       'updated_at',   '`updated_at` datetime');
CALL mg_add_col('com_encoc',       'updated_by',   '`updated_by` varchar(100)');


-- ===========================================================================
-- v6.0.49_terdemol
-- ===========================================================================
CALL mg_add_col('configuracion', 'oc_pagodefault', '`oc_pagodefault` varchar(20)');
CALL mg_add_col('inv_vales',     'created_at',     '`created_at` datetime');
CALL mg_add_col('inv_vales',     'created_by',     '`created_by` varchar(100)');
CALL mg_add_col('inv_vales',     'updated_at',     '`updated_at` datetime');
CALL mg_add_col('inv_vales',     'updated_by',     '`updated_by` varchar(100)');

CREATE TABLE IF NOT EXISTS xpr_linea (
    idlinea    bigint(20) NOT NULL,
    nombre     varchar(255),
    version    bigint,
    idcompania bigint(20),
    PRIMARY KEY (idlinea),
    CONSTRAINT fk_xpr_linea_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
);

CALL mg_add_col('xpr_proceso', 'idlinea',  '`idlinea` bigint(20) AFTER estado');
CALL mg_add_fk('xpr_proceso',  'fk_xpr_proceso_linea', 'FOREIGN KEY (idlinea) REFERENCES xpr_linea (idlinea)');
CALL mg_add_col('xpr_proceso', 'posicion', '`posicion` int(10) AFTER codigo');

CALL mg_func_add('PRODUCTION_LINE', NULL, 11, 15, 'Functionality.xproduction.productionLines', 1);

CALL mg_add_col('xpr_linea', 'codigo', '`codigo` varchar(100) AFTER idlinea');


-- ===========================================================================
-- v6.0.50_terdemol
-- ===========================================================================
CALL mg_add_col('inv_destino',    'tipo_area', '`tipo_area` varchar(100) AFTER activo');
CALL mg_add_col('inv_vales',      'idproceso', '`idproceso` bigint(20) AFTER iddestino');
CALL mg_add_fk('inv_vales',       'fk_inv_vales_proceso', 'FOREIGN KEY (idproceso) REFERENCES xpr_proceso (idproceso)');
CALL mg_add_col('inv_vales',      'cod_prod',  '`cod_prod` varchar(6) AFTER idproceso');
CALL mg_add_col('xpr_produccion', 'idlinea',   '`idlinea` bigint(20) AFTER idproceso');
CALL mg_add_fk('xpr_produccion',  'fk_xpr_produccion_linea', 'FOREIGN KEY (idlinea) REFERENCES xpr_linea (idlinea)');


-- ===========================================================================
-- v6.0.51_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS detalleanalitica (
    iddetalleanalitica BIGINT       NOT NULL,
    nombre             VARCHAR(255) NOT NULL,
    iddestino          BIGINT       NOT NULL,
    idcompania         BIGINT       NOT NULL,
    version            BIGINT       NOT NULL,
    PRIMARY KEY (iddetalleanalitica),
    CONSTRAINT fk_detanalitica_destino  FOREIGN KEY (iddestino)  REFERENCES inv_destino (iddestino),
    CONSTRAINT fk_detanalitica_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB;

CALL mg_add_col('inv_vales', 'iddetalleanalitica', '`iddetalleanalitica` BIGINT AFTER cod_prod');
CALL mg_add_fk('inv_vales',  'fk_inv_vales_detanalitica', 'FOREIGN KEY (iddetalleanalitica) REFERENCES detalleanalitica (iddetalleanalitica)');


-- ===========================================================================
-- v6.0.53_terdemol
-- ===========================================================================
CALL mg_add_col('sf_tmpenc', 'open',  '`open` int AFTER descri');
CALL mg_add_col('sf_tmpenc', 'close', '`close` int AFTER `open`');


-- ===========================================================================
-- v6.0.55_terdemol
-- ===========================================================================
CALL mg_mod_col('xpr_produccion', 'fechafin', '`fechafin` timestamp NULL DEFAULT NULL');

CREATE TABLE IF NOT EXISTS xpr_turno (
    idturno    bigint       NOT NULL,
    nombre     varchar(255) NOT NULL,
    codigo     varchar(50)  NOT NULL,
    version    bigint       NOT NULL,
    idcompania bigint       NOT NULL,
    PRIMARY KEY (idturno)
);

CALL mg_func_add('DASHBOARD_VIEW',            NULL, 7,  1, 'Dashboard.accessRight.panelAccess',       1);
CALL mg_func_add('PRODUCTION_INPUTS_REPORT',  NULL, 11, 1, 'menu.xproduction.productionInputsReport', 1);

CALL mg_add_col('xpr_produccion', 'idturno',     '`idturno` bigint AFTER idlinea');
CALL mg_add_fk('xpr_produccion',  'fk_xpr_produccion_turno', 'FOREIGN KEY (idturno) REFERENCES xpr_turno (idturno)');
CALL mg_add_col('xpr_produccion', 'tipoturno',   '`tipoturno` varchar(50) AFTER fechafin');
CALL mg_add_col('xpr_produccion', 'observacion', '`observacion` longtext AFTER id_tmpenc');
CALL mg_mod_col('sf_tmpenc',      'glosa',       '`glosa` varchar(1500)');
CALL mg_mod_col('sf_tmpenc',      'descri',      '`descri` varchar(1500)');


-- ===========================================================================
-- v6.0.56_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS xpr_grupo (
    idgrupo    bigint       NOT NULL,
    nombre     varchar(255) NOT NULL,
    codigo     varchar(50)  NOT NULL,
    version    bigint       NOT NULL,
    idcompania bigint       NOT NULL,
    PRIMARY KEY (idgrupo)
);

CALL mg_add_col('xpr_produccion', 'idgrupo', '`idgrupo` bigint AFTER idlinea');
CALL mg_add_fk('xpr_produccion',  'fk_xpr_produccion_grupo', 'FOREIGN KEY (idgrupo) REFERENCES xpr_grupo (idgrupo)');

-- xpr_turno se revierte aqui mismo: el modelo final usa xpr_grupo.
CALL mg_drop_col('xpr_produccion', 'idturno');
DROP TABLE IF EXISTS xpr_turno;


-- ===========================================================================
-- v6.0.58_terdemol
-- ===========================================================================
CALL mg_func_add('WAREHOUSEVOUCHER_INVENTORYEXPENSE_REPORT', NULL, 5, 1, 'menu.warehouse.report.inventoryExpenseSummary',        1);
CALL mg_func_add('ANALYTICALINVENTORYEXPENSE_REPORT',        NULL, 5, 1, 'menu.warehouse.report.analyticalInventoryExpenseReport', 1);


-- ===========================================================================
-- v6.0.67_terdemol
-- ===========================================================================
CALL mg_func_add('EXTENDED_INVENTORY_REPORT', NULL, 5, 1, 'menu.warehouse.report.extendedInventoryReport', 1);


-- ===========================================================================
-- v6.0.68_terdemol
-- ===========================================================================
CALL mg_add_col('arcgms', 'ind_regulariz', '`ind_regulariz` varchar(1) NOT NULL DEFAULT ''N''');
CALL mg_func_add('MAJOR_ACCOUNTING_GROUPED', NULL, 5, 1, 'Functionality.accounting.majorAccountingGrouped', 1);


-- ===========================================================================
-- v6.0.70_terdemol
-- ===========================================================================
CALL mg_add_col('com_encoc', 'fecha_anul',   '`fecha_anul` datetime NULL');
CALL mg_add_col('com_encoc', 'usuario_anul', '`usuario_anul` varchar(4) NULL');
CALL mg_add_col('com_encoc', 'motivo_anul',  '`motivo_anul` varchar(250) NULL');
CALL mg_add_col('inv_vales', 'fecha_anul',   '`fecha_anul` datetime NULL');
CALL mg_add_col('inv_vales', 'usuario_anul', '`usuario_anul` varchar(4) NULL');
CALL mg_add_col('inv_vales', 'motivo_anul',  '`motivo_anul` varchar(250) NULL');

CALL mg_func_add('WAREHOUSEPURCHASEORDERREVERSE', NULL, 5, 1, 'menu.warehouse.purchaseOrder.reverse', 1);
CALL mg_func_add('WAREHOUSEVOUCHERREVERSE',       NULL, 5, 1, 'menu.warehouse.voucher.reverse',       1);


-- ===========================================================================
-- v6.0.71_terdemol
-- ===========================================================================
CALL mg_drop_col('inv_articulos', 'cu');
CALL mg_drop_col('inv_articulos', 'ct');
CALL mg_func_del('PRODUCTITEMCOSTUNITREPORT');


-- ===========================================================================
-- v6.0.72_terdemol
-- ===========================================================================
CALL mg_func_add('PURCHASEDOCUMENTMODAL_REGISTER', NULL, 5, 1, 'menu.warehouse.purchaseDocument.modal.register', 1);
CALL mg_func_add('PURCHASEDOCUMENTMODAL_APPROVE',  NULL, 5, 1, 'menu.warehouse.purchaseDocument.modal.approve',  1);
CALL mg_func_add('PURCHASEDOCUMENTMODAL_NULLIFY',  NULL, 5, 1, 'menu.warehouse.purchaseDocument.modal.nullify',  1);


-- ===========================================================================
-- v6.0.73_terdemol
-- ===========================================================================
CALL mg_add_key('inv_movdet',    'idx_movdet_alm',       'INDEX idx_movdet_alm (cod_alm)');
CALL mg_add_key('inv_movdet',    'idx_movdet_notrans',   'INDEX idx_movdet_notrans (no_cia, no_trans)');
CALL mg_add_key('inv_movdet',    'idx_movdet_codart',    'INDEX idx_movdet_codart (no_cia, cod_art)');
CALL mg_add_key('inv_mov',       'idx_mov_notrans',      'INDEX idx_mov_notrans (no_cia, no_trans)');
CALL mg_add_key('inv_vales',     'idx_vales_alm_fecha',  'INDEX idx_vales_alm_fecha (cod_alm, fecha)');
CALL mg_add_key('inv_vales',     'idx_vales_estado',     'INDEX idx_vales_estado (estado)');
CALL mg_add_key('inv_articulos', 'idx_articulos_alm',    'INDEX idx_articulos_alm (cod_alm)');
CALL mg_add_key('inv_articulos', 'idx_articulos_grusub', 'INDEX idx_articulos_grusub (no_cia, cod_gru, cod_sub)');
CALL mg_add_key('inv_inicio',    'idx_inicio_alm_gest',  'INDEX idx_inicio_alm_gest (alm, gestion)');
CALL mg_add_key('inv_periodo',   'idx_periodo_alm_gest', 'INDEX idx_periodo_alm_gest (cod_alm, gestion, mes)');


-- ===========================================================================
-- v6.0.75_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inv_ajuste_saldo (
    id_ajuste          BIGINT       NOT NULL AUTO_INCREMENT,
    no_cia             VARCHAR(2)   NOT NULL,
    cod_alm            VARCHAR(6)   NOT NULL,
    cod_art            VARCHAR(6)   NOT NULL,
    fecha_ajuste       DATETIME     NOT NULL,
    fecha_inicio_calc  DATE         NOT NULL,
    fecha_fin_calc     DATE         NOT NULL,
    saldo_uni_anterior DECIMAL(16,2),
    saldo_uni_nuevo    DECIMAL(16,2),
    costo_uni_anterior DECIMAL(16,6),
    costo_uni_nuevo    DECIMAL(16,6),
    saldo_mon_anterior DECIMAL(20,6),
    saldo_mon_nuevo    DECIMAL(20,6),
    motivo             VARCHAR(500) NOT NULL,
    no_usr             VARCHAR(4),
    PRIMARY KEY (id_ajuste),
    KEY ix_ajuste_art (no_cia, cod_alm, cod_art, fecha_ajuste)
) ENGINE=InnoDB;

CALL mg_func_add('INVENTORYRECONCILIATION',
                 'Actualizar Inventario - reconciliar saldos y costo promedio por articulo',
                 5, 5, 'menu.warehouse.configuration.inventoryReconciliation', 1);


-- ===========================================================================
-- v6.0.76_terdemol
-- ===========================================================================
CALL mg_add_col('xpr_linea', 'report_template_code', '`report_template_code` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea', 'cod_art_mp_principal', '`cod_art_mp_principal` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea', 'cod_art_pt_a',         '`cod_art_pt_a` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea', 'cod_art_pt_b',         '`cod_art_pt_b` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea', 'cod_art_diluy_bent',   '`cod_art_diluy_bent` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea', 'cod_art_diluy_caolin', '`cod_art_diluy_caolin` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea', 'merma_factor',         '`merma_factor` DECIMAL(10,4) NOT NULL DEFAULT 1.0300');

CREATE TABLE IF NOT EXISTS xpr_produccion_ulexita (
    idproduccion_ulexita  BIGINT        NOT NULL AUTO_INCREMENT,
    idproduccion          BIGINT        NOT NULL,
    ley_mp_bentonita      DECIMAL(14,4) NULL,
    ley_pt                DECIMAL(14,4) NULL,
    producto_granulado_tn DECIMAL(14,4) NULL,
    consumo_reproceso_tn  DECIMAL(14,4) NULL,
    reproceso_final_tn    DECIMAL(14,4) NULL,
    diluyente_total_tn    DECIMAL(14,4) NULL,
    bentonita_pct         DECIMAL(8,4)  NULL,
    ulex_disponible_snap  DECIMAL(14,4) NULL,
    consumo_mp_calc_snap  DECIMAL(14,4) NULL,
    observacion_lab       VARCHAR(500)  NULL,
    version               BIGINT        NOT NULL DEFAULT 0,
    idcompania            BIGINT        NOT NULL,
    PRIMARY KEY (idproduccion_ulexita),
    UNIQUE KEY uk_xpr_prod_ulexita_prod (idproduccion),
    CONSTRAINT fk_xpr_prod_ulexita_prod
        FOREIGN KEY (idproduccion) REFERENCES xpr_produccion (idproduccion)
) ENGINE=InnoDB;

-- nombrerecurso: se inserta ya con el valor final que fijaba el UPDATE de v6.0.88.
CALL mg_func_add('PRODUCTION_LAB_DATA',
                 'Edicion de datos de laboratorio en produccion (leyes MP/PT) post-aprobacion',
                 11, 4, 'menu.xproduction.production.labData', 1);
CALL mg_func_add('PRODUCTION_LABOR',
                 'Registro de mano de obra en orden de produccion',
                 11, 13, 'menu.xproduction.production.labor', 1);


-- ===========================================================================
-- v6.0.77_terdemol
-- ===========================================================================
CALL mg_add_col('xpr_produccion_ulexita', 'merma_factor_snap',    '`merma_factor_snap` DECIMAL(10,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'diluyente_total_snap', '`diluyente_total_snap` DECIMAL(14,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'bentonita_pct_snap',   '`bentonita_pct_snap` DECIMAL(8,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'caolin_pct_snap',      '`caolin_pct_snap` DECIMAL(8,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'pt_a_snap',            '`pt_a_snap` DECIMAL(14,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'pt_b_snap',            '`pt_b_snap` DECIMAL(14,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'pt_total_bueno_snap',  '`pt_total_bueno_snap` DECIMAL(14,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'kpa_snap',             '`kpa_snap` DECIMAL(14,6) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'kpm_bentonita_snap',   '`kpm_bentonita_snap` DECIMAL(14,6) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'kpm_merma_snap',       '`kpm_merma_snap` DECIMAL(14,6) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'ley_mp_recalc_snap',   '`ley_mp_recalc_snap` DECIMAL(14,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'merma_snap',           '`merma_snap` DECIMAL(14,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'merma_pct_snap',       '`merma_pct_snap` DECIMAL(8,4) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'snap_at',              '`snap_at` DATETIME NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'snap_by',              '`snap_by` VARCHAR(4) NULL');


-- ===========================================================================
-- v6.0.78_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inv_lugardespacho (
    idlugardespacho BIGINT       NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)  NOT NULL,
    descripcion     VARCHAR(150) NOT NULL,
    direccion       VARCHAR(250),
    tipo            VARCHAR(20)  NOT NULL,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    version         BIGINT       DEFAULT 0,
    idcompania      BIGINT       NOT NULL,
    PRIMARY KEY (idlugardespacho),
    UNIQUE KEY uq_lugardespacho_cod (idcompania, codigo),
    KEY ix_lugardespacho_tipo (idcompania, tipo, activo),
    CONSTRAINT fk_lugardespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_valedespacho (
    idvaledespacho       BIGINT        NOT NULL AUTO_INCREMENT,
    estado               VARCHAR(15)   NOT NULL,
    no_orden_entrega     BIGINT        NULL,
    fecha_despacho       DATETIME      NOT NULL,
    idcontratopuestovend BIGINT        NULL,
    codigo_lote_venta    VARCHAR(80)   NOT NULL,
    cantidad_bolsas      INT           NOT NULL,
    numero_factura       VARCHAR(50)   NULL,
    cod_prov             VARCHAR(6)    CHARACTER SET utf8 COLLATE utf8_bin NULL,
    idcliente            BIGINT        NULL,
    idturno              BIGINT        NULL,
    idlugar_origen       BIGINT        NULL,
    idlugar_destino      BIGINT        NULL,
    conductor_nombre     VARCHAR(120)  NOT NULL,
    conductor_licencia   VARCHAR(30)   NOT NULL,
    conductor_celular    VARCHAR(30)   NULL,
    vehiculo_placa       VARCHAR(20)   NOT NULL,
    vehiculo_marca       VARCHAR(50)   NULL,
    vehiculo_color       VARCHAR(30)   NULL,
    hora_inicio          DATETIME      NOT NULL,
    hora_fin             DATETIME      NOT NULL,
    no_camion            INT           NOT NULL,
    no_boleta_balanza    VARCHAR(30)   NOT NULL,
    peso_tara_kg         DECIMAL(12,3) NOT NULL,
    peso_bruto_kg        DECIMAL(12,3) NOT NULL,
    peso_neto_kg         DECIMAL(12,3) NOT NULL,
    bolsa_desde          INT           NOT NULL,
    bolsa_hasta          INT           NOT NULL,
    no_cia               VARCHAR(2)    CHARACTER SET utf8 COLLATE utf8_bin NULL,
    cod_alm              VARCHAR(6)    CHARACTER SET utf8 COLLATE utf8_bin NULL,
    cod_cc               VARCHAR(8)    CHARACTER SET utf8 COLLATE utf8_bin NULL,
    idresponsable        BIGINT        NULL,
    idunidadnegocio      BIGINT        NULL,
    no_trans_vale        VARCHAR(10)   CHARACTER SET utf8 COLLATE utf8_bin NULL,
    no_cia_vale          VARCHAR(2)    CHARACTER SET utf8 COLLATE utf8_bin NULL,
    observacion          VARCHAR(500)  NULL,
    fecha_anul           DATETIME      NULL,
    usuario_anul         VARCHAR(4)    NULL,
    motivo_anul          VARCHAR(250)  NULL,
    createdby            VARCHAR(4)    NULL,
    createddate          DATETIME      NULL,
    updatedby            VARCHAR(4)    NULL,
    updateddate          DATETIME      NULL,
    version              BIGINT        DEFAULT 0,
    idcompania           BIGINT        NOT NULL,
    PRIMARY KEY (idvaledespacho),
    KEY ix_valedespacho_estado (idcompania, estado),
    KEY ix_valedespacho_fecha  (idcompania, fecha_despacho),
    KEY ix_valedespacho_orden  (idcompania, no_orden_entrega),
    KEY ix_valedespacho_alm    (no_cia, cod_alm),
    KEY ix_valedespacho_vale   (no_cia_vale, no_trans_vale),
    KEY ix_valedespacho_prov   (no_cia, cod_prov),
    CONSTRAINT fk_valedespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania),
    CONSTRAINT fk_valedespacho_almacen
        FOREIGN KEY (no_cia, cod_alm) REFERENCES inv_almacenes (no_cia, cod_alm),
    CONSTRAINT fk_valedespacho_lugarorigen
        FOREIGN KEY (idlugar_origen) REFERENCES inv_lugardespacho (idlugardespacho),
    CONSTRAINT fk_valedespacho_lugardestino
        FOREIGN KEY (idlugar_destino) REFERENCES inv_lugardespacho (idlugardespacho)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_valedespacho_det (
    iddetalledespacho BIGINT        NOT NULL AUTO_INCREMENT,
    idvaledespacho    BIGINT        NOT NULL,
    no_cia_art        VARCHAR(2)    CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_art           VARCHAR(6)    CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_med           VARCHAR(6)    CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cantidad          DECIMAL(14,6) NOT NULL,
    costo_unitario    DECIMAL(14,6) NOT NULL DEFAULT 0,
    monto             DECIMAL(14,6) NOT NULL DEFAULT 0,
    cantidad_bolsas   INT           NULL,
    observacion       VARCHAR(250)  NULL,
    version           BIGINT        DEFAULT 0,
    idcompania        BIGINT        NOT NULL,
    PRIMARY KEY (iddetalledespacho),
    KEY ix_detdespacho_vale (idvaledespacho),
    KEY ix_detdespacho_art (no_cia_art, cod_art),
    CONSTRAINT fk_detdespacho_vale
        FOREIGN KEY (idvaledespacho) REFERENCES inv_valedespacho (idvaledespacho)
        ON DELETE CASCADE,
    CONSTRAINT fk_detdespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('WAREHOUSEDISPATCH',         'Despacho de Productos Terminados',     5, 15, 'menu.warehouse.dispatch',         1);
CALL mg_func_add('WAREHOUSEDISPATCHAPPROVAL', 'Aprobacion de Despacho',               5,  1, 'menu.warehouse.dispatch.approve', 1);
CALL mg_func_add('WAREHOUSEDISPATCHREVERSE',  'Anulacion / Reversion de Despacho',    5,  1, 'menu.warehouse.dispatch.reverse', 1);
CALL mg_func_add('WAREHOUSEDISPATCHPLACE',    'Catalogo Lugares de Despacho/Entrega', 5, 15, 'menu.warehouse.dispatch.place',   1);


-- ===========================================================================
-- v6.0.80_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inv_conductor (
    idconductor BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(120) NOT NULL,
    licencia    VARCHAR(30)  NOT NULL,
    celular     VARCHAR(30)  NULL,
    estado      VARCHAR(3)   NOT NULL DEFAULT 'VIG',
    createdby   VARCHAR(4)   NULL,
    createddate DATETIME     NULL,
    updatedby   VARCHAR(4)   NULL,
    updateddate DATETIME     NULL,
    version     BIGINT       DEFAULT 0,
    idcompania  BIGINT       NOT NULL,
    PRIMARY KEY (idconductor),
    UNIQUE KEY uq_conductor_licencia (idcompania, licencia),
    KEY ix_conductor_nombre (idcompania, nombre),
    KEY ix_conductor_estado (idcompania, estado),
    CONSTRAINT fk_conductor_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_vehiculo (
    idvehiculo  BIGINT      NOT NULL AUTO_INCREMENT,
    placa       VARCHAR(20) NOT NULL,
    marca       VARCHAR(50) NULL,
    color       VARCHAR(30) NULL,
    estado      VARCHAR(3)  NOT NULL DEFAULT 'VIG',
    createdby   VARCHAR(4)  NULL,
    createddate DATETIME    NULL,
    updatedby   VARCHAR(4)  NULL,
    updateddate DATETIME    NULL,
    version     BIGINT      DEFAULT 0,
    idcompania  BIGINT      NOT NULL,
    PRIMARY KEY (idvehiculo),
    UNIQUE KEY uq_vehiculo_placa (idcompania, placa),
    KEY ix_vehiculo_estado (idcompania, estado),
    CONSTRAINT fk_vehiculo_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_conductor_vehiculo (
    idconductor BIGINT NOT NULL,
    idvehiculo  BIGINT NOT NULL,
    PRIMARY KEY (idconductor, idvehiculo),
    KEY ix_cond_veh_veh (idvehiculo),
    CONSTRAINT fk_condveh_conductor
        FOREIGN KEY (idconductor) REFERENCES inv_conductor (idconductor) ON DELETE CASCADE,
    CONSTRAINT fk_condveh_vehiculo
        FOREIGN KEY (idvehiculo) REFERENCES inv_vehiculo (idvehiculo) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_drop_col('inv_valedespacho', 'conductor_nombre');
CALL mg_drop_col('inv_valedespacho', 'conductor_licencia');
CALL mg_drop_col('inv_valedespacho', 'conductor_celular');
CALL mg_drop_col('inv_valedespacho', 'vehiculo_placa');
CALL mg_drop_col('inv_valedespacho', 'vehiculo_marca');
CALL mg_drop_col('inv_valedespacho', 'vehiculo_color');
CALL mg_add_col('inv_valedespacho',  'idconductor', '`idconductor` BIGINT NULL AFTER idlugar_destino');
CALL mg_add_col('inv_valedespacho',  'idvehiculo',  '`idvehiculo` BIGINT NULL AFTER idconductor');
CALL mg_add_key('inv_valedespacho',  'ix_valedespacho_conductor', 'KEY ix_valedespacho_conductor (idconductor)');
CALL mg_add_key('inv_valedespacho',  'ix_valedespacho_vehiculo',  'KEY ix_valedespacho_vehiculo (idvehiculo)');
CALL mg_add_fk('inv_valedespacho',   'fk_valedespacho_conductor', 'FOREIGN KEY (idconductor) REFERENCES inv_conductor (idconductor)');
CALL mg_add_fk('inv_valedespacho',   'fk_valedespacho_vehiculo',  'FOREIGN KEY (idvehiculo) REFERENCES inv_vehiculo (idvehiculo)');

CALL mg_func_add('WAREHOUSEDRIVER',  'Catalogo de Conductores',             5, 15, 'menu.warehouse.dispatch.driver',  1);
CALL mg_func_add('WAREHOUSEVEHICLE', 'Catalogo de Vehiculos de Transporte', 5, 15, 'menu.warehouse.dispatch.vehicle', 1);


-- ===========================================================================
-- v6.0.81_terdemol
-- ===========================================================================
CALL mg_add_col('personacliente', 'codigo_transbordo', '`codigo_transbordo` VARCHAR(30) NULL AFTER codigocliente');
CALL mg_add_key('personacliente', 'uq_personacliente_transbordo', 'UNIQUE KEY uq_personacliente_transbordo (codigo_transbordo)');
CALL mg_mod_col('inv_valedespacho', 'observacion', '`observacion` VARCHAR(1000) NULL');


-- ===========================================================================
-- v6.0.82_terdemol
-- ===========================================================================
CALL mg_drop_key('personacliente', 'uq_personacliente_transbordo');
CALL mg_drop_col('personacliente', 'codigo_transbordo');
CALL mg_drop_col('inv_valedespacho', 'codigo_transbordo');

CREATE TABLE IF NOT EXISTS inv_tipo_envase (
    idtipoenvase       BIGINT        NOT NULL AUTO_INCREMENT,
    nombre             VARCHAR(80)   NOT NULL,
    etiqueta_capacidad VARCHAR(60)   NOT NULL,
    capacidad_kg       DECIMAL(12,3) NULL,
    estado             VARCHAR(3)    NOT NULL DEFAULT 'VIG',
    createdby          VARCHAR(4)    NULL,
    createddate        DATETIME      NULL,
    updatedby          VARCHAR(4)    NULL,
    updateddate        DATETIME      NULL,
    version            BIGINT        DEFAULT 0,
    idcompania         BIGINT        NOT NULL,
    PRIMARY KEY (idtipoenvase),
    UNIQUE KEY uq_tipoenvase_nombre (idcompania, nombre, etiqueta_capacidad),
    KEY ix_tipoenvase_estado (idcompania, estado),
    CONSTRAINT fk_tipoenvase_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_add_col('inv_valedespacho_det', 'idtipoenvase', '`idtipoenvase` BIGINT NULL AFTER cantidad_bolsas');
CALL mg_add_key('inv_valedespacho_det', 'ix_valedespacho_det_tipoenvase', 'KEY ix_valedespacho_det_tipoenvase (idtipoenvase)');
CALL mg_add_fk('inv_valedespacho_det',  'fk_valedespacho_det_tipoenvase', 'FOREIGN KEY (idtipoenvase) REFERENCES inv_tipo_envase (idtipoenvase)');

CALL mg_func_add('WAREHOUSEPACKAGING', 'Catalogo de Tipos de Bolsa/Envase', 5, 15, 'menu.warehouse.dispatch.packaging', 1);

CALL mg_add_col('inv_tipo_envase', 'peso_bruto_promedio_kg', '`peso_bruto_promedio_kg` DECIMAL(12,3) NULL AFTER capacidad_kg');

CALL mg_add_col('personacliente', 'codprefijo', '`codprefijo` VARCHAR(10) NULL AFTER codigocliente');
CALL mg_mod_col('personacliente', 'codigocliente', '`codigocliente` VARCHAR(100) NULL');

CALL mg_func_add('CLIENTTYPE',        'Cliente: campo Tipo Cliente',             1, 1, 'Functionality.customers.client.field.type',        1);
CALL mg_func_add('CLIENTTERRITORY',   'Cliente: campo Territorio',               1, 1, 'Functionality.customers.client.field.territory',   1);
CALL mg_func_add('CLIENTCATEGORY',    'Cliente: campo Categoria Cliente',        1, 1, 'Functionality.customers.client.field.category',    1);
CALL mg_func_add('CLIENTDISCOUNT',    'Cliente: campos Porcentaje de Descuento', 1, 1, 'Functionality.customers.client.field.discount',    1);
CALL mg_func_add('CLIENTPAYMENT',     'Cliente: campo Metodo de Pago',           1, 1, 'Functionality.customers.client.field.payment',     1);
CALL mg_func_add('CLIENTCASHACCOUNT', 'Cliente: campo Cuenta Diferida',          1, 1, 'Functionality.customers.client.field.cashAccount', 1);

CALL mg_drop_col('inv_valedespacho_det', 'observacion');

CALL mg_drop_col('inv_valedespacho', 'bolsa_desde');
CALL mg_drop_col('inv_valedespacho', 'bolsa_hasta');
CALL mg_drop_col('inv_valedespacho', 'cantidad_bolsas');

CALL mg_add_col('inv_valedespacho_det', 'bolsa_desde', '`bolsa_desde` INT NULL AFTER cantidad_bolsas');
CALL mg_add_col('inv_valedespacho_det', 'bolsa_hasta', '`bolsa_hasta` INT NULL AFTER bolsa_desde');

CALL mg_add_col('inv_tipo_envase', 'detalle_envase_default', '`detalle_envase_default` VARCHAR(255) NULL AFTER peso_bruto_promedio_kg');

CREATE TABLE IF NOT EXISTS inv_valedespacho_envase (
    idenvase              BIGINT        NOT NULL AUTO_INCREMENT,
    idvaledespacho        BIGINT        NOT NULL,
    iddetalledespacho     BIGINT        NOT NULL,
    numero_correlativo    INT           NOT NULL,
    codigo_identificacion VARCHAR(120)  NOT NULL,
    detalle_envase        VARCHAR(255)  NULL,
    peso_neto_aprox_kg    DECIMAL(12,3) NULL,
    createdby             VARCHAR(4)    NULL,
    createddate           DATETIME      NULL,
    updatedby             VARCHAR(4)    NULL,
    updateddate           DATETIME      NULL,
    version               BIGINT        DEFAULT 0,
    idcompania            BIGINT        NOT NULL,
    PRIMARY KEY (idenvase),
    KEY ix_envase_dispatch (idvaledespacho),
    KEY ix_envase_detail (iddetalledespacho),
    CONSTRAINT fk_envase_dispatch
        FOREIGN KEY (idvaledespacho) REFERENCES inv_valedespacho (idvaledespacho),
    CONSTRAINT fk_envase_detail
        FOREIGN KEY (iddetalledespacho) REFERENCES inv_valedespacho_det (iddetalledespacho),
    CONSTRAINT fk_envase_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('WAREHOUSEDISPATCHFINALIZE',   'Finalizar Despacho',    5, 1, 'menu.warehouse.dispatch.finalize',   1);
CALL mg_func_add('WAREHOUSEDISPATCHUNFINALIZE', 'Desfinalizar Despacho', 5, 1, 'menu.warehouse.dispatch.unfinalize', 1);

CREATE TABLE IF NOT EXISTS inv_descripcion_producto (
    iddescripcion_producto BIGINT      NOT NULL AUTO_INCREMENT,
    no_cia_art             VARCHAR(2)  NOT NULL,
    cod_art                VARCHAR(6)  NOT NULL,
    descripcion            LONGTEXT    NOT NULL,
    estado                 VARCHAR(15) NOT NULL DEFAULT 'BORRADOR',
    createdby              VARCHAR(4)  NULL,
    createddate            DATETIME    NULL,
    updatedby              VARCHAR(4)  NULL,
    updateddate            DATETIME    NULL,
    version                BIGINT      DEFAULT 0,
    idcompania             BIGINT      NOT NULL,
    PRIMARY KEY (iddescripcion_producto),
    KEY ix_descprod_art (no_cia_art, cod_art),
    KEY ix_descprod_estado (estado),
    CONSTRAINT fk_descprod_articulo
        FOREIGN KEY (no_cia_art, cod_art) REFERENCES inv_articulos (no_cia, cod_art),
    CONSTRAINT fk_descprod_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_ruta_despacho (
    idruta                   BIGINT       NOT NULL AUTO_INCREMENT,
    nombre                   VARCHAR(120) NOT NULL,
    origen_texto             VARCHAR(200) NULL,
    destino_texto            VARCHAR(200) NULL,
    paradas                  LONGTEXT     NOT NULL,
    imagen_mapa              LONGBLOB     NULL,
    imagen_mapa_content_type VARCHAR(50)  NULL,
    distancia_km             DECIMAL(8,2) NULL,
    duracion_horas           DECIMAL(6,2) NULL,
    estado                   VARCHAR(15)  NOT NULL DEFAULT 'BORRADOR',
    createdby                VARCHAR(4)   NULL,
    createddate              DATETIME     NULL,
    updatedby                VARCHAR(4)   NULL,
    updateddate              DATETIME     NULL,
    version                  BIGINT       DEFAULT 0,
    idcompania               BIGINT       NOT NULL,
    PRIMARY KEY (idruta),
    UNIQUE KEY uq_ruta_nombre (idcompania, nombre),
    KEY ix_ruta_estado (estado),
    CONSTRAINT fk_ruta_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_add_col('inv_valedespacho_det', 'iddescripcion_producto', '`iddescripcion_producto` BIGINT NULL AFTER cod_med');
CALL mg_add_fk('inv_valedespacho_det',  'fk_valedespdet_descripcion',
               'FOREIGN KEY (iddescripcion_producto) REFERENCES inv_descripcion_producto (iddescripcion_producto)');

CALL mg_add_col('inv_valedespacho', 'idruta',        '`idruta` BIGINT NULL');
CALL mg_add_col('inv_valedespacho', 'vigencia_dias', '`vigencia_dias` INT NULL');
CALL mg_add_fk('inv_valedespacho',  'fk_valedespacho_ruta', 'FOREIGN KEY (idruta) REFERENCES inv_ruta_despacho (idruta)');

CALL mg_func_add('PRODUCTDESCRIPTION',          'Catalogo de Descripciones Tecnicas de Producto', 5, 15, 'menu.warehouse.dispatch.productDescription', 1);
CALL mg_func_add('WAREHOUSEDISPATCHROUTE',      'Catalogo de Rutas de Despacho',                  5, 15, 'menu.warehouse.dispatch.route',              1);
CALL mg_func_add('WAREHOUSEDISPATCHROUTESHEET', 'Imprimir Hoja de Ruta',                          5,  1, 'menu.warehouse.dispatch.routeSheet',         1);

CALL mg_add_col('inv_valedespacho', 'vigencia_max_dias', '`vigencia_max_dias` INT NULL AFTER vigencia_dias');

CALL mg_drop_col('inv_valedespacho', 'cod_cc');

CALL mg_func_add('WAREHOUSEDISPATCHUNAPPROVE', 'Desaprobar Despacho', 5, 1, 'menu.warehouse.dispatch.unapprove', 1);


-- ===========================================================================
-- v6.0.86_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS xpr_produccion_baritina (
    idproduccion_baritina BIGINT        NOT NULL AUTO_INCREMENT,
    idproduccion          BIGINT        NOT NULL,
    uso_mp_tn             DECIMAL(14,4) NULL,
    pt_tn                 DECIMAL(14,4) NULL,
    turnos                INT           NULL,
    observacion           VARCHAR(500)  NULL,
    version               BIGINT        NOT NULL DEFAULT 0,
    idcompania            BIGINT        NOT NULL,
    PRIMARY KEY (idproduccion_baritina),
    UNIQUE KEY uk_xpr_prod_baritina_prod (idproduccion),
    CONSTRAINT fk_xpr_prod_baritina_prod
        FOREIGN KEY (idproduccion) REFERENCES xpr_produccion (idproduccion)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS xpr_produccion_baritina_zona (
    idproduccion_baritina_zona BIGINT        NOT NULL AUTO_INCREMENT,
    idproduccion               BIGINT        NOT NULL,
    idzonaproductiva           BIGINT        NOT NULL,
    porcentaje                 DECIMAL(8,4)  NULL,
    cantidad_tn                DECIMAL(14,4) NULL,
    version                    BIGINT        NOT NULL DEFAULT 0,
    idcompania                 BIGINT        NOT NULL,
    PRIMARY KEY (idproduccion_baritina_zona),
    KEY ix_xpr_baritina_zona_prod (idproduccion),
    KEY ix_xpr_baritina_zona_zona (idzonaproductiva),
    CONSTRAINT fk_xpr_baritina_zona_prod
        FOREIGN KEY (idproduccion) REFERENCES xpr_produccion (idproduccion),
    CONSTRAINT fk_xpr_baritina_zona_zona
        FOREIGN KEY (idzonaproductiva) REFERENCES zonaproductiva (idzonaproductiva)
) ENGINE=InnoDB;

CALL mg_drop_col('xpr_produccion_baritina', 'despacho_tn');

CALL mg_func_add('PRODUCTION_DAILY_REPORT', 'Reporte de Produccion Diaria (ULEXITA/BARITINA)',
                 11, 1, 'menu.xproduction.dailyProductionReport', 1);


-- ===========================================================================
-- v6.0.87_terdemol
-- ===========================================================================
CALL mg_drop_col('xpr_produccion_ulexita', 'ulex_disponible_snap');


-- ===========================================================================
-- v6.0.88_terdemol
-- ===========================================================================
CALL mg_add_col('xpr_linea',              'cod_art_pt_principal', '`cod_art_pt_principal` VARCHAR(20) NULL');
CALL mg_add_col('xpr_linea',              'factor_pt_mp',         '`factor_pt_mp` DECIMAL(10,4) NULL');
CALL mg_add_col('xpr_linea',              'cod_art_reproc_final', '`cod_art_reproc_final` VARCHAR(20) NULL');
CALL mg_add_col('xpr_produccion_ulexita', 'cod_art_reproc_final', '`cod_art_reproc_final` VARCHAR(20) NULL');

CALL mg_func_add('PRODUCTION_DISAPPROVE', 'Desaprobar orden de produccion',
                 11, 4, 'menu.xproduction.production.disapprove', 1);
CALL mg_func_add('XPRODUCTION_BALANCE',   'Saldos de almacen (Materia Prima / Producto Terminado)',
                 11, 1, 'Functionality.xproduction.balance', 1);


-- ===========================================================================
-- v6.0.89_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inv_vale_restriccion (
    idvalerestriccion BIGINT     NOT NULL AUTO_INCREMENT,
    idusuario         BIGINT     NOT NULL,
    activo            TINYINT(1) NOT NULL DEFAULT 1,
    version           BIGINT     DEFAULT 0,
    idcompania        BIGINT     NOT NULL,
    PRIMARY KEY (idvalerestriccion),
    UNIQUE KEY uq_valerestriccion_usuario (idcompania, idusuario),
    CONSTRAINT fk_valerestriccion_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania),
    CONSTRAINT fk_valerestriccion_usuario  FOREIGN KEY (idusuario)  REFERENCES usuario (idusuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_vale_restriccion_tipodoc (
    idrestricciontipodoc BIGINT     NOT NULL AUTO_INCREMENT,
    idvalerestriccion    BIGINT     NOT NULL,
    no_cia               VARCHAR(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_doc              VARCHAR(3) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    idcompania           BIGINT     NOT NULL,
    PRIMARY KEY (idrestricciontipodoc),
    UNIQUE KEY uq_restriccion_tipodoc (idvalerestriccion, no_cia, cod_doc),
    KEY ix_restriccion_tipodoc (no_cia, cod_doc),
    CONSTRAINT fk_restriccion_tipodoc_cab
        FOREIGN KEY (idvalerestriccion) REFERENCES inv_vale_restriccion (idvalerestriccion) ON DELETE CASCADE,
    CONSTRAINT fk_restriccion_tipodoc_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_vale_restriccion_almacen (
    idrestriccionalmacen BIGINT     NOT NULL AUTO_INCREMENT,
    idvalerestriccion    BIGINT     NOT NULL,
    no_cia               VARCHAR(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_alm              VARCHAR(6) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    idcompania           BIGINT     NOT NULL,
    PRIMARY KEY (idrestriccionalmacen),
    UNIQUE KEY uq_restriccion_almacen (idvalerestriccion, no_cia, cod_alm),
    KEY ix_restriccion_almacen (no_cia, cod_alm),
    CONSTRAINT fk_restriccion_almacen_cab
        FOREIGN KEY (idvalerestriccion) REFERENCES inv_vale_restriccion (idvalerestriccion) ON DELETE CASCADE,
    CONSTRAINT fk_restriccion_almacen_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_vale_restriccion_articulo (
    idrestriccionarticulo BIGINT     NOT NULL AUTO_INCREMENT,
    idvalerestriccion     BIGINT     NOT NULL,
    no_cia                VARCHAR(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_art               VARCHAR(6) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    idcompania            BIGINT     NOT NULL,
    PRIMARY KEY (idrestriccionarticulo),
    UNIQUE KEY uq_restriccion_articulo (idvalerestriccion, no_cia, cod_art),
    KEY ix_restriccion_articulo (no_cia, cod_art),
    CONSTRAINT fk_restriccion_articulo_cab
        FOREIGN KEY (idvalerestriccion) REFERENCES inv_vale_restriccion (idvalerestriccion) ON DELETE CASCADE,
    CONSTRAINT fk_restriccion_articulo_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('WAREHOUSEVOUCHERRESTRICTION', 'Restriccion de Vales por Usuario', 5, 15, 'menu.warehouse.voucher.restriction', 1);


-- ===========================================================================
-- v6.0.93_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inv_observacion_despacho (
    idobservacion BIGINT        NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120)  NOT NULL,
    observacion   VARCHAR(1000) NOT NULL,
    estado        VARCHAR(15)   NOT NULL DEFAULT 'BORRADOR',
    createdby     VARCHAR(4)    NULL,
    createddate   DATETIME      NULL,
    updatedby     VARCHAR(4)    NULL,
    updateddate   DATETIME      NULL,
    version       BIGINT        DEFAULT 0,
    idcompania    BIGINT        NOT NULL,
    PRIMARY KEY (idobservacion),
    UNIQUE KEY uq_obsdespacho_nombre (idcompania, nombre),
    KEY ix_obsdespacho_estado (estado),
    CONSTRAINT fk_obsdespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('WAREHOUSEDISPATCHOBSERVATION', 'Catalogo de Observaciones de Despacho', 5, 15, 'menu.warehouse.dispatch.observation', 1);

CALL mg_add_col('inv_descripcion_producto', 'nombre', '`nombre` VARCHAR(120) NULL AFTER cod_art');
CALL mg_add_col('configuracion', 'desp_controla_inventario', '`desp_controla_inventario` INT NOT NULL DEFAULT 1');


-- ===========================================================================
-- v6.0.94_terdemol
-- ===========================================================================
CALL mg_add_col('xpr_formula', 'sigla', '`sigla` VARCHAR(10) NULL AFTER nombre');


-- ===========================================================================
-- v6.0.96_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS contactocliente (
    idcontactocliente BIGINT        NOT NULL,
    idpersonacliente  BIGINT        NOT NULL,
    idpersona         BIGINT        NULL,
    nombres           VARCHAR(200)  NOT NULL,
    apellidos         VARCHAR(200)  NULL,
    cargo             VARCHAR(150)  NULL,
    departamento      VARCHAR(150)  NULL,
    email             VARCHAR(150)  NULL,
    telefono          VARCHAR(30)   NULL,
    celular           VARCHAR(30)   NULL,
    telefonotrabajo   VARCHAR(30)   NULL,
    fax               VARCHAR(30)   NULL,
    web               VARCHAR(200)  NULL,
    direccion         VARCHAR(300)  NULL,
    observaciones     VARCHAR(1000) NULL,
    principal         INT           NOT NULL DEFAULT 0,
    activo            INT           NOT NULL DEFAULT 1,
    PRIMARY KEY (idcontactocliente),
    KEY ix_contactocli_cliente (idpersonacliente),
    KEY ix_contactocli_persona (idpersona),
    CONSTRAINT fk_contactocli_cliente FOREIGN KEY (idpersonacliente) REFERENCES personacliente (idpersonacliente),
    CONSTRAINT fk_contactocli_persona FOREIGN KEY (idpersona)        REFERENCES persona (idpersona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ===========================================================================
-- v6.0.97_terdemol
-- ===========================================================================
CALL mg_chg_col('contactocliente', 'departamento', 'area', 'VARCHAR(150) NULL');
CALL mg_add_col('contactocliente', 'empresa', '`empresa` VARCHAR(200) NULL AFTER cargo');
CALL mg_add_col('contactocliente', 'ciudad',  '`ciudad` VARCHAR(150) NULL AFTER direccion');
CALL mg_add_col('contactocliente', 'idpais',  '`idpais` BIGINT NULL AFTER ciudad');
CALL mg_add_key('contactocliente', 'ix_contactocli_pais', 'KEY ix_contactocli_pais (idpais)');
CALL mg_add_fk('contactocliente',  'fk_contactocli_pais', 'FOREIGN KEY (idpais) REFERENCES pais (idpais)');


-- ===========================================================================
-- v6.0.98_terdemol
-- ===========================================================================
CALL mg_chg_col('personacliente', 'tipo_persona', 'clase_cliente', 'VARCHAR(45) NULL');


-- ===========================================================================
-- v6.0.99_terdemol
-- ===========================================================================
CALL mg_add_col('personacliente', 'celular',        '`celular` VARCHAR(30) NULL AFTER telefono');
CALL mg_add_col('personacliente', 'empresa',        '`empresa` VARCHAR(200) NULL AFTER email');
CALL mg_add_col('personacliente', 'web',            '`web` VARCHAR(200) NULL AFTER empresa');
CALL mg_add_col('personacliente', 'idpais',         '`idpais` BIGINT NULL AFTER web');
CALL mg_add_col('personacliente', 'ciudad',         '`ciudad` VARCHAR(150) NULL AFTER idpais');
CALL mg_add_col('personacliente', 'iddepartamento', '`iddepartamento` BIGINT NULL');

CALL mg_add_fk('personacliente', 'fk_personacli_pais',         'FOREIGN KEY (idpais) REFERENCES pais (idpais)');
CALL mg_add_fk('personacliente', 'fk_personacli_departamento', 'FOREIGN KEY (iddepartamento) REFERENCES departamento (iddepartamento)');

CALL mg_add_col('contactocliente', 'iddepartamento', '`iddepartamento` BIGINT NULL AFTER idpais');
CALL mg_add_key('contactocliente', 'ix_contactocli_departamento', 'KEY ix_contactocli_departamento (iddepartamento)');
CALL mg_add_fk('contactocliente',  'fk_contactocli_departamento', 'FOREIGN KEY (iddepartamento) REFERENCES departamento (iddepartamento)');
CALL mg_drop_col('contactocliente', 'fax');


-- ===========================================================================
-- v6.0.100_terdemol  (normalizacion a minusculas de personacliente)
-- ===========================================================================
CALL mg_chg_col('personacliente', 'AM',                  'am',                  'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'AP',                  'ap',                  'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'CEM_COD',             'cem_cod',             'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'CODMETODOPAGOSIN',    'codmetodopagosin',    'int NULL');
CALL mg_chg_col('personacliente', 'COMP',                'comp',                'varchar(10) NULL');
CALL mg_chg_col('personacliente', 'DESCUENTO',           'descuento',           'decimal(10,2) NULL');
CALL mg_chg_col('personacliente', 'DESCUENTOPROD',       'descuentoprod',       'decimal(10,2) NULL');
CALL mg_chg_col('personacliente', 'DIRECCION',           'direccion',           'varchar(100) NULL');
CALL mg_chg_col('personacliente', 'ESPERSONA',           'espersona',           'int NULL');
CALL mg_chg_col('personacliente', 'EST_CIVIL',           'est_civil',           'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'FECHA_NAC',           'fecha_nac',           'date NULL');
CALL mg_chg_col('personacliente', 'IDRETENCION',         'idretencion',         'bigint NULL');
CALL mg_chg_col('personacliente', 'IDTERRITORIOTRABAJO', 'idterritoriotrabajo', 'bigint NULL');
CALL mg_chg_col('personacliente', 'IDTIPOCLIENTE',       'idtipocliente',       'bigint NULL');
CALL mg_chg_col('personacliente', 'NIT',                 'nit',                 'varchar(20) NULL');
CALL mg_chg_col('personacliente', 'NOM',                 'nom',                 'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'NRO_DOC',             'nro_doc',             'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'OBSERVACION',         'observacion',         'varchar(100) NULL');
CALL mg_chg_col('personacliente', 'OCU_COD',             'ocu_cod',             'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'PORCENTAJECOMISION',  'porcentajecomision',  'double NULL');
CALL mg_chg_col('personacliente', 'PORCENTAJEGARANTIA',  'porcentajegarantia',  'double NULL');
CALL mg_chg_col('personacliente', 'RAZONSOCIAL',         'razonsocial',         'varchar(100) NULL');
CALL mg_chg_col('personacliente', 'SEXO',                'sexo',                'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'SIS_COD',             'sis_cod',             'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'TDO_COD',             'tdo_cod',             'varchar(255) NULL');
CALL mg_chg_col('personacliente', 'TELEFONO',            'telefono',            'int NULL');

CALL mg_drop_fk_col('personacliente', 'iddepartamento');
CALL mg_chg_col('personacliente', 'IDDEPARTAMENTO', 'iddepartamento', 'bigint NULL');
CALL mg_add_fk('personacliente', 'fk_personacli_departamento', 'FOREIGN KEY (iddepartamento) REFERENCES departamento (iddepartamento)');

-- Las FK entrantes se sueltan por (tabla, columna): en otras bases sus nombres difieren.
CALL mg_drop_fk_col('contactocliente', 'idpersonacliente');
CALL mg_drop_fk_col('pago',            'IDPERSONACLIENTE');
CALL mg_drop_fk_col('pedidos',         'IDCLIENTE');
CALL mg_drop_fk_col('sf_tmpdet',       'idpersonacliente');
CALL mg_drop_fk_col('ventacliente',    'IDCLIENTE');
CALL mg_drop_fk_col('ventadirecta',    'IDCLIENTE');

CALL mg_chg_col('personacliente', 'IDPERSONACLIENTE', 'idpersonacliente', 'bigint NOT NULL');

CALL mg_add_fk('contactocliente', 'fk_contactocli_cliente',   'FOREIGN KEY (idpersonacliente) REFERENCES personacliente (idpersonacliente)');
CALL mg_add_fk('pago',            'pago_persona_FK',          'FOREIGN KEY (IDPERSONACLIENTE) REFERENCES personacliente (idpersonacliente)');
CALL mg_add_fk('pedidos',         'pedidos_ibfk_1',           'FOREIGN KEY (IDCLIENTE)        REFERENCES personacliente (idpersonacliente)');
CALL mg_add_fk('sf_tmpdet',       'sf_tmpdet_ibfk_1',         'FOREIGN KEY (idpersonacliente) REFERENCES personacliente (idpersonacliente)');
CALL mg_add_fk('ventacliente',    'VentaCliente_CLIENTE_FK',  'FOREIGN KEY (IDCLIENTE)        REFERENCES personacliente (idpersonacliente)');
CALL mg_add_fk('ventadirecta',    'fk_ventdir_cliente',       'FOREIGN KEY (IDCLIENTE)        REFERENCES personacliente (idpersonacliente)');


-- ===========================================================================
-- v6.0.102_terdemol
-- ===========================================================================
CALL mg_drop_col('personacliente', 'ciudad');
CALL mg_add_col('personacliente',  'idciudad', '`idciudad` BIGINT NULL AFTER iddepartamento');
CALL mg_add_fk('personacliente',   'fk_personacli_ciudad', 'FOREIGN KEY (idciudad) REFERENCES ciudad (idciudad)');

CALL mg_drop_col('contactocliente', 'ciudad');
CALL mg_add_col('contactocliente',  'idciudad', '`idciudad` BIGINT NULL AFTER iddepartamento');
CALL mg_add_key('contactocliente',  'ix_contactocli_ciudad', 'KEY ix_contactocli_ciudad (idciudad)');
CALL mg_add_fk('contactocliente',   'fk_contactocli_ciudad', 'FOREIGN KEY (idciudad) REFERENCES ciudad (idciudad)');

CALL mg_func_add('DOCUMENTTYPE',  'Catalogo de Tipos de Documento', 1, 15, 'menu.customers.configuration.documentType',  1);
CALL mg_func_add('TITLE',         'Catalogo de Titulos',            3, 15, 'menu.customers.configuration.title',         1);
CALL mg_func_add('SALUTATION',    'Catalogo de Saludos',            3, 15, 'menu.customers.configuration.salutation',    1);
CALL mg_func_add('MARITALSTATUS', 'Catalogo de Estados Civiles',    3, 15, 'menu.customers.configuration.maritalStatus', 1);
CALL mg_func_add('ORGANIZATION',  'Catalogo de Organizaciones',     3, 15, 'menu.contacts.configuration.organization',   1);


-- ===========================================================================
-- v6.0.103_terdemol
-- ===========================================================================
CALL mg_add_col('personacliente', 'fax', '`fax` VARCHAR(255) NULL AFTER email');

CREATE TABLE IF NOT EXISTS incoterm (
    idincoterm  BIGINT       NOT NULL,
    codigo      VARCHAR(10)  NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500) NULL,
    activo      INT          NOT NULL DEFAULT 1,
    idcompania  BIGINT       NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (idincoterm),
    UNIQUE KEY uq_incoterm_cia_cod (idcompania, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS centrocosto_comercial (
    idcentrocosto_comercial BIGINT       NOT NULL,
    codigo                  VARCHAR(20)  NOT NULL,
    descripcion             VARCHAR(150) NOT NULL,
    activo                  INT          NOT NULL DEFAULT 1,
    idcompania              BIGINT       NOT NULL,
    version                 BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (idcentrocosto_comercial),
    UNIQUE KEY uq_cencoscom_cia_cod (idcompania, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ordenventa_nota (
    idordenventa_nota BIGINT        NOT NULL,
    texto             VARCHAR(1000) NOT NULL,
    orden             INT           NULL DEFAULT 0,
    activo            INT           NOT NULL DEFAULT 1,
    idcompania        BIGINT        NOT NULL,
    version           BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventa_nota)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('INCOTERM',             'Catalogo de Incoterms',                  1, 15, 'menu.customers.configuration.incoterm',             1);
CALL mg_func_add('COMMERCIALCOSTCENTER', 'Catalogo de Centros de Costo Comercial', 1, 15, 'menu.customers.configuration.commercialCostCenter', 1);
CALL mg_func_add('SALESORDERNOTE',       'Catalogo de Notas de Orden de Venta',    1, 15, 'menu.customers.configuration.salesOrderNote',       1);


-- ===========================================================================
-- v6.0.104_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS ordenventa (
    idordenventa           BIGINT        NOT NULL,
    no_cia                 VARCHAR(2)    NOT NULL,
    nro_orden              VARCHAR(20)   NOT NULL,
    estado                 VARCHAR(5)    NOT NULL,
    fecha                  DATETIME      NOT NULL,
    fecha_entrega          DATE          NULL,
    idpersonacliente       BIGINT        NOT NULL,
    comprador              VARCHAR(300)  NULL,
    nro_registro           VARCHAR(50)   NULL,
    direccion              VARCHAR(500)  NULL,
    telefono               VARCHAR(50)   NULL,
    fax                    VARCHAR(50)   NULL,
    email                  VARCHAR(150)  NULL,
    moneda                 VARCHAR(1)    NOT NULL,
    idincoterm             BIGINT        NULL,
    incoterm_lugar         VARCHAR(200)  NULL,
    cotizacion             VARCHAR(100)  NULL,
    sc_codigo              VARCHAR(100)  NULL,
    cc_codigo              VARCHAR(100)  NULL,
    comentarios            VARCHAR(2000) NULL,
    observaciones_gerencia VARCHAR(2000) NULL,
    sub_total              DECIMAL(16,2) NULL DEFAULT 0,
    descuento              DECIMAL(16,2) NULL DEFAULT 0,
    recargo                DECIMAL(16,2) NULL DEFAULT 0,
    total                  DECIMAL(16,2) NULL DEFAULT 0,
    created_at             DATETIME      NULL,
    created_by             VARCHAR(50)   NULL,
    fecha_revision         DATETIME      NULL,
    fecha_aprobacion       DATETIME      NULL,
    aprobado_por           VARCHAR(50)   NULL,
    fecha_anulacion        DATETIME      NULL,
    version                BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventa),
    KEY ix_ordenventa_cliente (idpersonacliente),
    KEY ix_ordenventa_incoterm (idincoterm),
    CONSTRAINT fk_ordenventa_cliente  FOREIGN KEY (idpersonacliente) REFERENCES personacliente (idpersonacliente),
    CONSTRAINT fk_ordenventa_incoterm FOREIGN KEY (idincoterm)       REFERENCES incoterm (idincoterm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ordenventadetalle (
    idordenventadetalle     BIGINT        NOT NULL,
    no_cia                  VARCHAR(2)    NOT NULL,
    idordenventa            BIGINT        NOT NULL,
    nro                     BIGINT        NOT NULL,
    cod_art                 VARCHAR(20)   NOT NULL,
    idcentrocosto_comercial BIGINT        NULL,
    descripcion             VARCHAR(500)  NULL,
    unidad_medida           VARCHAR(50)   NULL,
    cantidad                DECIMAL(16,2) NOT NULL DEFAULT 0,
    precio_unitario         DECIMAL(16,2) NOT NULL DEFAULT 0,
    total                   DECIMAL(16,2) NOT NULL DEFAULT 0,
    version                 BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventadetalle),
    KEY ix_ordenventadet_orden (idordenventa),
    KEY ix_ordenventadet_cc (idcentrocosto_comercial),
    CONSTRAINT fk_ordenventadet_orden FOREIGN KEY (idordenventa)            REFERENCES ordenventa (idordenventa),
    CONSTRAINT fk_ordenventadet_cc    FOREIGN KEY (idcentrocosto_comercial) REFERENCES centrocosto_comercial (idcentrocosto_comercial)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ordenventa_nota_sel (
    idordenventa      BIGINT NOT NULL,
    idordenventa_nota BIGINT NOT NULL,
    PRIMARY KEY (idordenventa, idordenventa_nota),
    KEY ix_ovnotasel_nota (idordenventa_nota),
    CONSTRAINT fk_ovnotasel_orden FOREIGN KEY (idordenventa)      REFERENCES ordenventa (idordenventa),
    CONSTRAINT fk_ovnotasel_nota  FOREIGN KEY (idordenventa_nota) REFERENCES ordenventa_nota (idordenventa_nota)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_func_add('SALESORDER',        'Ordenes de Venta',                  1, 15, 'menu.customers.sales.salesOrder',         1);
CALL mg_func_add('SALESORDERSEND',    'Orden de Venta: enviar a revision', 1,  1, 'menu.customers.sales.salesOrder.send',    1);
CALL mg_func_add('SALESORDERAPPROVE', 'Orden de Venta: aprobar/rechazar',  1,  1, 'menu.customers.sales.salesOrder.approve', 1);
CALL mg_func_add('SALESORDERNULLIFY', 'Orden de Venta: anular',            1,  1, 'menu.customers.sales.salesOrder.nullify', 1);


-- ===========================================================================
-- v6.0.105_terdemol
-- ===========================================================================
CALL mg_add_col('ordenventa', 'condiciones_pago', '`condiciones_pago` VARCHAR(1000) NULL AFTER cc_codigo');


-- ===========================================================================
-- v6.0.106_terdemol
-- ===========================================================================
CREATE TABLE IF NOT EXISTS ordenventa_termino (
    idordenventa_termino BIGINT        NOT NULL,
    texto                VARCHAR(1000) NOT NULL,
    orden                INT           NULL DEFAULT 0,
    activo               INT           NOT NULL DEFAULT 1,
    idcompania           BIGINT        NOT NULL,
    version              BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventa_termino)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CALL mg_add_col('ordenventa', 'termino_cabecera', '`termino_cabecera` VARCHAR(1000) NULL AFTER condiciones_pago');

CALL mg_func_add('SALESORDERTERM', 'Catalogo de Terminos de cabecera (Orden de Venta)', 1, 15, 'menu.customers.configuration.salesOrderTerm', 1);


-- ===========================================================================
-- v6.0.107_terdemol
-- ===========================================================================
CALL mg_func_add('XPRODUCTION_DISTRIBUTIONINDIRECTCOST', 'Distribucion de costos indirectos',
                 11, 1, 'Functionality.xproduction.process.distributionIndirectCost', 1);
CALL mg_func_add('XPRODUCTION_CLOSINGPRODUCTIONCOST',    'Cierre de Ctas - Costo de Produccion',
                 11, 1, 'Functionality.xproduction.process.closingProductionCost', 1);


-- ===========================================================================
-- v6.0.108_terdemol
-- ===========================================================================
CALL mg_func_add('SALESBOX',                   'Ventas (Caja)',                          1, 1, 'Functionality.customers.salesBox',                    1);
CALL mg_func_add('SALESLIST',                  'Lista de ventas',                        1, 1, 'Functionality.customers.salesList',                   1);
CALL mg_func_add('SALESREPORTCUSTOMER',        'Reporte Ventas por Cliente',             1, 1, 'Functionality.customers.report.salesCustomer',        1);
CALL mg_func_add('SALESREPORTPRODUCT',         'Reporte Ventas por Producto',            1, 1, 'Functionality.customers.report.salesProduct',         1);
CALL mg_func_add('SALESREPORTCUSTOMERPRODUCT', 'Reporte Ventas por Cliente y Producto',  1, 1, 'Functionality.customers.report.salesCustomerProduct', 1);
CALL mg_func_add('CUSTOMERKARDEXREPORT',       'Reporte Kardex de Clientes',             1, 1, 'Functionality.customers.report.customerKardex',       1);
CALL mg_func_add('SUMMARYCLIENTSTATEREPORT',   'Reporte Resumen Estado de Clientes',     1, 1, 'Functionality.customers.report.summaryClientState',   1);


-- ===========================================================================
-- v6.0.109_terdemol
-- ===========================================================================
CALL mg_func_add('XPRODUCTION_KARDEX', 'Kardex Articulos (Produccion)', 11, 1, 'Functionality.xproduction.kardexArticulos', 1);


-- ===========================================================================
-- v6.0.110_terdemol
-- ===========================================================================
CALL mg_add_col('configuracion', 'idlogocabecera', '`idlogocabecera` BIGINT(20) DEFAULT NULL');
CALL mg_add_col('configuracion', 'idlogologin',    '`idlogologin` BIGINT(20) DEFAULT NULL');

-- permiso 5 es el valor final que dejaba el UPDATE del script original.
CALL mg_func_add('COMPANYSETTING', 'Preferencias de compania (configuracion)', 2, 5, 'Functionality.admin.companySetting', 1);

CALL mg_add_col('configuracion', 'email_unisueldo', '`email_unisueldo` VARCHAR(100) DEFAULT NULL AFTER hrsdialaboral');


-- ===========================================================================
-- v6.0.111_terdemol
-- ===========================================================================
CALL mg_func_add('CASHACCOUNTLEVEL', 'Analisis y correccion de niveles de cuentas (cta_raiz / cta_niv3)',
                 5, 5, 'Functionality.finances.cashAccountLevelAnalysis', 1);


-- ===========================================================================
-- v6.0.112_terdemol
-- ===========================================================================
CALL mg_mod_col('arcgms', 'cta_raiz', '`cta_raiz` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('arcgms', 'cta_niv3', '`cta_niv3` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');

-- [!] Las 2 FK fallan si hay cta_raiz/cta_niv3 apuntando a cuentas inexistentes.
--     Verificado en khipus local: 0 colgadas. Diagnostico si falla:
--       select cuenta, cta_raiz from arcgms a where a.cta_raiz is not null and a.cta_raiz <> ''
--        and not exists (select 1 from arcgms p where p.cuenta = a.cta_raiz);
CALL mg_add_fk('arcgms', 'fk_arcgms_cta_raiz', 'FOREIGN KEY (cta_raiz) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('arcgms', 'fk_arcgms_cta_niv3', 'FOREIGN KEY (cta_niv3) REFERENCES arcgms (cuenta)');


-- ===========================================================================
-- v6.0.113_terdemol
-- ===========================================================================
CALL mg_mod_col('configuracion', 'cajagral1mn',    '`cajagral1mn` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ct_cajaahorro',  '`ct_cajaahorro` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ct_cajaveter',   '`ct_cajaveter` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cta_pat01',      '`cta_pat01` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cta_pat02',      '`cta_pat02` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cta_pat03',      '`cta_pat03` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cta_pat04',      '`cta_pat04` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cta_pat05',      '`cta_pat05` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaAlmPT',       '`ctaAlmPT` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaAlmPTAG',     '`ctaAlmPTAG` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaAlmPV',       '`ctaAlmPV` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctacomision',    '`ctacomision` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaCostPT',      '`ctaCostPT` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaCostPV',      '`ctaCostPV` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaG_it',        '`ctaG_it` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaI_ventapri',  '`ctaI_ventapri` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaI_ventasec',  '`ctaI_ventasec` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaMerma',       '`ctaMerma` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaMermaBaj',    '`ctaMermaBaj` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaP_debFisIva', '`ctaP_debFisIva` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaP_itxpagar',  '`ctaP_itxpagar` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaProm',        '`ctaProm` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaprovaf',      '`ctaprovaf` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'ctaReproc',      '`ctaReproc` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cxp_cns',        '`cxp_cns` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cxp_iva',        '`cxp_iva` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cxp_provmn',     '`cxp_provmn` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'cxp_regalia',    '`cxp_regalia` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'i_pvig_pf_mn',   '`i_pvig_pf_mn` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'it_ret',         '`it_ret` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'iue_ret',        '`iue_ret` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'oc_pagodefault', '`oc_pagodefault` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'res_perdida',    '`res_perdida` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');
CALL mg_mod_col('configuracion', 'res_utilidad',   '`res_utilidad` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL');

-- Saneamiento previo: toda cuenta de configuracion que no exista en arcgms pasa a
-- NULL, si no la FK correspondiente no se puede crear. Generaliza el UPDATE de
-- rescate que query_v6.0.113 traia comentado (cubria 11 columnas; faltaba ctaAlmPTAG).
-- Las cuentas que queden en NULL hay que reconfigurarlas en Preferencias de compania.
CALL mg_null_fk('configuracion', 'ctaaitb',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaantprovme',    'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaantprovmn',    'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctadiftipcam',    'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctadeptrame',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctadeptramn',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaafet',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaivacrefime',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaivacrefimn',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaivacrefitrmn', 'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaprovobu',      'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaalmme',        'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctatransalmme',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaalmmn',        'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctatransalm1mn',  'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctatransalm2mn',  'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctatransalmmn',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'iue_ret',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'it_ret',          'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaCostPT',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaAlmPT',        'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaAlmPTAG',      'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaCostPV',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaAlmPV',        'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaMerma',        'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaProm',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaMermaBaj',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaReproc',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ct_cajaahorro',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ct_cajaveter',    'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cajagral1mn',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'i_pvig_pf_mn',    'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaprovaf',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaG_it',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaP_debFisIva',  'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaP_itxpagar',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaI_ventapri',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctaI_ventasec',   'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'ctacomision',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cxp_provmn',      'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cta_pat01',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cta_pat02',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cta_pat03',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cta_pat04',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cta_pat05',       'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cxp_iva',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cxp_regalia',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'cxp_cns',         'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'res_perdida',     'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'res_utilidad',    'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'oc_pagodefault',  'arcgms', 'cuenta');

CALL mg_add_fk('configuracion', 'fk_configuracion_ctaaitb',         'FOREIGN KEY (ctaaitb) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaantprovme',    'FOREIGN KEY (ctaantprovme) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaantprovmn',    'FOREIGN KEY (ctaantprovmn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctadiftipcam',    'FOREIGN KEY (ctadiftipcam) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctadeptrame',     'FOREIGN KEY (ctadeptrame) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctadeptramn',     'FOREIGN KEY (ctadeptramn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaafet',         'FOREIGN KEY (ctaafet) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaivacrefime',   'FOREIGN KEY (ctaivacrefime) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaivacrefimn',   'FOREIGN KEY (ctaivacrefimn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaivacrefitrmn', 'FOREIGN KEY (ctaivacrefitrmn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaprovobu',      'FOREIGN KEY (ctaprovobu) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaalmme',        'FOREIGN KEY (ctaalmme) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctatransalmme',   'FOREIGN KEY (ctatransalmme) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaalmmn',        'FOREIGN KEY (ctaalmmn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctatransalm1mn',  'FOREIGN KEY (ctatransalm1mn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctatransalm2mn',  'FOREIGN KEY (ctatransalm2mn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctatransalmmn',   'FOREIGN KEY (ctatransalmmn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_iue_ret',         'FOREIGN KEY (iue_ret) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_it_ret',          'FOREIGN KEY (it_ret) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctacostpt',       'FOREIGN KEY (ctaCostPT) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaalmpt',        'FOREIGN KEY (ctaAlmPT) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaalmptag',      'FOREIGN KEY (ctaAlmPTAG) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctacostpv',       'FOREIGN KEY (ctaCostPV) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaalmpv',        'FOREIGN KEY (ctaAlmPV) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctamerma',        'FOREIGN KEY (ctaMerma) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaprom',         'FOREIGN KEY (ctaProm) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctamermabaj',     'FOREIGN KEY (ctaMermaBaj) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctareproc',       'FOREIGN KEY (ctaReproc) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ct_cajaahorro',   'FOREIGN KEY (ct_cajaahorro) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ct_cajaveter',    'FOREIGN KEY (ct_cajaveter) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cajagral1mn',     'FOREIGN KEY (cajagral1mn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_i_pvig_pf_mn',    'FOREIGN KEY (i_pvig_pf_mn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctaprovaf',       'FOREIGN KEY (ctaprovaf) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctag_it',         'FOREIGN KEY (ctaG_it) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctap_debfisiva',  'FOREIGN KEY (ctaP_debFisIva) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctap_itxpagar',   'FOREIGN KEY (ctaP_itxpagar) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctai_ventapri',   'FOREIGN KEY (ctaI_ventapri) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctai_ventasec',   'FOREIGN KEY (ctaI_ventasec) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_ctacomision',     'FOREIGN KEY (ctacomision) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cxp_provmn',      'FOREIGN KEY (cxp_provmn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cta_pat01',       'FOREIGN KEY (cta_pat01) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cta_pat02',       'FOREIGN KEY (cta_pat02) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cta_pat03',       'FOREIGN KEY (cta_pat03) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cta_pat04',       'FOREIGN KEY (cta_pat04) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cta_pat05',       'FOREIGN KEY (cta_pat05) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cxp_iva',         'FOREIGN KEY (cxp_iva) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cxp_regalia',     'FOREIGN KEY (cxp_regalia) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_cxp_cns',         'FOREIGN KEY (cxp_cns) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_res_perdida',     'FOREIGN KEY (res_perdida) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_res_utilidad',    'FOREIGN KEY (res_utilidad) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion', 'fk_configuracion_oc_pagodefault',  'FOREIGN KEY (oc_pagodefault) REFERENCES arcgms (cuenta)');

CALL mg_func_add('CASHACCOUNTRENAME', 'Renumerar cuenta contable (cambia el codigo)',
                 5, 4, 'Functionality.finances.cashAccountRename', 1);


-- ===========================================================================
-- v6.0.114_terdemol
-- ===========================================================================
CALL mg_mod_col('_sequence', 'seq_val', '`seq_val` BIGINT NOT NULL');
CALL mg_add_col('_sequence', 'idcompania', '`idcompania` BIGINT NULL');
CALL mg_add_col('_sequence', 'version',    '`version` BIGINT NOT NULL DEFAULT 0');

-- Backfill obligatorio: sin el, el MODIFY a NOT NULL falla sobre las filas existentes.
-- Compania 1, igual que el script original (query_v6.0.114 usaba @company_id := 1).
UPDATE _sequence SET idcompania = 1 WHERE idcompania IS NULL;
CALL mg_mod_col('_sequence', 'idcompania', '`idcompania` BIGINT NOT NULL');
CALL mg_set_pk('_sequence', 'seq_name,idcompania', 'seq_name, idcompania');
CALL mg_add_fk('_sequence', 'fk_sequence_compania', 'FOREIGN KEY (idcompania) REFERENCES compania (idcompania)');

CALL mg_add_col('sf_tmpenc', 'idcompania', '`idcompania` BIGINT NULL');
UPDATE sf_tmpenc SET idcompania = 1 WHERE idcompania IS NULL;
CALL mg_mod_col('sf_tmpenc', 'idcompania', '`idcompania` BIGINT NOT NULL');
CALL mg_add_fk('sf_tmpenc',  'fk_sf_tmpenc_compania', 'FOREIGN KEY (idcompania) REFERENCES compania (idcompania)');

CALL mg_add_col('sf_tmpdet', 'idcompania', '`idcompania` BIGINT NULL');
UPDATE sf_tmpdet SET idcompania = 1 WHERE idcompania IS NULL;
CALL mg_mod_col('sf_tmpdet', 'idcompania', '`idcompania` BIGINT NOT NULL');
CALL mg_add_fk('sf_tmpdet',  'fk_sf_tmpdet_compania', 'FOREIGN KEY (idcompania) REFERENCES compania (idcompania)');

CALL mg_add_col('sf_tmpenc', 'version',   '`version` BIGINT NOT NULL DEFAULT 0');
CALL mg_add_col('sf_tmpdet', 'version',   '`version` BIGINT NOT NULL DEFAULT 0');
CALL mg_add_col('sf_tmpdet', 'nro_orden', '`nro_orden` INT NULL');


-- ===========================================================================
-- v6.0.115_terdemol
-- ===========================================================================
DROP TABLE IF EXISTS detallepagoacopiomp;
DROP TABLE IF EXISTS pagoparcialacopiomp;
DROP TABLE IF EXISTS descuentoacopiomp;
DROP TABLE IF EXISTS tipodescuentoacopiomp;
DROP TABLE IF EXISTS pagoacopiomp;

CALL mg_func_del('RAWMATERIALPAYMENTREQUEST');
CALL mg_func_del('PAYMENT_RAWMATERIALPRODUCER');


-- ===========================================================================
-- v6.0.117_terdemol
-- ===========================================================================
CALL mg_add_col('cxp_proveedores', 'ctaxcobrar', '`ctaxcobrar` VARCHAR(20) NULL AFTER `ctaxpagar`');


-- ===========================================================================
-- v6.0.120_terdemol
-- ===========================================================================
CALL mg_mod_col('xpr_linea', 'merma_factor', '`merma_factor` DECIMAL(10,4) NULL');


-- ===========================================================================
-- v6.0.121_terdemol
-- ===========================================================================
CALL mg_add_col('configuracion', 'i_ppag_dpf_mn', '`i_ppag_dpf_mn` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT ''Gasto intereses provision DPF MN''');
CALL mg_add_col('configuracion', 'i_ppag_dpf_me', '`i_ppag_dpf_me` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT ''Gasto intereses provision DPF ME''');
CALL mg_null_fk('configuracion', 'i_ppag_dpf_mn', 'arcgms', 'cuenta');
CALL mg_null_fk('configuracion', 'i_ppag_dpf_me', 'arcgms', 'cuenta');
CALL mg_add_fk('configuracion',  'fk_configuracion_i_ppag_dpf_mn', 'FOREIGN KEY (i_ppag_dpf_mn) REFERENCES arcgms (cuenta)');
CALL mg_add_fk('configuracion',  'fk_configuracion_i_ppag_dpf_me', 'FOREIGN KEY (i_ppag_dpf_me) REFERENCES arcgms (cuenta)');

CALL mg_func_add('FINANCESEXCHANGERATE', 'Tipos de cambio de contabilidad (arcgtc)',    5, 15, 'Functionality.finances.financesExchangeRate', 1);
CALL mg_func_add('PROVISIONDPF',         'Provision de intereses por pagar sobre DPF',  1,  3, 'Functionality.customers.provisionDPF',        1);


-- ===========================================================================
-- v6.0.122_terdemol
-- ===========================================================================
CALL mg_func_add('FINANCESBANKACCOUNT', 'Cuentas bancarias (ck_ctas_bco)', 5, 15, 'Functionality.finances.financesBankAccount', 1);

-- Backfill obligatorio: open/close nacen NULL en v6.0.53 y aqui pasan a NOT NULL.
-- Es el mismo UPDATE que traia el script original de v6.0.122.
UPDATE sf_tmpenc SET `open`  = 0 WHERE `open`  IS NULL;
UPDATE sf_tmpenc SET `close` = 0 WHERE `close` IS NULL;
CALL mg_mod_col('sf_tmpenc', 'open',  '`open` int NOT NULL DEFAULT 0');
CALL mg_mod_col('sf_tmpenc', 'close', '`close` int NOT NULL DEFAULT 0');

CALL mg_func_add('ACCOUNTTYPE', 'Tipos de cuenta de ahorro (tipocuenta)', 1, 15, 'Functionality.customers.accountType', 1);

CALL mg_add_col('configuracion', 'cajagral1me', '`cajagral1me` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT ''Caja general ME''');
CALL mg_null_fk('configuracion', 'cajagral1me', 'arcgms', 'cuenta');
CALL mg_add_fk('configuracion',  'fk_configuracion_cajagral1me', 'FOREIGN KEY (cajagral1me) REFERENCES arcgms (cuenta)');

CALL mg_func_add('DPFCLOSE', 'Cierre de Depositos a Plazo Fijo', 1, 3, 'Functionality.customers.dpfClose', 1);

CALL mg_add_col('configuracion', 'tipo_doc_dpf', '`tipo_doc_dpf` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT ''Comprobante de apertura DPF''');
CALL mg_null_fk('configuracion', 'tipo_doc_dpf', 'tipodoc', 'nombre');
CALL mg_add_fk('configuracion',  'fk_configuracion_tipo_doc_dpf', 'FOREIGN KEY (tipo_doc_dpf) REFERENCES tipodoc (nombre)');

CALL mg_func_add('ACCOUNTANNUL', 'Anulacion de cuentas de ahorro y DPF', 1, 4, 'Functionality.customers.accountAnnul', 1);


-- ===========================================================================
-- v6.0.123_terdemol
-- ===========================================================================
CALL mg_add_col('acopiomp', 'created_at', '`created_at` DATETIME NULL AFTER version');
CALL mg_add_col('acopiomp', 'created_by', '`created_by` VARCHAR(100) CHARACTER SET utf8mb4 NULL AFTER created_at');
CALL mg_add_col('acopiomp', 'updated_at', '`updated_at` DATETIME NULL AFTER created_by');
CALL mg_add_col('acopiomp', 'updated_by', '`updated_by` VARCHAR(100) CHARACTER SET utf8mb4 NULL AFTER updated_at');

CALL mg_add_col('acopiomp', 'id_tmpenc',               '`id_tmpenc` BIGINT NULL AFTER updated_by');
CALL mg_add_col('acopiomp', 'nro_reversiones',         '`nro_reversiones` INT NOT NULL DEFAULT 0 AFTER id_tmpenc');
CALL mg_add_col('acopiomp', 'ultima_reversion_at',     '`ultima_reversion_at` DATETIME NULL AFTER nro_reversiones');
CALL mg_add_col('acopiomp', 'ultima_reversion_by',     '`ultima_reversion_by` VARCHAR(100) CHARACTER SET utf8mb4 NULL AFTER ultima_reversion_at');
CALL mg_add_col('acopiomp', 'ultima_reversion_motivo', '`ultima_reversion_motivo` VARCHAR(500) CHARACTER SET utf8mb4 NULL AFTER ultima_reversion_by');
CALL mg_add_key('acopiomp', 'ix_acopiomp_tmpenc', 'KEY ix_acopiomp_tmpenc (id_tmpenc)');

CREATE TABLE IF NOT EXISTS acopiomp_reversion (
    idacopiomp_reversion BIGINT        NOT NULL AUTO_INCREMENT,
    idacopiomp           BIGINT        NOT NULL,
    fecha_hora           DATETIME      NOT NULL,
    usuario              VARCHAR(100)  NOT NULL,
    motivo               VARCHAR(500)  NOT NULL,
    estado_anterior      VARCHAR(25)   NOT NULL,
    id_tmpenc            BIGINT        NULL,
    saldo_antes          DECIMAL(12,2) NULL,
    saldo_despues        DECIMAL(12,2) NULL,
    costo_antes          DECIMAL(16,6) NULL,
    costo_despues        DECIMAL(16,6) NULL,
    version              BIGINT        DEFAULT 0,
    idcompania           BIGINT        NOT NULL,
    PRIMARY KEY (idacopiomp_reversion),
    KEY ix_acopiomp_reversion_acopio (idacopiomp),
    CONSTRAINT fk_acopiomp_reversion_acopio   FOREIGN KEY (idacopiomp) REFERENCES acopiomp (idacopiomp),
    CONSTRAINT fk_acopiomp_reversion_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CALL mg_add_col('configuracion', 'acopio_dias_reversion', '`acopio_dias_reversion` INT NOT NULL DEFAULT 30');

CALL mg_func_add('COLLECTMATERIALREVERT', 'Revertir acopio de materia prima', 6, 1, 'Functionality.production.collectMaterialRevert', 1);


-- ===========================================================================
-- v6.0.124_terdemol
-- ===========================================================================
CALL mg_func_add('DAILYCOLLECTIONREPORT', 'Reporte de Recaudacion Diaria (CISC)', 5, 1, 'Functionality.accounting.dailyCollectionReport', 1);


-- ===========================================================================
-- v6.0.125_terdemol
-- ===========================================================================
-- LEGACYFINANCIALADMINMANAGEMENT se da de alta y se elimina dentro de este mismo
-- script; se conserva la secuencia original para no alterar el resultado final.
CALL mg_func_add('LEGACYFINANCIALADMINMANAGEMENT', 'Legacy Gestion Administrativa Financiera', 2, 1, 'Functionality.admin.legacyFinancialAdminManagement', 1);
CALL mg_func_add('LEGACYPRODUCTIONPROCESSES',      'Legacy Procesos Productivos',              2, 1, 'Functionality.admin.legacyProductionProcesses',      1);

CALL mg_func_del('PREPAREDELIVERY');
CALL mg_func_del('APPROVEDALLACCOUNTENTRIES');
CALL mg_func_del('PENDINGSOLDPRODUCTDELIVERYREPORT');

CALL mg_func_add('TAXRULE',          'Reglas tributarias',        5, 15, 'Functionality.finances.taxRule',          1);
CALL mg_func_add('TAXPERCENTAGE',    'Porcentajes tributarios',   5, 15, 'Functionality.finances.taxPercentage',    1);
CALL mg_func_add('CASHACCOUNTGROUP', 'Grupos de cuenta contable', 5, 15, 'Functionality.finances.cashAccountGroup', 1);

CALL mg_func_add('ENTRYBUDGETREPORT',                   'Reporte de presupuesto de ingresos',                       5, 1, 'Functionality.finances.budget.entryBudgetReport',                       1);
CALL mg_func_add('EXPENSEBUDGETREPORT',                 'Reporte de presupuesto de gastos',                         5, 1, 'Functionality.finances.budget.expenseBudgetReport',                     1);
CALL mg_func_add('EXPENSEBUDGETEXECUTIONREPORT',        'Reporte de ejecucion de presupuesto de gasto',             5, 1, 'Functionality.finances.budget.expenseBudgetExecutionReport',            1);
CALL mg_func_add('EXPENSEBUDGETCONSOLIDATEDEXECREPORT', 'Reporte de ejecucion de presupuesto de gasto consolidado', 5, 1, 'Functionality.finances.budget.expenseBudgetConsolidatedExecutionReport', 1);
CALL mg_func_add('EXPENSEBUDGETGLOBALEXECUTIONREPORT',  'Reporte de ejecucion de presupuesto de gasto global',      5, 1, 'Functionality.finances.budget.expenseBudgetGlobalExecutionReport',      1);
CALL mg_func_add('REPORTROTATORYFUND',                  'Reporte de fondo rotatorio',                               5, 1, 'Functionality.finances.report.rotatoryFund',                            1);
CALL mg_func_add('REPORTROTATORYFUNDBALANCES',          'Reporte de saldos de fondos rotatorios',                   5, 1, 'Functionality.finances.report.rotatoryFundBalances',                    1);
CALL mg_func_add('REPORTROTATORYFUNDBALANCESDETAIL',    'Reporte de detalle de saldos de fondos rotatorios',        5, 1, 'Functionality.finances.report.rotatoryFundBalancesDetail',              1);
CALL mg_func_add('REPORTROTATORYFUNDPAYRECEIVABLE',     'Reporte de pagos de fondos rotatorios por cobrar',         5, 1, 'Functionality.finances.report.rotatoryFundPaymentReceivable',           1);
CALL mg_func_add('REPORTRETRIEVEBYDEBTOR',              'Reporte de cobros por deudor',                             5, 1, 'Functionality.finances.report.retrieveByDebtor',                        1);
CALL mg_func_add('REPORTROTATORYFUNDCOLLECTIONDETAIL',  'Reporte de detalle de cobros de fondos rotatorios',        5, 1, 'Functionality.finances.report.rotatoryFundCollectionDetail',            1);
CALL mg_func_add('REPORTROTATORYFUNDINVOICECOLLECTION', 'Reporte de cobro de facturas de fondos rotatorios',        5, 1, 'Functionality.finances.report.rotatoryFundInvoiceCollection',           1);
CALL mg_func_add('ROTFUNDBYACCOUNTREPORT',              'Reporte de fondos rotatorios por cuenta contable',         5, 1, 'Functionality.finances.report.rotatoryFundByAccount',                   1);

CALL mg_func_del('LEGACYFINANCIALADMINMANAGEMENT');


-- ---------------------------------------------------------------------------
-- Limpieza de los helpers.
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS mg_add_col;
DROP PROCEDURE IF EXISTS mg_drop_col;
DROP PROCEDURE IF EXISTS mg_mod_col;
DROP PROCEDURE IF EXISTS mg_chg_col;
DROP PROCEDURE IF EXISTS mg_add_key;
DROP PROCEDURE IF EXISTS mg_drop_key;
DROP PROCEDURE IF EXISTS mg_add_fk;
DROP PROCEDURE IF EXISTS mg_drop_fk_col;
DROP PROCEDURE IF EXISTS mg_set_pk;
DROP PROCEDURE IF EXISTS mg_null_fk;
DROP PROCEDURE IF EXISTS mg_func_add;
DROP PROCEDURE IF EXISTS mg_func_del;
