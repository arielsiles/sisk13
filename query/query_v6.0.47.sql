-- ============================================================================
-- query_v6.0.47.sql  (rama dev_ilva / feat/acopio-excedentes)
-- ============================================================================
-- FASE 1 del pago de excedentes de acopio por cupo.
-- Ver docs/acopio/requerimiento-excedentes-acopio.md
--
-- Crea el catalogo de Restriccion de Acopio por Productor (cupo diario +
-- precios de excedente por productor, con vigencia). No afecta la generacion
-- de planillas (eso es Fase 2/3).
-- ============================================================================

-- 1) Tabla de configuracion -------------------------------------------------
CREATE TABLE IF NOT EXISTS restriccion_acopio_productor (
    idrestriccion_acopio_productor  BIGINT       NOT NULL,
    idproductormateriaprima         BIGINT       NOT NULL,
    cupolitrosdia                   DECIMAL(16,2) NOT NULL,
    precioexcedentehabil            DECIMAL(9,2)  NOT NULL,
    precioexcedentedomingo          DECIMAL(9,2)  NOT NULL,
    fechaini                        DATE          NOT NULL,
    fechafin                        DATE          NOT NULL,
    estado                          VARCHAR(10)   NOT NULL,
    PRIMARY KEY (idrestriccion_acopio_productor),
    CONSTRAINT fk_restacopio_productor
        FOREIGN KEY (idproductormateriaprima)
        REFERENCES productormateriaprima (idproductormateriaprima)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 2) Secuencia de la tabla (TableGenerator) ---------------------------------
INSERT INTO secuencia (tabla, valor)
    SELECT 'restriccion_acopio_productor', 0 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'restriccion_acopio_productor');

-- 3) Funcionalidad / permiso del catalogo -----------------------------------
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. CRUD completo = 15.
--   idmodulo se toma de un permiso de produccion existente (RESERVPRODUCERMILK,
--   catalogo "Reserva productores lecheros" del mismo menu).
--   Los subselect van envueltos en derivadas para evitar el error 1093 de MySQL
--   (no poder leer la tabla destino en el mismo INSERT).
--   NOTA: si en la BD no existiera 'RESERVPRODUCERMILK', reemplazar el idmodulo
--   por el del modulo de Produccion manualmente.
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT
    (SELECT MAX(t.idfuncionalidad) + 1 FROM (SELECT idfuncionalidad FROM funcionalidad) t),
    'PRODUCERCOLLECTIONRESTRICTION',
    'Restriccion de Acopio por Productor (cupo/excedente)',
    (SELECT t2.idmodulo FROM (SELECT idmodulo, codigo FROM funcionalidad) t2 WHERE t2.codigo = 'RESERVPRODUCERMILK' LIMIT 1),
    15,
    'Functionality.production.producerCollectionRestriction',
    1
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM (SELECT codigo FROM funcionalidad) fx WHERE fx.codigo = 'PRODUCERCOLLECTIONRESTRICTION');

-- Actualizar secuencia interna de funcionalidad
UPDATE secuencia SET valor = (SELECT MAX(t.idfuncionalidad) + 1 FROM (SELECT idfuncionalidad FROM funcionalidad) t)
WHERE tabla = 'funcionalidad';

-- Asignacion por defecto al rol Administrador (idrol=1). Descomentar si se requiere:
-- INSERT INTO derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
-- SELECT f.idfuncionalidad, 1, 15, 1, f.idmodulo FROM funcionalidad f
--  WHERE f.codigo = 'PRODUCERCOLLECTIONRESTRICTION';

-- ============================================================================
-- FASE 3: tipo de planilla (NORMAL / EXCEDENTE)
-- ============================================================================
-- Discrimina la planilla normal de la de excedentes. Las planillas existentes
-- quedan como NORMAL (default), asi la generacion actual no cambia.
-- Requiere ejecutarse antes de desplegar (hbm2ddl.auto=validate).
ALTER TABLE planillapagomateriaprima
    ADD COLUMN tipoplanilla VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

UPDATE planillapagomateriaprima SET tipoplanilla = 'NORMAL'
 WHERE tipoplanilla IS NULL OR tipoplanilla = '';

-- ============================================================================
-- FASE 4: planilla unica + precios con vigencia
--   Ver docs/acopio/plan-planilla-unica-precios-v1.md
-- ============================================================================

-- 4.1) Tipo de dia de la planilla (HABIL / DOMINGO / NINGUNO) --------------
--   Junto con tipoplanilla (NORMAL/EXCEDENTE) distingue las 4 planillas que
--   genera el proceso unico. Las planillas existentes quedan como NINGUNO.
ALTER TABLE planillapagomateriaprima
    ADD COLUMN tipodia VARCHAR(10) NOT NULL DEFAULT 'NINGUNO';

UPDATE planillapagomateriaprima SET tipodia = 'NINGUNO'
 WHERE tipodia IS NULL OR tipodia = '';

-- 4.2) Configuracion global de precios de acopio de leche (con vigencia) ----
CREATE TABLE IF NOT EXISTS precio_acopio_leche (
    idprecio_acopio_leche   BIGINT        NOT NULL,
    preciohabil             DECIMAL(9,2)  NOT NULL,
    preciodomingo           DECIMAL(9,2)  NOT NULL,
    precioexcedentehabil    DECIMAL(9,2)  NOT NULL,
    precioexcedentedomingo  DECIMAL(9,2)  NOT NULL,
    fechaini                DATE          NOT NULL,
    fechafin                DATE          NOT NULL,
    estado                  VARCHAR(10)   NOT NULL,
    PRIMARY KEY (idprecio_acopio_leche)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Secuencia de la tabla (TableGenerator)
INSERT INTO secuencia (tabla, valor)
    SELECT 'precio_acopio_leche', 0 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'precio_acopio_leche');

-- 4.3) Funcionalidad / permiso del catalogo de precios ----------------------
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. CRUD completo = 15.
--   Se precalcula en variables: el INSERT no lee la tabla destino (evita el
--   error 1093 de MySQL y las subconsultas anidadas). @idmod se toma del modulo
--   de Produccion (RESERVPRODUCERMILK); si no existe, el INSERT no hace nada.
SET @idmod  = (SELECT idmodulo FROM funcionalidad WHERE codigo = 'RESERVPRODUCERMILK' LIMIT 1);
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'MILKPRICECONFIG');

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'MILKPRICECONFIG',
       'Precios de Acopio de Leche (habil/domingo/excedente, con vigencia)',
       @idmod, 15, 'Functionality.production.milkPriceConfig', 1
FROM dual
WHERE @existe = 0 AND @idmod IS NOT NULL;

-- Actualizar secuencia interna de funcionalidad
UPDATE secuencia SET valor = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad)
WHERE tabla = 'funcionalidad';

-- Asignacion por defecto al rol Administrador (idrol=1). Descomentar si se requiere:
-- INSERT INTO derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
-- SELECT f.idfuncionalidad, 1, 15, 1, f.idmodulo FROM funcionalidad f
--  WHERE f.codigo = 'MILKPRICECONFIG';

-- 4.4) Semilla de precios vigente (opcional) --------------------------------
--   Descomentar y ajustar fechas/precios para dejar una config inicial:
-- INSERT INTO precio_acopio_leche
--   (idprecio_acopio_leche, preciohabil, preciodomingo, precioexcedentehabil, precioexcedentedomingo, fechaini, fechafin, estado)
--   VALUES ((SELECT valor+1 FROM secuencia WHERE tabla='precio_acopio_leche'),
--           5.00, 4.50, 4.00, 4.00, '2026-01-01', '2027-12-31', 'ENABLE');
