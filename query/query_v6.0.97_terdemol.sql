-- ============================================================================
-- v6.0.97 :: Contacto de Cliente - Area/Ubicacion + precarga de Paises
-- ============================================================================
--
--  Complementa v6.0.96 (tabla contactocliente):
--   1) Renombra 'departamento' -> 'area'. El campo es el AREA ORGANIZACIONAL
--      del contacto dentro de la empresa cliente (Compras, Contabilidad...),
--      NO el departamento geografico. Se renombra para evitar la ambiguedad
--      con la geografia (tabla 'departamento').
--   2) Agrega 'ciudad' (texto libre; el detalle geografico va Pais + Ciudad).
--   3) Agrega 'idpais' (FK a 'pais', reutiliza el catalogo existente). El
--      prefijo telefonico sale de pais.prefijo (no se agrega campo aparte).
--   4) PRECARGA el catalogo 'pais' con la lista internacional + prefijo, para
--      que el dropdown funcione tipo web sin carga manual.
--
--  Los tres cambios en contactocliente son ADITIVOS/rename; no afectan ventas.
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) contactocliente: area / ciudad / pais
-- ----------------------------------------------------------------------------
ALTER TABLE contactocliente
    CHANGE departamento area VARCHAR(150) NULL;

ALTER TABLE contactocliente
    ADD COLUMN empresa VARCHAR(200) NULL AFTER cargo;

ALTER TABLE contactocliente
    ADD COLUMN ciudad VARCHAR(150) NULL AFTER direccion;

ALTER TABLE contactocliente
    ADD COLUMN idpais BIGINT NULL AFTER ciudad;

ALTER TABLE contactocliente
    ADD KEY ix_contactocli_pais (idpais);

ALTER TABLE contactocliente
    ADD CONSTRAINT fk_contactocli_pais
        FOREIGN KEY (idpais) REFERENCES pais (idpais);


-- ----------------------------------------------------------------------------
-- 2) Precarga del catalogo 'pais' (idcompania = 1)
--    - Reutiliza el catalogo existente (mismo que usa Person/Employee).
--    - Los ids se generan a partir del MAX actual; la secuencia queda en max+1
--      (misma convencion que el resto del sistema).
--    - Idempotente: no reinserta un pais ya existente (por nombre + compania).
--    - Ajustar idcompania si la empresa no es 1, o repetir por cada compania.
-- ----------------------------------------------------------------------------
SET @i := (SELECT COALESCE(MAX(idpais), 0) FROM pais);

INSERT INTO pais (idpais, nombre, prefijo, idcompania, version)
SELECT @i := @i + 1, t.nombre, t.prefijo, 1, 0
FROM (
    SELECT 'BOLIVIA' AS nombre, '+591' AS prefijo
    UNION ALL SELECT 'ARGENTINA', '+54'
    UNION ALL SELECT 'BRASIL', '+55'
    UNION ALL SELECT 'CHILE', '+56'
    UNION ALL SELECT 'COLOMBIA', '+57'
    UNION ALL SELECT 'ECUADOR', '+593'
    UNION ALL SELECT 'PARAGUAY', '+595'
    UNION ALL SELECT 'PERU', '+51'
    UNION ALL SELECT 'URUGUAY', '+598'
    UNION ALL SELECT 'VENEZUELA', '+58'
    UNION ALL SELECT 'MEXICO', '+52'
    UNION ALL SELECT 'ESTADOS UNIDOS', '+1'
    UNION ALL SELECT 'CANADA', '+1'
    UNION ALL SELECT 'COSTA RICA', '+506'
    UNION ALL SELECT 'CUBA', '+53'
    UNION ALL SELECT 'EL SALVADOR', '+503'
    UNION ALL SELECT 'GUATEMALA', '+502'
    UNION ALL SELECT 'HONDURAS', '+504'
    UNION ALL SELECT 'NICARAGUA', '+505'
    UNION ALL SELECT 'PANAMA', '+507'
    UNION ALL SELECT 'PUERTO RICO', '+1'
    UNION ALL SELECT 'REPUBLICA DOMINICANA', '+1'
    UNION ALL SELECT 'ESPANA', '+34'
    UNION ALL SELECT 'PORTUGAL', '+351'
    UNION ALL SELECT 'FRANCIA', '+33'
    UNION ALL SELECT 'ITALIA', '+39'
    UNION ALL SELECT 'ALEMANIA', '+49'
    UNION ALL SELECT 'REINO UNIDO', '+44'
    UNION ALL SELECT 'PAISES BAJOS', '+31'
    UNION ALL SELECT 'BELGICA', '+32'
    UNION ALL SELECT 'SUIZA', '+41'
    UNION ALL SELECT 'AUSTRIA', '+43'
    UNION ALL SELECT 'SUECIA', '+46'
    UNION ALL SELECT 'NORUEGA', '+47'
    UNION ALL SELECT 'DINAMARCA', '+45'
    UNION ALL SELECT 'FINLANDIA', '+358'
    UNION ALL SELECT 'IRLANDA', '+353'
    UNION ALL SELECT 'POLONIA', '+48'
    UNION ALL SELECT 'RUSIA', '+7'
    UNION ALL SELECT 'UCRANIA', '+380'
    UNION ALL SELECT 'TURQUIA', '+90'
    UNION ALL SELECT 'GRECIA', '+30'
    UNION ALL SELECT 'CHINA', '+86'
    UNION ALL SELECT 'JAPON', '+81'
    UNION ALL SELECT 'COREA DEL SUR', '+82'
    UNION ALL SELECT 'INDIA', '+91'
    UNION ALL SELECT 'INDONESIA', '+62'
    UNION ALL SELECT 'FILIPINAS', '+63'
    UNION ALL SELECT 'TAILANDIA', '+66'
    UNION ALL SELECT 'VIETNAM', '+84'
    UNION ALL SELECT 'MALASIA', '+60'
    UNION ALL SELECT 'SINGAPUR', '+65'
    UNION ALL SELECT 'ISRAEL', '+972'
    UNION ALL SELECT 'ARABIA SAUDITA', '+966'
    UNION ALL SELECT 'EMIRATOS ARABES UNIDOS', '+971'
    UNION ALL SELECT 'SUDAFRICA', '+27'
    UNION ALL SELECT 'EGIPTO', '+20'
    UNION ALL SELECT 'NIGERIA', '+234'
    UNION ALL SELECT 'MARRUECOS', '+212'
    UNION ALL SELECT 'AUSTRALIA', '+61'
    UNION ALL SELECT 'NUEVA ZELANDA', '+64'
) t
WHERE NOT EXISTS (
    SELECT 1 FROM pais p WHERE p.nombre = t.nombre AND p.idcompania = 1
);

-- Deja la secuencia en el siguiente id libre (convencion del sistema).
UPDATE secuencia
    SET valor = (SELECT COALESCE(MAX(idpais), 0) + 1 FROM pais)
    WHERE tabla = 'pais';
