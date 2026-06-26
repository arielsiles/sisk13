-- ============================================================================
-- v6.0.89 :: Restriccion de Vales de Almacen por Usuario
--             Schema + permiso.
--   Permite limitar, para ciertos usuarios, los Tipos de Documento, Almacenes
--   y Articulos disponibles al crear/editar un Vale de Almacen
--   (view/warehouse/warehouseVoucherCreate.xhtml y warehouseVoucherUpdate.xhtml).
--
--   Diseno OPT-IN: un usuario queda restringido SOLO si tiene una fila en
--   inv_vale_restriccion con activo=1. Sin fila (o activo=0) el comportamiento
--   es identico al actual -> cero impacto para usuarios en produccion.
--   Una lista de detalle vacia = sin restriccion en esa dimension (Interp. A).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0.0 Charset / collation
-- ----------------------------------------------------------------------------
--   Las referencias a catalogos legacy (inv_almacenes, inv_articulos,
--   inv_tipodocs) tienen PK compuesta en CHARACTER SET utf8 COLLATE utf8_bin;
--   se replica el mismo charset/collation en las columnas locales para evitar
--   el error MySQL 3780/1822. No se declaran FK fisicas hacia esos catalogos
--   (mismo criterio que inv_valedespacho_det): la integridad de
--   Warehouse/ProductItem/DocumentType se valida a nivel JPA. Solo indices.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 0.1 Cabecera: configuracion de restriccion por usuario
-- ----------------------------------------------------------------------------
CREATE TABLE inv_vale_restriccion (
    idvalerestriccion BIGINT     NOT NULL AUTO_INCREMENT,
    idusuario         BIGINT     NOT NULL,
    activo            TINYINT(1) NOT NULL DEFAULT 1,
    version           BIGINT     DEFAULT 0,
    idcompania        BIGINT     NOT NULL,
    PRIMARY KEY (idvalerestriccion),
    UNIQUE KEY uq_valerestriccion_usuario (idcompania, idusuario),
    CONSTRAINT fk_valerestriccion_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania),
    CONSTRAINT fk_valerestriccion_usuario
        FOREIGN KEY (idusuario) REFERENCES usuario (idusuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 0.2 Detalle: Tipos de Documento permitidos  (WarehouseDocumentType: no_cia, cod_doc)
-- ----------------------------------------------------------------------------
CREATE TABLE inv_vale_restriccion_tipodoc (
    idrestricciontipodoc BIGINT     NOT NULL AUTO_INCREMENT,
    idvalerestriccion    BIGINT     NOT NULL,
    no_cia               VARCHAR(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_doc              VARCHAR(3) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    idcompania           BIGINT     NOT NULL,
    PRIMARY KEY (idrestricciontipodoc),
    UNIQUE KEY uq_restriccion_tipodoc (idvalerestriccion, no_cia, cod_doc),
    KEY ix_restriccion_tipodoc (no_cia, cod_doc),
    CONSTRAINT fk_restriccion_tipodoc_cab
        FOREIGN KEY (idvalerestriccion) REFERENCES inv_vale_restriccion (idvalerestriccion)
        ON DELETE CASCADE,
    CONSTRAINT fk_restriccion_tipodoc_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 0.3 Detalle: Almacenes permitidos  (Warehouse: no_cia, cod_alm)
-- ----------------------------------------------------------------------------
CREATE TABLE inv_vale_restriccion_almacen (
    idrestriccionalmacen BIGINT     NOT NULL AUTO_INCREMENT,
    idvalerestriccion    BIGINT     NOT NULL,
    no_cia               VARCHAR(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_alm              VARCHAR(6) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    idcompania           BIGINT     NOT NULL,
    PRIMARY KEY (idrestriccionalmacen),
    UNIQUE KEY uq_restriccion_almacen (idvalerestriccion, no_cia, cod_alm),
    KEY ix_restriccion_almacen (no_cia, cod_alm),
    CONSTRAINT fk_restriccion_almacen_cab
        FOREIGN KEY (idvalerestriccion) REFERENCES inv_vale_restriccion (idvalerestriccion)
        ON DELETE CASCADE,
    CONSTRAINT fk_restriccion_almacen_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 0.4 Detalle: Articulos permitidos  (ProductItem: no_cia, cod_art)
-- ----------------------------------------------------------------------------
CREATE TABLE inv_vale_restriccion_articulo (
    idrestriccionarticulo BIGINT     NOT NULL AUTO_INCREMENT,
    idvalerestriccion     BIGINT     NOT NULL,
    no_cia                VARCHAR(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    cod_art               VARCHAR(6) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    idcompania            BIGINT     NOT NULL,
    PRIMARY KEY (idrestriccionarticulo),
    UNIQUE KEY uq_restriccion_articulo (idvalerestriccion, no_cia, cod_art),
    KEY ix_restriccion_articulo (no_cia, cod_art),
    CONSTRAINT fk_restriccion_articulo_cab
        FOREIGN KEY (idvalerestriccion) REFERENCES inv_vale_restriccion (idvalerestriccion)
        ON DELETE CASCADE,
    CONSTRAINT fk_restriccion_articulo_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 0.5 Permiso (funcionalidad) para administrar las restricciones
-- ----------------------------------------------------------------------------
--   Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. CRUD completo = 15.
--   idmodulo = 5 (warehouse). Columnas: (idfuncionalidad, codigo, descripcion,
--   idmodulo, permiso, nombrerecurso, idcompania).
--   Esta funcionalidad SOLO protege la pantalla de administracion. El que un
--   usuario quede restringido depende de su fila en inv_vale_restriccion.
-- ----------------------------------------------------------------------------
insert into funcionalidad values (477, 'WAREHOUSEVOUCHERRESTRICTION', 'Restriccion de Vales por Usuario', 5, 15, 'menu.warehouse.voucher.restriction', 1);
-- Asignacion por defecto al rol Administrador (idrol=1):
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (476, 1, 15, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';
