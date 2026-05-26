-- ============================================================================
-- v6.0.80 :: Vale de Despacho - Fase 9.A
--            Catalogo Conductor / Vehiculo (M:N) y refactor de inv_valedespacho.
--            Plan: docs/dispatch_voucher_implementation_plan.md (seccion 14).
-- ============================================================================
--
--  Cambios:
--  --------
--   1) Limpia los datos de prueba existentes en inv_valedespacho* (estamos
--      en pruebas; no hay datos productivos que migrar).
--   2) Crea inv_conductor   (catalogo de conductores).
--   3) Crea inv_vehiculo    (catalogo de vehiculos de transporte).
--   4) Crea inv_conductor_vehiculo  (tabla puente M:N).
--   5) En inv_valedespacho:
--        DROP de las 6 columnas string del conductor/vehiculo.
--        ADD  de idconductor + idvehiculo (FKs).
--   6) Agrega permisos WAREHOUSEDRIVER, WAREHOUSEVEHICLE en funcionalidad.
--
--  Notas:
--  ------
--   * Estado VIG/ANL (borrado logico) en ambos catalogos: permite filtrar en
--     selectores (WHERE estado='VIG') sin perder historial referenciado por
--     despachos antiguos.
--   * Sin no_cia: el catalogo es 100% multi-empresa via idcompania (modelo
--     moderno, no compuesto al estilo legacy).
--   * Licencia unica por idcompania, Placa unica por idcompania.
--   * idmodulo = 5 (warehouse) alineado con WAREHOUSEDISPATCH*.
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 9.A.1  Limpieza de datos de prueba (estamos en pruebas)
-- ----------------------------------------------------------------------------
DELETE FROM inv_valedespacho_det;
DELETE FROM inv_valedespacho;


-- ----------------------------------------------------------------------------
-- 9.A.2  Catalogo de Conductores
-- ----------------------------------------------------------------------------
CREATE TABLE inv_conductor (
    idconductor       BIGINT       NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(120) NOT NULL,
    licencia          VARCHAR(30)  NOT NULL,
    celular           VARCHAR(30)  NULL,
    estado            VARCHAR(3)   NOT NULL DEFAULT 'VIG',    -- VIG | ANL
    createdby         VARCHAR(4)   NULL,
    createddate       DATETIME     NULL,
    updatedby         VARCHAR(4)   NULL,
    updateddate       DATETIME     NULL,
    version           BIGINT       DEFAULT 0,
    idcompania        BIGINT       NOT NULL,
    PRIMARY KEY (idconductor),
    UNIQUE KEY uq_conductor_licencia (idcompania, licencia),
    KEY ix_conductor_nombre (idcompania, nombre),
    KEY ix_conductor_estado (idcompania, estado),
    CONSTRAINT fk_conductor_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 9.A.3  Catalogo de Vehiculos de Transporte
-- ----------------------------------------------------------------------------
CREATE TABLE inv_vehiculo (
    idvehiculo        BIGINT       NOT NULL AUTO_INCREMENT,
    placa             VARCHAR(20)  NOT NULL,
    marca             VARCHAR(50)  NULL,
    color             VARCHAR(30)  NULL,
    estado            VARCHAR(3)   NOT NULL DEFAULT 'VIG',    -- VIG | ANL
    createdby         VARCHAR(4)   NULL,
    createddate       DATETIME     NULL,
    updatedby         VARCHAR(4)   NULL,
    updateddate       DATETIME     NULL,
    version           BIGINT       DEFAULT 0,
    idcompania        BIGINT       NOT NULL,
    PRIMARY KEY (idvehiculo),
    UNIQUE KEY uq_vehiculo_placa (idcompania, placa),
    KEY ix_vehiculo_estado (idcompania, estado),
    CONSTRAINT fk_vehiculo_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 9.A.4  Asociacion M:N Conductor <-> Vehiculo
--         Un conductor puede operar varios vehiculos; un vehiculo puede ser
--         operado por varios conductores.
-- ----------------------------------------------------------------------------
CREATE TABLE inv_conductor_vehiculo (
    idconductor       BIGINT       NOT NULL,
    idvehiculo        BIGINT       NOT NULL,
    PRIMARY KEY (idconductor, idvehiculo),
    KEY ix_cond_veh_veh (idvehiculo),
    CONSTRAINT fk_condveh_conductor
        FOREIGN KEY (idconductor) REFERENCES inv_conductor (idconductor)
        ON DELETE CASCADE,
    CONSTRAINT fk_condveh_vehiculo
        FOREIGN KEY (idvehiculo) REFERENCES inv_vehiculo (idvehiculo)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 9.A.5  Refactor de inv_valedespacho
--         DROP de las 6 columnas string (conductor_*, vehiculo_*)
--         ADD  idconductor + idvehiculo + FKs
-- ----------------------------------------------------------------------------
ALTER TABLE inv_valedespacho
    DROP COLUMN conductor_nombre,
    DROP COLUMN conductor_licencia,
    DROP COLUMN conductor_celular,
    DROP COLUMN vehiculo_placa,
    DROP COLUMN vehiculo_marca,
    DROP COLUMN vehiculo_color,
    ADD COLUMN idconductor BIGINT NULL AFTER idlugar_destino,
    ADD COLUMN idvehiculo  BIGINT NULL AFTER idconductor,
    ADD KEY ix_valedespacho_conductor (idconductor),
    ADD KEY ix_valedespacho_vehiculo  (idvehiculo),
    ADD CONSTRAINT fk_valedespacho_conductor
        FOREIGN KEY (idconductor) REFERENCES inv_conductor (idconductor),
    ADD CONSTRAINT fk_valedespacho_vehiculo
        FOREIGN KEY (idvehiculo)  REFERENCES inv_vehiculo  (idvehiculo);


-- ----------------------------------------------------------------------------
-- 9.A.6  Permisos
-- ----------------------------------------------------------------------------
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8.
--   CRUD completo = 15.
--   idmodulo = 5 (warehouse), igual que el resto de funcionalidades del modulo.
--   Columnas funcionalidad: (idfuncionalidad, codigo, descripcion, idmodulo,
--                            permiso, nombrerecurso, idcompania).
-- ----------------------------------------------------------------------------
insert into funcionalidad values (459, 'WAREHOUSEDRIVER',  'Catalogo de Conductores',             5, 15, 'menu.warehouse.dispatch.driver',  1);
insert into funcionalidad values (460, 'WAREHOUSEVEHICLE', 'Catalogo de Vehiculos de Transporte', 5, 15, 'menu.warehouse.dispatch.vehicle', 1);

-- Asignacion por defecto al rol Administrador (idrol=1).
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (459, 1, 15, 1, 5);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (460, 1, 15, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';
