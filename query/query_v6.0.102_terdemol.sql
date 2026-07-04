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
