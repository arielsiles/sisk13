-- ============================================================================
-- v6.0.92 :: ULEXITA - (A) Articulo del Reproceso final = 2096 (ULEXITA PROCESADA),
--             (B) costo de los articulos PROCESADOS = el de su materia prima, y
--             (C) en las ordenes, cambiar el insumo a su articulo PROCESADO con
--                 ese costo y recalcular el costeo.
-- ============================================================================
--
-- CONTEXTO / CRITERIO (acordado)
--   1) En xpr_produccion_ulexita.cod_art_reproc_final debe guardarse el articulo
--      del Reproceso final: 2096 = ULEXITA PROCESADA (hoy esta en NULL). El campo
--      ya esta implementado; solo falta cargar el codigo.
--   2) Los articulos procesados destino tienen costo_uni = 0. Se les asigna el
--      MISMO costo de su materia prima de origen:
--          1573 (BENTONITA PROCESADA) := costo_uni de 7   (BENTONITA)
--          2097 (CAOLIN PROCESADO)     := costo_uni de 2037 (CAOLIN)
--   3) En las ordenes ULEXITA, los insumos quedaron con el articulo de MP cruda
--      en vez del procesado (cantidades correctas). Se reemplaza:
--          insumo 7    -> 1573  con costouni = costo de 7
--          insumo 2037 -> 2097  con costouni = costo de 2037
--      y se recalcula costototal / totalmp / costeo de PT.
--
-- ALCANCE
--   * (A) y (B): se ejecutan una vez.
--   * (C) swap de articulo: todas las ordenes ULEXITA no anuladas (APR + PEN).
--     Recalculo de costototal y PT: SOLO ordenes APR (las congeladas); las PEN
--     recalculan solas al aprobarse.
--
-- IDEMPOTENTE / REVERSIBLE: respaldos bkp_v6092_*; recalculos deterministas.
-- NOTA: el insumo conserva su idinsumoformula (apunta a la MP cruda en la
--   formula). El costeo usa xpr_insumo.cod_art/costouni, no la formula, asi que
--   no afecta. Para que las ordenes NUEVAS usen el procesado, hay que corregir la
--   formula maestra (fuera de este script).
-- ----------------------------------------------------------------------------

-- Costos de origen (se capturan al inicio para reusarlos).
SELECT costo_uni INTO @c_bent FROM inv_articulos WHERE cod_art = '7'    LIMIT 1;
SELECT costo_uni INTO @c_caol FROM inv_articulos WHERE cod_art = '2037' LIMIT 1;

-- ----------------------------------------------------------------------------
-- 1) Costo de los articulos PROCESADOS = el de su materia prima.
-- ----------------------------------------------------------------------------
UPDATE inv_articulos SET costo_uni = @c_bent WHERE cod_art = '1573';   -- BENTONITA PROCESADA
UPDATE inv_articulos SET costo_uni = @c_caol WHERE cod_art = '2097';   -- CAOLIN PROCESADO

-- ----------------------------------------------------------------------------
-- 2) Reproceso final: cod_art_reproc_final = 2096 en las ordenes ULEXITA.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6092_ulexita (
    idproduccion             BIGINT     NOT NULL,
    cod_art_reproc_final_old VARCHAR(20) NULL,
    backed_at                TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproduccion)
) ENGINE=InnoDB;

INSERT INTO bkp_v6092_ulexita (idproduccion, cod_art_reproc_final_old)
SELECT u.idproduccion, u.cod_art_reproc_final
FROM xpr_produccion_ulexita u
JOIN xpr_produccion p ON p.idproduccion = u.idproduccion
JOIN xpr_linea l ON l.idlinea = p.idlinea
WHERE l.report_template_code = 'ULEXITA' AND p.estado <> 'ANL'
  AND NOT EXISTS (SELECT 1 FROM bkp_v6092_ulexita b WHERE b.idproduccion = u.idproduccion);

UPDATE xpr_produccion_ulexita u
JOIN xpr_produccion p ON p.idproduccion = u.idproduccion
JOIN xpr_linea l ON l.idlinea = p.idlinea
SET u.cod_art_reproc_final = '2096'
WHERE l.report_template_code = 'ULEXITA' AND p.estado <> 'ANL'
  AND COALESCE(u.cod_art_reproc_final, '') <> '2096';

-- ----------------------------------------------------------------------------
-- 3) RESPALDOS para el swap de insumos y el recalculo.
-- ----------------------------------------------------------------------------
-- 3.a Insumos a cambiar (cod_art 7 / 2037 en ordenes ULEXITA no anuladas).
CREATE TABLE IF NOT EXISTS bkp_v6092_insumo (
    idinsumo     BIGINT        NOT NULL,
    idproduccion BIGINT        NULL,
    estado_orden VARCHAR(3)    NULL,
    cod_art_old  VARCHAR(6)    NULL,
    cod_art_new  VARCHAR(6)    NULL,
    costouni_old DECIMAL(16,6) NULL,
    costouni_new DECIMAL(16,6) NULL,
    backed_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idinsumo)
) ENGINE=InnoDB;

INSERT INTO bkp_v6092_insumo (idinsumo, idproduccion, estado_orden, cod_art_old, cod_art_new, costouni_old, costouni_new)
SELECT i.idinsumo, i.idproduccion, p.estado, i.cod_art,
       CASE i.cod_art WHEN '7' THEN '1573' WHEN '2037' THEN '2097' END,
       i.costouni,
       CASE i.cod_art WHEN '7' THEN @c_bent WHEN '2037' THEN @c_caol END
FROM xpr_insumo i
JOIN xpr_produccion p ON p.idproduccion = i.idproduccion
JOIN xpr_linea l ON l.idlinea = p.idlinea
WHERE l.report_template_code = 'ULEXITA' AND p.estado <> 'ANL'
  AND i.cod_art IN ('7', '2037')
  AND NOT EXISTS (SELECT 1 FROM bkp_v6092_insumo b WHERE b.idinsumo = i.idinsumo);

-- 3.b Ordenes APR afectadas (las que recalculan costeo).
CREATE TABLE IF NOT EXISTS bkp_v6092_orden (
    idproduccion   BIGINT        NOT NULL,
    costototal_old DECIMAL(16,2) NULL,
    totalmp_old    DECIMAL(16,2) NULL,
    backed_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproduccion)
) ENGINE=InnoDB;

INSERT INTO bkp_v6092_orden (idproduccion, costototal_old, totalmp_old)
SELECT p.idproduccion, p.costototal, p.totalmp
FROM xpr_produccion p
JOIN xpr_linea l ON l.idlinea = p.idlinea
WHERE l.report_template_code = 'ULEXITA' AND p.estado = 'APR'
  AND EXISTS (SELECT 1 FROM xpr_insumo i WHERE i.idproduccion = p.idproduccion AND i.cod_art IN ('7','2037'))
  AND NOT EXISTS (SELECT 1 FROM bkp_v6092_orden b WHERE b.idproduccion = p.idproduccion);

-- 3.c Productos terminados de esas ordenes APR.
CREATE TABLE IF NOT EXISTS bkp_v6092_producto (
    idproducto   BIGINT        NOT NULL,
    idproduccion BIGINT        NULL,
    cantidad     DECIMAL(14,4) NULL,
    costo_old    DECIMAL(16,2) NULL,
    costouni_old DECIMAL(16,2) NULL,
    costo_a      DECIMAL(16,2) NULL,
    costo_b_old  DECIMAL(16,2) NULL,
    costo_c      DECIMAL(16,2) NULL,
    backed_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproducto)
) ENGINE=InnoDB;

INSERT INTO bkp_v6092_producto (idproducto, idproduccion, cantidad, costo_old, costouni_old, costo_a, costo_b_old, costo_c)
SELECT pr.idproducto, pr.idproduccion, pr.cantidad, pr.costo, pr.costouni, pr.costo_a, pr.costo_b, pr.costo_c
FROM xpr_producto pr
JOIN bkp_v6092_orden b ON b.idproduccion = pr.idproduccion
WHERE NOT EXISTS (SELECT 1 FROM bkp_v6092_producto x WHERE x.idproducto = pr.idproducto);

-- ----------------------------------------------------------------------------
-- 4) SWAP de insumo: cod_art a su procesado + costouni = costo de la MP origen.
--    Dos UPDATE separados (constantes) para no depender del orden de asignacion.
-- ----------------------------------------------------------------------------
UPDATE xpr_insumo i
JOIN xpr_produccion p ON p.idproduccion = i.idproduccion
JOIN xpr_linea l ON l.idlinea = p.idlinea
SET i.cod_art = '1573', i.costouni = @c_bent
WHERE l.report_template_code = 'ULEXITA' AND p.estado <> 'ANL' AND i.cod_art = '7';

UPDATE xpr_insumo i
JOIN xpr_produccion p ON p.idproduccion = i.idproduccion
JOIN xpr_linea l ON l.idlinea = p.idlinea
SET i.cod_art = '2097', i.costouni = @c_caol
WHERE l.report_template_code = 'ULEXITA' AND p.estado <> 'ANL' AND i.cod_art = '2037';

-- ----------------------------------------------------------------------------
-- 5) Recalcular COSTO TOTAL / totalmp (solo ordenes APR afectadas).
-- ----------------------------------------------------------------------------
UPDATE xpr_produccion p
JOIN bkp_v6092_orden b ON b.idproduccion = p.idproduccion
SET
    p.costototal = (
        SELECT ROUND(SUM(ROUND(i.cantidad * i.costouni, 6)), 2)
        FROM xpr_insumo i WHERE i.idproduccion = p.idproduccion),
    p.totalmp = (
        SELECT ROUND(SUM(i.cantidad), 2)
        FROM xpr_insumo i
        JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula AND f.defecto = 1
        WHERE i.idproduccion = p.idproduccion);

-- ----------------------------------------------------------------------------
-- 6) Recalcular COSTO B, COSTO y COSTO UNITARIO de cada PT (ordenes APR afectadas).
-- ----------------------------------------------------------------------------
UPDATE xpr_producto pr
JOIN bkp_v6092_orden b ON b.idproduccion = pr.idproduccion
JOIN ( SELECT idproduccion, rc FROM (
           SELECT i.idproduccion AS idproduccion,
                  ROUND(SUM(ROUND(i.cantidad * i.costouni, 6)), 2) AS rc
           FROM xpr_insumo i WHERE i.idproducto IS NULL
           GROUP BY i.idproduccion
       ) zr ) rc ON rc.idproduccion = pr.idproduccion
JOIN ( SELECT idproduccion, tv FROM (
           SELECT pr2.idproduccion AS idproduccion,
                  SUM(ROUND(pr2.cantidad *
                       (SELECT a2.cant_pr FROM inv_articulos a2 WHERE a2.cod_art = pr2.cod_art LIMIT 1), 2)) AS tv
           FROM xpr_producto pr2 GROUP BY pr2.idproduccion
       ) zv ) tv ON tv.idproduccion = pr.idproduccion
SET pr.costo_b = ROUND(
        rc.rc * ROUND(
                  ROUND(
                    ROUND(
                      ROUND(pr.cantidad *
                        (SELECT a.cant_pr FROM inv_articulos a WHERE a.cod_art = pr.cod_art LIMIT 1), 2)
                      * 100, 2)
                    / tv.tv, 2)
                  / 100, 6),
        2)
WHERE pr.cantidad > 0 AND tv.tv > 0;

UPDATE xpr_producto pr
JOIN bkp_v6092_orden b ON b.idproduccion = pr.idproduccion
SET pr.costo    = ROUND(ROUND(pr.costo_a + pr.costo_b, 2) + pr.costo_c, 2),
    pr.costouni = ROUND(ROUND(ROUND(pr.costo_a + pr.costo_b, 2) + pr.costo_c, 2) / pr.cantidad, 2)
WHERE pr.cantidad > 0;

-- ----------------------------------------------------------------------------
-- 7) VERIFICACION (opcional).
-- ----------------------------------------------------------------------------
-- a) Costo de articulos procesados:
-- SELECT cod_art, descri, costo_uni FROM inv_articulos WHERE cod_art IN ('1573','2097');
-- b) Reproceso final cargado (deberia ser 2096):
-- SELECT u.cod_art_reproc_final, COUNT(*) FROM xpr_produccion_ulexita u
--   JOIN xpr_produccion p ON p.idproduccion=u.idproduccion JOIN xpr_linea l ON l.idlinea=p.idlinea
--   WHERE l.report_template_code='ULEXITA' AND p.estado<>'ANL' GROUP BY u.cod_art_reproc_final;
-- c) Insumos ya cambiados (no debe quedar 7 ni 2037 en ordenes ULEXITA):
-- SELECT i.cod_art, COUNT(*) FROM xpr_insumo i JOIN xpr_produccion p ON p.idproduccion=i.idproduccion
--   JOIN xpr_linea l ON l.idlinea=p.idlinea
--   WHERE l.report_template_code='ULEXITA' AND p.estado<>'ANL' AND i.cod_art IN ('7','2037') GROUP BY i.cod_art;
-- d) Costo Total = suma de insumos en APR (0 filas):
-- SELECT p.idproduccion FROM xpr_produccion p JOIN bkp_v6092_orden b ON b.idproduccion=p.idproduccion
--   WHERE ABS(p.costototal - ROUND((SELECT SUM(ROUND(i.cantidad*i.costouni,6)) FROM xpr_insumo i WHERE i.idproduccion=p.idproduccion),2)) > 0.01;

-- ----------------------------------------------------------------------------
-- 8) LIMPIEZA (opcional, tras validar):
-- DROP TABLE IF EXISTS bkp_v6092_producto;
-- DROP TABLE IF EXISTS bkp_v6092_orden;
-- DROP TABLE IF EXISTS bkp_v6092_insumo;
-- DROP TABLE IF EXISTS bkp_v6092_ulexita;
-- ============================================================================


-- ============================================================================
-- v6.0.92 (cont.) :: Quitar insumos con cantidad CERO de TODAS las ordenes.
-- ============================================================================
--   En todas las ordenes de produccion (cualquier linea y estado), elimina las
--   filas de xpr_insumo cuya cantidad sea 0 o NULL. Ejemplos:
--   1376-LIGNOSULFONATO DE SODIO, 1256-AGUA X M3, etc.
--
--   Es COSTO-NEUTRO: esas filas aportan 0 al costototal y al reparto de costo de
--   los PT, por lo que NO requiere recalcular nada.
--
--   IMPORTANTE: se PRESERVA la materia prima por defecto (insumo inputDefault,
--   xpr_insumoformula.defecto=1) aunque este en 0. Hay 35 ordenes ULEXITA APR y 1
--   BARITINA PEN cuya MP quedo en 0 (sin consumo/lab data); borrar su MP romperia
--   la orden. Esas se revisan aparte. Solo se quitan los insumos NO-default en 0
--   (lignosulfonato, agua, etc.).
--
--   Respaldo completo (estructura identica) en bkp_v6092_insumo_cero por si hay
--   que restaurar. Idempotente: al re-correr no queda ningun no-default en 0.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6092_insumo_cero LIKE xpr_insumo;

INSERT INTO bkp_v6092_insumo_cero
SELECT i.* FROM xpr_insumo i
LEFT JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula
WHERE (i.cantidad IS NULL OR i.cantidad = 0)
  AND COALESCE(f.defecto, 0) <> 1                    -- preservar MP inputDefault
  AND NOT EXISTS (SELECT 1 FROM bkp_v6092_insumo_cero b WHERE b.idinsumo = i.idinsumo);

DELETE i FROM xpr_insumo i
LEFT JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula
WHERE (i.cantidad IS NULL OR i.cantidad = 0)
  AND COALESCE(f.defecto, 0) <> 1;                   -- preservar MP inputDefault

-- Verificacion (debe dar 0 = no quedan no-default en cero):
-- SELECT COUNT(*) FROM xpr_insumo i LEFT JOIN xpr_insumoformula f ON f.idinsumoformula=i.idinsumoformula
--   WHERE (i.cantidad IS NULL OR i.cantidad=0) AND COALESCE(f.defecto,0)<>1;
-- MP inputDefault en 0 que quedan (revisar aparte): debe listar las 35 APR + 1 PEN:
-- SELECT i.idproduccion, i.cod_art, p.estado FROM xpr_insumo i
--   JOIN xpr_insumoformula f ON f.idinsumoformula=i.idinsumoformula AND f.defecto=1
--   JOIN xpr_produccion p ON p.idproduccion=i.idproduccion
--   WHERE (i.cantidad IS NULL OR i.cantidad=0);
-- Restaurar (si hiciera falta): INSERT INTO xpr_insumo SELECT * FROM bkp_v6092_insumo_cero;
-- Limpieza: DROP TABLE IF EXISTS bkp_v6092_insumo_cero;
-- ============================================================================
