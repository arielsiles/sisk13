-- ============================================================================
-- query_v6.0.123_terdemol.sql
-- ============================================================================
-- Flags booleanos de arcgms (S/N) que quedaron en NULL en las cuentas cargadas
-- antes de que existieran. Idempotente: solo toca las filas en NULL.

UPDATE arcgms SET ind_mov       = 'N' WHERE ind_mov       IS NULL;
UPDATE arcgms SET ind_regulariz = 'N' WHERE ind_regulariz IS NULL;
UPDATE arcgms SET ind_presup    = 'N' WHERE ind_presup    IS NULL;
UPDATE arcgms SET util          = 'N' WHERE util          IS NULL;
UPDATE arcgms SET permite_iva   = 'N' WHERE permite_iva   IS NULL;
UPDATE arcgms SET permiso_con   = 'N' WHERE permiso_con   IS NULL;
UPDATE arcgms SET permiso_che   = 'N' WHERE permiso_che   IS NULL;
UPDATE arcgms SET permiso_cxp   = 'N' WHERE permiso_cxp   IS NULL;
UPDATE arcgms SET permiso_afijo = 'N' WHERE permiso_afijo IS NULL;
UPDATE arcgms SET permiso_inv   = 'N' WHERE permiso_inv   IS NULL;
UPDATE arcgms SET permiso_cxc   = 'N' WHERE permiso_cxc   IS NULL;
UPDATE arcgms SET exije_cc      = 'N' WHERE exije_cc      IS NULL;

-- activa va en 'S' y no en 'N': es el unico flag cuyo default es verdadero (una
-- cuenta existente esta activa). En caisc no hay ninguna en NULL; esto es por si
-- otra base las tiene.
UPDATE arcgms SET activa = 'S' WHERE activa IS NULL;


-- ============================================================================
-- REVERTIR ACOPIO DE MATERIA PRIMA (Aprobado/Contabilizado -> Pendiente).
-- Solo prepara la BD; el codigo viene aparte. Aplicarlo antes es inofensivo.

-- Auditoria (mismos nombres que sf_tmpenc). Las filas existentes quedan en NULL.
ALTER TABLE acopiomp
    ADD COLUMN created_at DATETIME NULL AFTER version,
    ADD COLUMN created_by VARCHAR(100) CHARACTER SET utf8mb4 NULL AFTER created_at,
    ADD COLUMN updated_at DATETIME NULL AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) CHARACTER SET utf8mb4 NULL AFTER updated_at;

-- id_tmpenc: asiento que contabilizo este acopio (sin FK: sf_tmpenc puede estar en otro schema).
-- nro_reversiones y ultima_reversion_*: copia de la ultima reversion para pintar el listado sin joins.
ALTER TABLE acopiomp
    ADD COLUMN id_tmpenc BIGINT NULL AFTER updated_by,
    ADD COLUMN nro_reversiones INT NOT NULL DEFAULT 0 AFTER id_tmpenc,
    ADD COLUMN ultima_reversion_at DATETIME NULL AFTER nro_reversiones,
    ADD COLUMN ultima_reversion_by VARCHAR(100) CHARACTER SET utf8mb4 NULL AFTER ultima_reversion_at,
    ADD COLUMN ultima_reversion_motivo VARCHAR(500) CHARACTER SET utf8mb4 NULL AFTER ultima_reversion_by,
    ADD KEY ix_acopiomp_tmpenc (id_tmpenc);

-- Bitacora de reversiones: una fila por reversion, no se borra ni se edita.
CREATE TABLE acopiomp_reversion (
    idacopiomp_reversion BIGINT NOT NULL AUTO_INCREMENT,
    idacopiomp           BIGINT NOT NULL,
    fecha_hora           DATETIME NOT NULL,
    usuario              VARCHAR(100) NOT NULL,
    motivo               VARCHAR(500) NOT NULL,
    estado_anterior      VARCHAR(25) NOT NULL,
    id_tmpenc            BIGINT NULL,
    saldo_antes          DECIMAL(12,2) NULL,
    saldo_despues        DECIMAL(12,2) NULL,
    costo_antes          DECIMAL(16,6) NULL,
    costo_despues        DECIMAL(16,6) NULL,
    version              BIGINT DEFAULT 0,
    idcompania           BIGINT NOT NULL,
    PRIMARY KEY (idacopiomp_reversion),
    KEY ix_acopiomp_reversion_acopio (idacopiomp),
    CONSTRAINT fk_acopiomp_reversion_acopio FOREIGN KEY (idacopiomp) REFERENCES acopiomp (idacopiomp),
    CONSTRAINT fk_acopiomp_reversion_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ventana de reversion en dias (0 = sin limite). Cambiar con UPDATE, sin tocar codigo.
ALTER TABLE configuracion ADD COLUMN acopio_dias_reversion INT NOT NULL DEFAULT 30;

-- Permiso de accion (permiso=1: un solo check en Roles). No se asigna a ningun rol aqui.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'COLLECTMATERIALREVERT', 'Revertir acopio de materia prima', 6, 1, 'Functionality.production.collectMaterialRevert', 1
  FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'COLLECTMATERIALREVERT');
UPDATE secuencia SET valor = (SELECT MAX(e.idfuncionalidad) + 1 FROM funcionalidad e) WHERE tabla = 'funcionalidad';

-- ----------------------------------------------------------------------------
-- BACKFILL del vinculo. Unico rastro: la glosa lleva ", <codigo>, Boleta:".
-- Solo enlaza sin ambiguedad: codigo no repetido en acopiomp Y un unico asiento IMP
-- vigente (o, si no hay vigente, el ultimo anulado). El resto queda NULL = NO revertible,
-- incluido lo contabilizado con el esquema viejo 'IA' (un asiento por dia), que no se toca.
DROP TEMPORARY TABLE IF EXISTS tmp_cod_dup;
CREATE TEMPORARY TABLE tmp_cod_dup (codigo VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL, PRIMARY KEY (codigo));
INSERT INTO tmp_cod_dup SELECT codigo FROM acopiomp WHERE codigo IS NOT NULL GROUP BY codigo HAVING COUNT(*) > 1;

-- Un unico asiento vigente (terdemol: 733).
UPDATE acopiomp a
   SET a.id_tmpenc = (SELECT v.id_tmpenc FROM sf_tmpenc v WHERE v.tipo_doc='IMP' AND v.estado<>'ANL' AND v.glosa LIKE CONCAT('%, ',a.codigo,', Boleta:%'))
 WHERE a.estado='CONTA' AND a.id_tmpenc IS NULL AND a.codigo IS NOT NULL
   AND a.codigo NOT IN (SELECT codigo FROM tmp_cod_dup)
   AND (SELECT COUNT(*) FROM sf_tmpenc v WHERE v.tipo_doc='IMP' AND v.estado<>'ANL' AND v.glosa LIKE CONCAT('%, ',a.codigo,', Boleta:%')) = 1;

-- Sin vigente pero con anulado: se enlaza el ultimo (terdemol: 0 hoy).
UPDATE acopiomp a
   SET a.id_tmpenc = (SELECT MAX(v.id_tmpenc) FROM sf_tmpenc v WHERE v.tipo_doc='IMP' AND v.estado='ANL' AND v.glosa LIKE CONCAT('%, ',a.codigo,', Boleta:%'))
 WHERE a.estado='CONTA' AND a.id_tmpenc IS NULL AND a.codigo IS NOT NULL
   AND a.codigo NOT IN (SELECT codigo FROM tmp_cod_dup)
   AND (SELECT COUNT(*) FROM sf_tmpenc v WHERE v.tipo_doc='IMP' AND v.estado<>'ANL' AND v.glosa LIKE CONCAT('%, ',a.codigo,', Boleta:%')) = 0
   AND (SELECT COUNT(*) FROM sf_tmpenc v WHERE v.tipo_doc='IMP' AND v.estado='ANL'  AND v.glosa LIKE CONCAT('%, ',a.codigo,', Boleta:%')) >= 1;

DROP TEMPORARY TABLE IF EXISTS tmp_cod_dup;

-- Verificacion: cobertura (esperado ~733 enlazados / ~246 sin vinculo).
SELECT IF(id_tmpenc IS NULL,'SIN VINCULO (no revertible)','ENLAZADO') vinculo, COUNT(*) acopios, MIN(fecha) desde, MAX(fecha) hasta
  FROM acopiomp WHERE estado='CONTA' GROUP BY vinculo;

-- Verificacion: ningun asiento reclamado por dos acopios. Debe dar 0 filas.
SELECT id_tmpenc, COUNT(*) acopios FROM acopiomp WHERE id_tmpenc IS NOT NULL GROUP BY id_tmpenc HAVING COUNT(*) > 1;

-- Verificacion: todo vinculo apunta a un IMP existente. Debe dar 0 filas.
SELECT a.idacopiomp, a.codigo, a.id_tmpenc FROM acopiomp a
  LEFT JOIN sf_tmpenc v ON v.id_tmpenc = a.id_tmpenc
 WHERE a.id_tmpenc IS NOT NULL AND (v.id_tmpenc IS NULL OR v.tipo_doc <> 'IMP');
-- ============================================================================
