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
