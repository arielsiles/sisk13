-- ============================================================================
-- query_v6.0.107_terdemol.sql
-- ============================================================================
-- Permisos propios para el submenu "Procesos" de Produccion (XProduccion).
--
-- Problema corregido:
--   Los items del submenu Produccion > Procesos ("Distribucion de costos
--   indirectos" y "Cierre de Ctas - Costo de Produccion") no tenian ninguna
--   funcionalidad/permiso asociada; se mostraban a cualquier usuario con acceso
--   a la pestaña XProduccion (XPRODUCTION/VIEW), sin poder ocultarse por rol.
--
-- Solucion:
--   Se registran dos funcionalidades propias (una por opcion). El menu ahora
--   las gatea con s:hasPermission(...,'VIEW') y el dropdown "Procesos" solo
--   aparece si el rol tiene al menos una de las dos.
--
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. CRUD completo = 15.
--   idmodulo = 11 (xproduction, alineado con XPRODUCTION_BALANCE / reportes).
--   Estas funcionalidades son de tipo proceso: solo requieren VIEW(1).
--   Columnas (orden fisico): (idfuncionalidad, codigo, descripcion,
--                             idmodulo, permiso, nombrerecurso, idcompania).
--
-- NOTA: solo se registra la funcionalidad. La asignacion de accesos
--       (derechoacceso) se hace desde el panel de permisos del sistema.
-- ----------------------------------------------------------------------------

insert into funcionalidad
values (492, 'XPRODUCTION_DISTRIBUTIONINDIRECTCOST', 'Distribucion de costos indirectos',
        11, 1, 'Functionality.xproduction.process.distributionIndirectCost', 1);

insert into funcionalidad
values (493, 'XPRODUCTION_CLOSINGPRODUCTIONCOST', 'Cierre de Ctas - Costo de Produccion',
        11, 1, 'Functionality.xproduction.process.closingProductionCost', 1);

-- Asignacion por defecto al rol Administrador (idrol=1). Descomentar si se requiere:
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (492, 1, 1, 1, 11);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (493, 1, 1, 1, 11);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';
