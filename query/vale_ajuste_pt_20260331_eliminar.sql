-- ============================================================================
-- ELIMINACION COMPLETA de los DOS vales de ajuste creados por
--   query/vale_ajuste_pt_20260331_crear.sql
--
-- Los ubica por sus MARCAS (no_vale 'AJ-PT-S-20260331' y 'AJ-PT-E-20260331')
-- y borra sus filas en:  inv_movdet -> inv_mov -> inv_vales.
--
-- Uso: correr esto ANTES de re-ejecutar el script de creacion (por si hay que
--   corregir cantidades y volver a generar los vales).
--
-- NOTA sobre secuencias: NO se retroceden (_sequence('VALE') / secuencia
--   ('inv_movdet')). No hace falta: el script de creacion siempre toma el
--   siguiente numero disponible. Retroceder secuencias seria riesgoso si se
--   crearon otros vales/movimientos entre medio.
--
-- Motor: MySQL. Ejecutar en el schema de PRODUCCION (terdemol).
-- ============================================================================

START TRANSACTION;

SET @no_vale_s := 'AJ-PT-S-20260331';
SET @no_vale_e := 'AJ-PT-E-20260331';

-- no_cia + no_trans de cada marca (NULL si no existe)
SET @cia      := (SELECT no_cia   FROM inv_vales WHERE no_vale IN (@no_vale_s, @no_vale_e) LIMIT 1);
SET @no_trans_s := (SELECT no_trans FROM inv_vales WHERE no_vale = @no_vale_s LIMIT 1);
SET @no_trans_e := (SELECT no_trans FROM inv_vales WHERE no_vale = @no_vale_e LIMIT 1);

-- Que se va a borrar (revisar antes de confirmar):
SELECT @cia AS no_cia, @no_trans_s AS no_trans_salida, @no_trans_e AS no_trans_entrada;
SELECT 'inv_movdet' AS tabla, COUNT(*) AS filas FROM inv_movdet WHERE no_cia = @cia AND no_trans IN (@no_trans_s, @no_trans_e)
UNION ALL
SELECT 'inv_mov',   COUNT(*) FROM inv_mov   WHERE no_cia = @cia AND no_trans IN (@no_trans_s, @no_trans_e)
UNION ALL
SELECT 'inv_vales', COUNT(*) FROM inv_vales WHERE no_cia = @cia AND no_trans IN (@no_trans_s, @no_trans_e);

-- Borrado (hijo -> padre):
DELETE FROM inv_movdet WHERE no_cia = @cia AND no_trans IN (@no_trans_s, @no_trans_e);
DELETE FROM inv_mov    WHERE no_cia = @cia AND no_trans IN (@no_trans_s, @no_trans_e);
DELETE FROM inv_vales  WHERE no_cia = @cia AND no_trans IN (@no_trans_s, @no_trans_e);

-- Si los conteos y los no_trans eran los esperados:
COMMIT;
-- Si algo esta mal:  ROLLBACK;
-- ============================================================================


-- ============================================================================
-- Elimina el vale de ajuste REC (BENTONITA PROCESADA, 24/03/2026) por su marca.
START TRANSACTION;
SET @no_vale := 'AJ-PT-E-20260324';
SET @cia      := (SELECT no_cia  FROM inv_vales WHERE no_vale=@no_vale LIMIT 1);
SET @no_trans := (SELECT no_trans FROM inv_vales WHERE no_vale=@no_vale LIMIT 1);
SELECT @cia AS no_cia, @no_trans AS no_trans;
DELETE FROM inv_movdet WHERE no_cia=@cia AND no_trans=@no_trans;
DELETE FROM inv_mov    WHERE no_cia=@cia AND no_trans=@no_trans;
DELETE FROM inv_vales  WHERE no_cia=@cia AND no_trans=@no_trans;
COMMIT;
-- ROLLBACK si algo esta mal.
-- ============================================================================
