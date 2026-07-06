-- ============================================================================
-- ELIMINACION COMPLETA del vale de ajuste creado por
--   query/vale_ajuste_mp_20260331_crear.sql
--
-- Lo ubica por su MARCA (no_vale = 'AJ-MP-20260331') y borra las 3 filas:
--   inv_movdet -> inv_mov -> inv_vales.
--
-- Uso: correr esto ANTES de re-ejecutar el script de creacion (por si hay que
--   corregir cantidades y volver a generar el vale).
--
-- NOTA sobre secuencias: NO se retroceden (_sequence('VALE') / secuencia
--   ('inv_movdet')). No hace falta: el script de creacion siempre toma el
--   siguiente numero disponible. Retroceder secuencias seria riesgoso si se
--   crearon otros vales/movimientos entre medio.
--
-- Motor: MySQL. Ejecutar en el schema de PRODUCCION (terdemol).
-- ============================================================================

START TRANSACTION;

SET @no_vale  := 'AJ-MP-20260331';
SET @no_trans := (SELECT no_trans FROM inv_vales WHERE no_vale = @no_vale LIMIT 1);
SET @cia      := (SELECT no_cia  FROM inv_vales WHERE no_vale = @no_vale LIMIT 1);

-- Que se va a borrar (revisar antes de confirmar):
SELECT @cia AS no_cia, @no_trans AS no_trans;
SELECT 'inv_movdet' AS tabla, COUNT(*) AS filas FROM inv_movdet WHERE no_trans = @no_trans AND no_cia = @cia
UNION ALL
SELECT 'inv_mov',   COUNT(*) FROM inv_mov   WHERE no_trans = @no_trans AND no_cia = @cia
UNION ALL
SELECT 'inv_vales', COUNT(*) FROM inv_vales WHERE no_trans = @no_trans AND no_cia = @cia;

-- Borrado (hijo -> padre):
DELETE FROM inv_movdet WHERE no_cia = @cia AND no_trans = @no_trans;
DELETE FROM inv_mov    WHERE no_cia = @cia AND no_trans = @no_trans;
DELETE FROM inv_vales  WHERE no_cia = @cia AND no_trans = @no_trans;

-- Si los conteos y el no_trans eran los esperados:
COMMIT;
-- Si algo esta mal:  ROLLBACK;
-- ============================================================================
