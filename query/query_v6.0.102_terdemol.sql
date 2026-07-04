-- ============================================================================
-- v6.0.102 :: Ciudad como catalogo (FK a City) en cliente y contacto
-- ============================================================================
--
--  Ciudad deja de ser texto libre y pasa a ser FK al catalogo 'ciudad' (City),
--  que a su vez pertenece a un departamento. En la UI se escribe el nombre y al
--  guardar se hace find-or-create sobre el catalogo bajo el departamento elegido.
--
--  IMPORTANTE (hbm2ddl=validate): ejecutar ANTES del deploy.
-- ----------------------------------------------------------------------------

-- personacliente
ALTER TABLE personacliente DROP COLUMN ciudad;
ALTER TABLE personacliente ADD COLUMN idciudad BIGINT NULL AFTER iddepartamento;
ALTER TABLE personacliente
    ADD CONSTRAINT fk_personacli_ciudad FOREIGN KEY (idciudad) REFERENCES ciudad (idciudad);

-- contactocliente
ALTER TABLE contactocliente DROP COLUMN ciudad;
ALTER TABLE contactocliente ADD COLUMN idciudad BIGINT NULL AFTER iddepartamento;
ALTER TABLE contactocliente ADD KEY ix_contactocli_ciudad (idciudad);
ALTER TABLE contactocliente
    ADD CONSTRAINT fk_contactocli_ciudad FOREIGN KEY (idciudad) REFERENCES ciudad (idciudad);

-- ----------------------------------------------------------------------------
-- Correccion de datos: BOLIVIA (fila preexistente) tenia el prefijo/codigo de
-- area invertidos. Se deja consistente con el resto: prefijo internacional en
-- 'prefijo' y 'codigoarea' en NULL.
-- ----------------------------------------------------------------------------
UPDATE pais SET prefijo = '+591', codigoarea = NULL
WHERE nombre = 'BOLIVIA' AND idcompania = 1;

-- ============================================================================
-- Permisos faltantes: catalogos del menu "Contacto"
-- ============================================================================
--
--  El menu global "Contacto" (view/layout/menu.xhtml) y sus vistas ya usan
--  s:hasPermission(...) sobre estos codigos, pero las funcionalidades no estaban
--  registradas en la tabla 'funcionalidad'. Se agregan aqui.
--
--  permiso = 15 (VIEW=1 + CREATE=2 + UPDATE=4 + DELETE=8): las cinco vistas
--  (list + edit) implementan las cuatro acciones (ver PermissionType).
--
--  idmodulo: DOCUMENTTYPE -> 1 (customers, model.customers.DocumentType);
--            TITLE/SALUTATION/MARITALSTATUS/ORGANIZATION -> 3 (contacts,
--            model.contacts.*).
--
--  nombrerecurso: claves ya existentes en messages_app.properties (no se agrega
--  texto nuevo).
--
--  Orden de columnas: (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
--
--  NOTA: solo se registra la funcionalidad. La asignacion de accesos
--  (derechoacceso) se hace por el panel de permisos del sistema.
-- ----------------------------------------------------------------------------
insert into funcionalidad values (479, 'DOCUMENTTYPE',  'Catalogo de Tipos de Documento', 1, 15, 'menu.customers.configuration.documentType',  1);
insert into funcionalidad values (480, 'TITLE',         'Catalogo de Titulos',            3, 15, 'menu.customers.configuration.title',         1);
insert into funcionalidad values (481, 'SALUTATION',    'Catalogo de Saludos',            3, 15, 'menu.customers.configuration.salutation',    1);
insert into funcionalidad values (482, 'MARITALSTATUS', 'Catalogo de Estados Civiles',    3, 15, 'menu.customers.configuration.maritalStatus', 1);
insert into funcionalidad values (483, 'ORGANIZATION',  'Catalogo de Organizaciones',     3, 15, 'menu.contacts.configuration.organization',   1);
