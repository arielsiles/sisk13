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
