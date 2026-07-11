-- =============================================================================
-- query_v6.0.48.sql
-- Permisos por boton del proceso de PLANILLA DE ACOPIO (uno por boton).
--
-- Modelo de permisos (ver AppIdentity.hasPermission):
--   hasPermission('CODIGO','ACCION') -> la funcionalidad CODIGO debe existir y
--   estar asignada al rol; ACCION es un bit del bitmask permiso:
--     VIEW=1, CREATE=2, UPDATE=4, DELETE=8 (CRUD completo = 15).
--
-- Cada boton tiene su propia funcionalidad para poder asignarse por separado:
--   RAWMATERIALPAYROLL          -> Generar planillas (acceso, lista)      CREATE
--   RAWMATERIALPAYROLLGENERATE  -> Generar planilla                       CREATE
--   RAWMATERIALPAYROLLAPPROVE   -> Aprobar planilla                       UPDATE
--   RAWMATERIALPAYROLLACCOUNT   -> Contabilizar planilla                  UPDATE
--   RAWMATERIALPAYROLLREVERT    -> Revertir planilla                      UPDATE
--   RAWMATERIALPAYROLLDELETE    -> Borrar planillas                       DELETE
--
-- Se siembran con permiso=15 (la funcionalidad admite las 4 acciones); el rol
-- decide que bit se otorga. Patron sin leer la tabla destino en el INSERT
-- (variables precalculadas) para evitar el error 1093 de MySQL. Idempotente.
-- El modulo (idmodulo) se toma de una funcionalidad de Produccion existente.
-- =============================================================================

SET @idmod = (SELECT idmodulo FROM funcionalidad WHERE codigo = 'RESERVPRODUCERMILK' LIMIT 1);

-- 1) RAWMATERIALPAYROLL ------------------------------------------------------
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'RAWMATERIALPAYROLL');
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'RAWMATERIALPAYROLL',
       'Acopio - Generar planillas (acceso a la lista/pantalla)',
       @idmod, 15, 'Functionality.production.rawMaterialPayRoll', 1
FROM dual WHERE @existe = 0 AND @idmod IS NOT NULL;

-- 2) RAWMATERIALPAYROLLGENERATE ---------------------------------------------
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'RAWMATERIALPAYROLLGENERATE');
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'RAWMATERIALPAYROLLGENERATE',
       'Acopio - Generar planilla de pago',
       @idmod, 15, 'Functionality.production.rawMaterialPayRoll.generate', 1
FROM dual WHERE @existe = 0 AND @idmod IS NOT NULL;

-- 3) RAWMATERIALPAYROLLAPPROVE ----------------------------------------------
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'RAWMATERIALPAYROLLAPPROVE');
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'RAWMATERIALPAYROLLAPPROVE',
       'Acopio - Aprobar planilla',
       @idmod, 15, 'Functionality.production.rawMaterialPayRoll.approve', 1
FROM dual WHERE @existe = 0 AND @idmod IS NOT NULL;

-- 4) RAWMATERIALPAYROLLACCOUNT ----------------------------------------------
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'RAWMATERIALPAYROLLACCOUNT');
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'RAWMATERIALPAYROLLACCOUNT',
       'Acopio - Contabilizar planilla',
       @idmod, 15, 'Functionality.production.rawMaterialPayRoll.account', 1
FROM dual WHERE @existe = 0 AND @idmod IS NOT NULL;

-- 5) RAWMATERIALPAYROLLREVERT -----------------------------------------------
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'RAWMATERIALPAYROLLREVERT');
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'RAWMATERIALPAYROLLREVERT',
       'Acopio - Revertir planilla',
       @idmod, 15, 'Functionality.production.rawMaterialPayRoll.revert', 1
FROM dual WHERE @existe = 0 AND @idmod IS NOT NULL;

-- 6) RAWMATERIALPAYROLLDELETE -----------------------------------------------
SET @newid  = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
SET @existe = (SELECT COUNT(*) FROM funcionalidad WHERE codigo = 'RAWMATERIALPAYROLLDELETE');
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @newid, 'RAWMATERIALPAYROLLDELETE',
       'Acopio - Borrar planillas',
       @idmod, 15, 'Functionality.production.rawMaterialPayRoll.delete', 1
FROM dual WHERE @existe = 0 AND @idmod IS NOT NULL;

-- Actualizar secuencia interna de funcionalidad ------------------------------
UPDATE secuencia SET valor = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad)
WHERE tabla = 'funcionalidad';

-- =============================================================================
-- Asignacion por defecto al rol Administrador (idrol=1), para no bloquear al
-- admin tras el deploy. Idempotente (no duplica si ya tiene el derecho).
-- Si NO se quiere asignar automaticamente, comentar este bloque y otorgar los
-- permisos desde la pantalla de Roles/Permisos.
-- =============================================================================
INSERT INTO derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
SELECT f.idfuncionalidad, 1, 15, 1, f.idmodulo
FROM funcionalidad f
WHERE f.codigo IN ('RAWMATERIALPAYROLL','RAWMATERIALPAYROLLGENERATE','RAWMATERIALPAYROLLAPPROVE',
                   'RAWMATERIALPAYROLLACCOUNT','RAWMATERIALPAYROLLREVERT','RAWMATERIALPAYROLLDELETE')
  AND NOT EXISTS (
        SELECT 1 FROM (SELECT idfuncionalidad, idrol, idcompania FROM derechoacceso) d
        WHERE d.idfuncionalidad = f.idfuncionalidad AND d.idrol = 1 AND d.idcompania = 1);
