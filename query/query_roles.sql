-- ============================================================================
-- query_roles.sql :: Consultas de auditoria de usuarios, roles y permisos
-- ============================================================================
--
-- Consultas de SOLO LECTURA para revisar la seguridad del sistema:
--   1) Usuarios y los roles que tienen asignados.
--   2) Roles, sus funcionalidades y las opciones de permiso (otorgadas /
--      disponibles).
--
-- Esquema involucrado:
--   usuario(idusuario, usuario, email, numerousuario, idcompania)
--   rol(idrol, nombre, idcompania)
--   usuariorol(idusuario, idrol)                      -- N a N usuario<->rol
--   funcionalidad(idfuncionalidad, codigo, descripcion,
--                 idmodulo, permiso, nombrerecurso)
--   modulo(idmodulo, nombrerecurso, idcompania)
--   derechoacceso(idfuncionalidad, idrol, permiso,    -- permiso por rol+func
--                 idcompania, idmodulo)
--
-- Bitmask de la columna 'permiso' (funcionalidad.permiso = opciones disponibles;
-- derechoacceso.permiso = opciones otorgadas al rol):
--   VER = 1 , CREAR = 2 , ACTUALIZAR = 4 , ELIMINAR = 8
--   ej.: 13 = VER+ACTUALIZAR+ELIMINAR ; 15 = VER+CREAR+ACTUALIZAR+ELIMINAR
--
-- NOTA: 'modulo.nombrerecurso' y 'funcionalidad.nombrerecurso' guardan la CLAVE
-- de traduccion (ej. 'menu.xproduction.production.labData'), no el texto final;
-- el texto legible lo resuelve la aplicacion con resources/messages_app.properties.
--
-- NOTA: el filtro por 'idcompania' esta comentado; descomentar y ajustar el id
-- si la base de datos es multi-compania (evita mezclar/duplicar filas).
-- ----------------------------------------------------------------------------


-- ============================================================================
-- 1) USUARIOS Y SUS ROLES
-- ============================================================================

-- 1a) Una fila por usuario-rol (LEFT JOIN: incluye usuarios sin rol asignado)
SELECT u.idusuario,
       u.usuario           AS usuario,
       u.numerousuario     AS nro,
       u.email,
       r.idrol,
       r.nombre            AS rol
FROM usuario u
LEFT JOIN usuariorol ur ON ur.idusuario = u.idusuario
LEFT JOIN rol r         ON r.idrol       = ur.idrol
-- WHERE u.idcompania = 1
where u.usuario = 'admin'
ORDER BY u.usuario, r.nombre;


-- 1b) Compacta: roles concatenados en una sola fila por usuario
SELECT u.idusuario,
       u.usuario,
       u.email,
       GROUP_CONCAT(r.nombre ORDER BY r.nombre SEPARATOR ', ') AS roles
FROM usuario u
LEFT JOIN usuariorol ur ON ur.idusuario = u.idusuario
LEFT JOIN rol r         ON r.idrol       = ur.idrol
GROUP BY u.idusuario, u.usuario, u.email
ORDER BY u.usuario;


-- 1c) Permisos EFECTIVOS por usuario y funcionalidad (formato como 2b).
--     Un usuario puede tener varios roles: el permiso efectivo es la UNION de
--     los bits de todos sus roles -> BIT_OR(da.permiso). Solo lista las
--     funcionalidades que el usuario tiene otorgadas (por al menos un rol).
SELECT u.usuario                                                   AS usuario,
       f.codigo                                                    AS funcionalidad,
       f.descripcion                                               AS descripcion,
       BIT_OR(da.permiso)                                          AS permiso_otorgado,
       CASE WHEN (BIT_OR(da.permiso) & 1) = 1 THEN 'X' ELSE '' END AS ver,
       CASE WHEN (BIT_OR(da.permiso) & 2) = 2 THEN 'X' ELSE '' END AS crear,
       CASE WHEN (BIT_OR(da.permiso) & 4) = 4 THEN 'X' ELSE '' END AS actualizar,
       CASE WHEN (BIT_OR(da.permiso) & 8) = 8 THEN 'X' ELSE '' END AS eliminar
FROM usuario u
JOIN usuariorol ur    ON ur.idusuario      = u.idusuario
JOIN derechoacceso da ON da.idrol          = ur.idrol
JOIN funcionalidad f  ON f.idfuncionalidad = da.idfuncionalidad
-- WHERE u.idcompania = 1
-- AND u.usuario = 'NOMBRE_USUARIO'      -- opcional: un solo usuario
GROUP BY u.idusuario, u.usuario, f.idfuncionalidad, f.codigo, f.descripcion
ORDER BY u.usuario, f.codigo;


-- ============================================================================
-- 2) ROLES, SUS FUNCIONALIDADES Y LAS OPCIONES DE PERMISO
-- ============================================================================

-- 2a) Funcionalidades OTORGADAS a cada rol, con el bitmask decodificado y las
--     opciones disponibles de la funcionalidad.
SELECT r.nombre                                       AS rol,
       m.nombrerecurso                                AS modulo,
       f.codigo                                       AS funcionalidad,
       f.descripcion                                  AS descripcion,
       da.permiso                                     AS permiso_otorgado,
       CASE WHEN (da.permiso & 1) = 1 THEN 'X' ELSE '' END AS ver,
       CASE WHEN (da.permiso & 2) = 2 THEN 'X' ELSE '' END AS crear,
       CASE WHEN (da.permiso & 4) = 4 THEN 'X' ELSE '' END AS actualizar,
       CASE WHEN (da.permiso & 8) = 8 THEN 'X' ELSE '' END AS eliminar,
       f.permiso                                      AS opciones_disponibles
FROM rol r
JOIN derechoacceso da ON da.idrol          = r.idrol
JOIN funcionalidad f  ON f.idfuncionalidad = da.idfuncionalidad
LEFT JOIN modulo m    ON m.idmodulo   = f.idmodulo
                     AND m.idcompania = da.idcompania     -- evita duplicados multi-compania
-- WHERE r.idcompania = 1
ORDER BY r.nombre, m.nombrerecurso, f.codigo;


-- 2b) TODAS las funcionalidades por rol, incluyendo las NO otorgadas
--     (util para ver que falta asignar). permiso_otorgado = 0 => sin acceso.
SELECT r.nombre                                                     AS rol,
       f.codigo                                                     AS funcionalidad,
       f.descripcion                                                AS descripcion,
       COALESCE(da.permiso, 0)                                      AS permiso_otorgado,
       CASE WHEN (COALESCE(da.permiso,0) & 1) = 1 THEN 'X' ELSE '' END AS ver,
       CASE WHEN (COALESCE(da.permiso,0) & 2) = 2 THEN 'X' ELSE '' END AS crear,
       CASE WHEN (COALESCE(da.permiso,0) & 4) = 4 THEN 'X' ELSE '' END AS actualizar,
       CASE WHEN (COALESCE(da.permiso,0) & 8) = 8 THEN 'X' ELSE '' END AS eliminar
FROM rol r
CROSS JOIN funcionalidad f
LEFT JOIN derechoacceso da ON da.idrol = r.idrol
                          AND da.idfuncionalidad = f.idfuncionalidad
-- WHERE r.idcompania = 1
ORDER BY r.nombre, f.codigo;


-- ============================================================================
-- 3) PINPOINT: en que ROL(es) un usuario tiene otorgada una funcionalidad
-- ============================================================================
--
-- Responde "que rol me da este permiso?". Util cuando la 1c muestra que el
-- usuario tiene una funcionalidad pero hay que ubicar el rol exacto que la
-- otorga (un usuario puede tener varios roles).
--
-- Parametros: ajustar los dos valores antes de ejecutar.
SET @usuario       = 'admin';
SET @funcionalidad = 'PRODUCTION_LAB_DATA';

SELECT u.usuario,
       r.idrol,
       r.nombre        AS rol,
       f.codigo        AS funcionalidad,
       da.permiso      AS permiso_otorgado,
       CASE WHEN (da.permiso & 1) = 1 THEN 'X' ELSE '' END AS ver,
       CASE WHEN (da.permiso & 2) = 2 THEN 'X' ELSE '' END AS crear,
       CASE WHEN (da.permiso & 4) = 4 THEN 'X' ELSE '' END AS actualizar,
       CASE WHEN (da.permiso & 8) = 8 THEN 'X' ELSE '' END AS eliminar
FROM usuario u
JOIN usuariorol ur    ON ur.idusuario      = u.idusuario
JOIN rol r            ON r.idrol           = ur.idrol
JOIN derechoacceso da ON da.idrol          = ur.idrol
JOIN funcionalidad f  ON f.idfuncionalidad = da.idfuncionalidad
WHERE u.usuario = @usuario
  AND f.codigo  = @funcionalidad
-- AND u.idcompania = 1
ORDER BY r.nombre;
