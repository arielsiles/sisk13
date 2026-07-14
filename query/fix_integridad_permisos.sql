-- =============================================================================
-- Script:      fix_integridad_permisos.sql
-- Objetivo:    Restaurar la integridad referencial del modulo de permisos
--              (modulo / modulocompania / funcionalidad / rol / derechoacceso).
-- Base:        terdemol (MySQL 8.0, InnoDB)
-- Tipo:        Script independiente / de ejecucion unica (no versionado).
--
-- Diagnostico realizado sobre la base real (terdemol):
--   * Las 5 tablas son InnoDB.
--   * Solo 'funcionalidad' tiene PRIMARY KEY; modulo, rol, modulocompania y
--     derechoacceso NO tienen ningun indice -> causa del error 1822 al crear FKs.
--   * Todas las columnas clave son BIGINT (tipos compatibles para FK).
--   * PKs sin NULLs ni duplicados -> se pueden crear directamente.
--   * derechoacceso tiene 160 filas huerfanas que apuntan a roles inexistentes
--     (permisos de roles borrados) -> deben eliminarse antes de crear la FK a rol.
--
-- Relaciones que se van a establecer:
--   modulocompania (idmodulo)               -> modulo (idmodulo)
--   funcionalidad  (idmodulo)               -> modulo (idmodulo)
--   derechoacceso  (idfuncionalidad)        -> funcionalidad (idfuncionalidad)
--   derechoacceso  (idrol)                  -> rol (idrol)
--   derechoacceso  (idcompania, idmodulo)   -> modulocompania (idcompania, idmodulo)
--
-- IMPORTANTE: hacer respaldo antes de ejecutar (ver seccion 0).
-- Ejecutar las secciones en orden. Es seguro re-ejecutar las verificaciones.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 0) RESPALDO RECOMENDADO (ejecutar desde consola, NO dentro de este script)
-- -----------------------------------------------------------------------------
--   mysqldump -u adm -p terdemol modulo modulocompania funcionalidad rol \
--             derechoacceso > backup_permisos.sql


-- -----------------------------------------------------------------------------
-- 1) VERIFICACION PREVIA (solo lectura)  -- opcional, para confirmar estado
-- -----------------------------------------------------------------------------
-- Indices actuales de cada tabla:
--   SHOW KEYS FROM modulo;
--   SHOW KEYS FROM modulocompania;
--   SHOW KEYS FROM funcionalidad;
--   SHOW KEYS FROM rol;
--   SHOW KEYS FROM derechoacceso;

-- Filas de derechoacceso con rol inexistente (deben ser 160 antes de limpiar):
   SELECT da.idrol, COUNT(*)
   FROM derechoacceso da
   LEFT JOIN rol r ON r.idrol = da.idrol
   WHERE r.idrol IS NULL
   GROUP BY da.idrol;


-- -----------------------------------------------------------------------------
-- 2) LIMPIEZA DE HUERFANOS
--    Elimina los permisos que apuntan a roles que ya no existen.
--    (idfuncionalidad e idrol forman la PK -> no pueden quedar en NULL,
--     por eso se borran en lugar de anularse.)
-- -----------------------------------------------------------------------------
DELETE da
FROM derechoacceso da
LEFT JOIN rol r ON r.idrol = da.idrol
WHERE r.idrol IS NULL;
-- Se esperan 160 filas afectadas.


-- -----------------------------------------------------------------------------
-- 3) CREACION DE PRIMARY KEYS
--    'funcionalidad' ya tiene PK, por eso no se incluye.
--    Al agregar la PK, MySQL convierte las columnas a NOT NULL automaticamente
--    (ya se verifico que no hay NULLs ni duplicados).
-- -----------------------------------------------------------------------------
ALTER TABLE modulo          ADD PRIMARY KEY (idmodulo);
ALTER TABLE rol             ADD PRIMARY KEY (idrol);
ALTER TABLE modulocompania  ADD PRIMARY KEY (idcompania, idmodulo);
ALTER TABLE derechoacceso   ADD PRIMARY KEY (idfuncionalidad, idrol);


-- -----------------------------------------------------------------------------
-- 4) CREACION DE LLAVES FORANEAS
--    El orden respeta las dependencias: primero las que apuntan a 'modulo',
--    luego las de 'derechoacceso'. La FK compuesta hacia 'modulocompania'
--    cubre la integridad de idcompania e idmodulo en derechoacceso.
--
--    Sin ON UPDATE/ON DELETE explicitos -> MySQL usa RESTRICT por defecto:
--    impide borrar un padre (modulo/rol/funcionalidad/modulocompania) que
--    todavia tenga registros hijos.
-- -----------------------------------------------------------------------------
ALTER TABLE modulocompania
    ADD CONSTRAINT fk_modulocompania_modulo
    FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo);

ALTER TABLE funcionalidad
    ADD CONSTRAINT fk_funcionalidad_modulo
    FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo);

ALTER TABLE derechoacceso
    ADD CONSTRAINT fk_derechoacceso_funcionalidad
    FOREIGN KEY (idfuncionalidad) REFERENCES funcionalidad (idfuncionalidad);

ALTER TABLE derechoacceso
    ADD CONSTRAINT fk_derechoacceso_rol
    FOREIGN KEY (idrol) REFERENCES rol (idrol);

ALTER TABLE derechoacceso
    ADD CONSTRAINT fk_derechoacceso_modulocompania
    FOREIGN KEY (idcompania, idmodulo) REFERENCES modulocompania (idcompania, idmodulo);


-- -----------------------------------------------------------------------------
-- 5) VERIFICACION FINAL (solo lectura)
-- -----------------------------------------------------------------------------
-- Listar las FKs creadas:
--   SELECT constraint_name, table_name, referenced_table_name
--   FROM information_schema.referential_constraints
--   WHERE constraint_schema = 'terdemol';
-- =============================================================================
