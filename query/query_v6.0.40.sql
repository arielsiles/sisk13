-- ======================================================================================================
-- query_v6.0.40.sql
-- Restriccion FK: descuentproductmateriaprima y movimientosalarioproductor hacia productormateriaprima
-- ======================================================================================================

-- DIAGNOSTICO: Registros huerfanos (ejecutar antes para verificar)
-- descuentproductmateriaprima: 194 huerfanos (idproductormateriaprima 166=110, 354=84)
-- movimientosalarioproductor:  7 huerfanos   (idproductormateriaprima 166=4, 746=1, 739=1, 745=1)
-- revisar query_v6.0.40_backup.sql

-- =====================================================================================
-- ELIMINACION: Registros huerfanos
-- =====================================================================================

-- Eliminar 194 registros huerfanos de registropagomateriaprima
-- (primero, porque tienen FK hacia descuentproductmateriaprima)
DELETE FROM registropagomateriaprima
WHERE iddescuentproductmateriaprima IN (
    SELECT d.iddescuentproductmateriaprima
    FROM descuentproductmateriaprima d
    WHERE d.idproductormateriaprima NOT IN (SELECT idproductormateriaprima FROM productormateriaprima)
);
--
-- Eliminar 194 registros huerfanos de descuentproductmateriaprima
DELETE FROM descuentproductmateriaprima
WHERE idproductormateriaprima NOT IN (SELECT idproductormateriaprima FROM productormateriaprima);

-- Eliminar 7 registros huerfanos de movimientosalarioproductor
DELETE FROM movimientosalarioproductor
WHERE idproductormateriaprima IS NOT NULL
  AND idproductormateriaprima NOT IN (SELECT idproductormateriaprima FROM productormateriaprima);

-- =====================================================================================
-- FK CONSTRAINTS: Crear restricciones de llave foranea
-- =====================================================================================
ALTER TABLE descuentproductmateriaprima
    ADD CONSTRAINT fk_descuentproductmp_productormateriaprima
        FOREIGN KEY (idproductormateriaprima) REFERENCES productormateriaprima (idproductormateriaprima);

ALTER TABLE movimientosalarioproductor
    ADD CONSTRAINT fk_movsalarioproductor_productormateriaprima
        FOREIGN KEY (idproductormateriaprima) REFERENCES productormateriaprima (idproductormateriaprima);
