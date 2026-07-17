-- ============================================================================
-- query_v6.0.110_terdemol.sql
-- ============================================================================
-- CRUD de la tabla `configuracion` (pantalla "Preferencias de compania") y
-- logos de compania administrables desde esa pantalla.
--
-- Contexto:
--   La pantalla /admin/companySetting.xhtml ya existia y edita `configuracion`,
--   pero era INACCESIBLE: su render depende de s:hasPermission('COMPANYSETTING',..)
--   y la funcionalidad 'COMPANYSETTING' nunca se sembro en `funcionalidad`.
--   Sin fila en `funcionalidad` no puede haber fila en `derechoacceso`, el mapa
--   de permisos que arma UserServiceBean al login no tiene la clave, y
--   AppIdentity.hasPermission() devuelve false en silencio. Efecto: ni el link
--   del menu ni el boton Guardar se renderizaban, y la funcionalidad tampoco
--   aparecia en la pantalla de Roles, asi que nadie podia otorgarla.
--
-- Detalle:
--   Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8.
--   La pantalla es de UN solo registro por compania (findByCompany usa
--   getSingleResult) y no tiene alta ni baja: por eso permiso = 5
--   (VIEW=1 + UPDATE=4) y no 15. OJO: 3 seria VIEW+CREATE, no VIEW+UPDATE.
--   idmodulo = 2 (admin), igual que ROLE / USER / BUSINESSUNIT.
--   Columnas de funcionalidad (orden fisico): (idfuncionalidad, codigo,
--       descripcion, idmodulo, permiso, nombrerecurso, idcompania).
--
-- IMPORTANTE (derechoacceso.idmodulo): la named query AccessRight.findByUser
--   hace join contra modulocompania por (idcompania, idmodulo) y filtra por
--   companyModule.active. Si idmodulo va NULL el join no matchea y el permiso
--   se ignora sin error. Por eso el INSERT de abajo setea idmodulo = 2.
-- ----------------------------------------------------------------------------

-- 1) Logos de compania -------------------------------------------------------
--    Se reutiliza la tabla `archivo` (entidad File, BLOB + discriminador `tipo`)
--    que ya usan las fotos de activos fijos, en vez de meter LONGBLOBs en
--    `configuracion`, que se consulta en ~100 lugares.
--    Los logos se reescalan al guardar: cabecera 200x50, login 300x300.
--
--    NOTA: no se crean FKs hacia `archivo` porque `archivo.idarchivo` no tiene
--    PRIMARY KEY ni indice unico en esta base (mismo hueco de integridad que
--    documenta query/fix_integridad_permisos.sql para el modulo de permisos).
--    MySQL rechazaria la FK con error 1822. Si mas adelante se corrige la
--    integridad de `archivo`, agregar aqui las dos FKs.

ALTER TABLE configuracion ADD COLUMN idlogocabecera BIGINT(20) DEFAULT NULL;
ALTER TABLE configuracion ADD COLUMN idlogologin    BIGINT(20) DEFAULT NULL;

-- 2) Funcionalidad del permiso ----------------------------------------------
--    nombrerecurso apunta a la clave i18n ya existente en messages_app.properties
--    ('Functionality.admin.companySetting = Preferencias de compania'), que hasta
--    ahora estaba huerfana.

insert into funcionalidad values (503, 'COMPANYSETTING', 'Preferencias de compania (configuracion)', 2, 5, 'Functionality.admin.companySetting', 1);

-- 3) Otorgar el permiso al rol Administrador (idrol = 1) ---------------------

insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
values (503, 1, 5, 1, 2);
