-- ============================================================================
-- Actualizacion de acopio (excedente) — OLGA ZAMBRANA OTALORA  (CI 5920932)
-- Mayo y Junio 2026
-- ============================================================================
-- Regla: por cada dia de la imagen,  cantidad = cantidad + excedente(imagen).
--        Suma el excedente diario (litros sobre el cupo de 80 L/dia) al acopio
--        ya registrado en la tabla acopiomateriaprima.
--
-- Solo se listan los dias con excedente > 0 (los dias en 0 no cambian).
--
-- ⚠️  EJECUTAR UNA SOLA VEZ EN PRODUCCION.
--     Es una suma RELATIVA (cantidad = cantidad + X). Correrlo dos veces DUPLICA
--     el excedente. No hay forma de detectar si ya se aplico.
--
-- ⚠️  ENTORNO DEV (khipus / localhost): la PARTE 1 (MAYO) YA fue aplicada alli.
--     NO volver a ejecutar mayo en dev. Este script es para PRODUCCION, donde el
--     acopio de Olga esta SIN el excedente.
--
-- Verificacion de origen: en dev, (cantidad_actual - 80) coincide exactamente con
-- la imagen de mayo dia por dia; y el preview de junio da (80 + excedente) por dia,
-- con exactamente 1 fila por fecha (sin duplicados).
-- ============================================================================


-- --------------------------------------------------------------------------
-- (Opcional) Verificacion PREVIA: 1 fila por fecha y cantidad actual.
-- --------------------------------------------------------------------------
-- SELECT s.fecha, COUNT(*) AS filas, SUM(a.cantidad) AS cantidad_actual
-- FROM acopiomateriaprima a
--   JOIN sesionacopio s ON s.idsesionacopio = a.idsesionacopio
--   JOIN entidad e       ON e.identidad     = a.idproductormateriaprima
-- WHERE e.noidentificacion = '5920932'
--   AND s.fecha BETWEEN '2026-05-16' AND '2026-06-30'
-- GROUP BY s.fecha
-- ORDER BY s.fecha;
-- (Cada fecha debe dar filas = 1. Si alguna da > 1, revisar antes de actualizar.)


-- ==========================================================================
-- PARTE 1 — MAYO 2026
-- ==========================================================================
UPDATE acopiomateriaprima a
  JOIN sesionacopio s ON s.idsesionacopio = a.idsesionacopio
  JOIN entidad e      ON e.identidad      = a.idproductormateriaprima
  JOIN (
    SELECT '2026-05-19' AS fecha,  20 AS exc UNION ALL
    SELECT '2026-05-20',  20 UNION ALL
    SELECT '2026-05-21', 280 UNION ALL
    SELECT '2026-05-22', 214 UNION ALL
    SELECT '2026-05-23', 210 UNION ALL
    SELECT '2026-05-25', 190 UNION ALL
    SELECT '2026-05-26', 204 UNION ALL
    SELECT '2026-05-27', 210 UNION ALL
    SELECT '2026-05-29', 220
  ) x ON x.fecha = s.fecha
SET a.cantidad = a.cantidad + x.exc
WHERE e.noidentificacion = '5920932';


-- ==========================================================================
-- PARTE 2 — JUNIO 2026
-- ==========================================================================
UPDATE acopiomateriaprima a
  JOIN sesionacopio s ON s.idsesionacopio = a.idsesionacopio
  JOIN entidad e      ON e.identidad      = a.idproductormateriaprima
  JOIN (
    SELECT '2026-06-02' AS fecha, 220 AS exc UNION ALL
    SELECT '2026-06-06', 177 UNION ALL
    SELECT '2026-06-08', 220 UNION ALL
    SELECT '2026-06-09', 208 UNION ALL
    SELECT '2026-06-10', 212 UNION ALL
    SELECT '2026-06-11', 210 UNION ALL
    SELECT '2026-06-12', 138 UNION ALL
    SELECT '2026-06-13', 275 UNION ALL
    SELECT '2026-06-14', 265 UNION ALL
    SELECT '2026-06-15', 276 UNION ALL
    SELECT '2026-06-16', 240 UNION ALL
    SELECT '2026-06-17', 274 UNION ALL
    SELECT '2026-06-18', 270 UNION ALL
    SELECT '2026-06-19', 264 UNION ALL
    SELECT '2026-06-20', 258 UNION ALL
    SELECT '2026-06-21', 259 UNION ALL
    SELECT '2026-06-22', 267 UNION ALL
    SELECT '2026-06-23', 257 UNION ALL
    SELECT '2026-06-24', 245 UNION ALL
    SELECT '2026-06-25', 275 UNION ALL
    SELECT '2026-06-26', 186 UNION ALL
    SELECT '2026-06-27', 282 UNION ALL
    SELECT '2026-06-28', 200 UNION ALL
    SELECT '2026-06-29', 146 UNION ALL
    SELECT '2026-06-30', 177
  ) x ON x.fecha = s.fecha
SET a.cantidad = a.cantidad + x.exc
WHERE e.noidentificacion = '5920932';


-- --------------------------------------------------------------------------
-- (Opcional) Verificacion POSTERIOR: cantidad resultante por dia.
-- --------------------------------------------------------------------------
 SELECT s.fecha, a.cantidad
 FROM acopiomateriaprima a
   JOIN sesionacopio s ON s.idsesionacopio = a.idsesionacopio
   JOIN entidad e       ON e.identidad     = a.idproductormateriaprima
 WHERE e.noidentificacion = '5920932'
   AND s.fecha BETWEEN '2026-05-16' AND '2026-06-30'
 ORDER BY s.fecha;
