-- ============================================================================
-- v6.0.99 :: Cliente y Contacto - ubicacion (pais/departamento/ciudad),
--            datos empresariales en cliente, y precarga de departamentos
-- ============================================================================
--
--  IMPORTANTE: hibernate.hbm2ddl.auto = validate. Estas columnas DEBEN existir
--  antes de desplegar el nuevo codigo. Ejecutar este script ANTES del deploy.
--
--  1) personacliente: + celular, empresa, web, idpais, ciudad y FK a pais.
--     iddepartamento ya podria existir (columna heredada); se agrega solo si
--     falta y se le pone la FK a departamento.
--  2) contactocliente: + iddepartamento (FK) y se ELIMINA la columna fax.
--  3) Precarga de departamentos/estados: Bolivia (completo) + paises
--     principales (regiones mas usadas). Ampliable desde el catalogo.
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) personacliente
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente ADD COLUMN celular VARCHAR(30)  NULL AFTER telefono;
ALTER TABLE personacliente ADD COLUMN empresa VARCHAR(200) NULL AFTER email;
ALTER TABLE personacliente ADD COLUMN web     VARCHAR(200) NULL AFTER empresa;
ALTER TABLE personacliente ADD COLUMN idpais  BIGINT       NULL AFTER web;
ALTER TABLE personacliente ADD COLUMN ciudad  VARCHAR(150) NULL AFTER idpais;

-- iddepartamento: agregar SOLO si no existe (en algunos esquemas ya viene).
SET @s := (SELECT IF(COUNT(*) = 0,
        'ALTER TABLE personacliente ADD COLUMN iddepartamento BIGINT NULL',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'personacliente'
      AND COLUMN_NAME = 'iddepartamento');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

ALTER TABLE personacliente
    ADD CONSTRAINT fk_personacli_pais        FOREIGN KEY (idpais)         REFERENCES pais (idpais);
ALTER TABLE personacliente
    ADD CONSTRAINT fk_personacli_departamento FOREIGN KEY (iddepartamento) REFERENCES departamento (iddepartamento);


-- ----------------------------------------------------------------------------
-- 2) contactocliente
-- ----------------------------------------------------------------------------
ALTER TABLE contactocliente ADD COLUMN iddepartamento BIGINT NULL AFTER idpais;
ALTER TABLE contactocliente ADD KEY ix_contactocli_departamento (iddepartamento);
ALTER TABLE contactocliente
    ADD CONSTRAINT fk_contactocli_departamento FOREIGN KEY (iddepartamento) REFERENCES departamento (iddepartamento);

ALTER TABLE contactocliente DROP COLUMN fax;


-- ----------------------------------------------------------------------------
-- 3) Precarga de departamentos/estados (idcompania = 1)
--    Solo se insertan los de paises ya precargados (JOIN a pais). Idempotente.
-- ----------------------------------------------------------------------------
SET @d := (SELECT COALESCE(MAX(iddepartamento), 0) FROM departamento);

INSERT INTO departamento (iddepartamento, nombre, idpais, idcompania, version)
SELECT @d := @d + 1, x.dep, p.idpais, 1, 0
FROM (
    -- Bolivia (completo)
    SELECT 'BOLIVIA' AS pais, 'LA PAZ' AS dep
    UNION ALL SELECT 'BOLIVIA', 'SANTA CRUZ'
    UNION ALL SELECT 'BOLIVIA', 'COCHABAMBA'
    UNION ALL SELECT 'BOLIVIA', 'ORURO'
    UNION ALL SELECT 'BOLIVIA', 'POTOSI'
    UNION ALL SELECT 'BOLIVIA', 'CHUQUISACA'
    UNION ALL SELECT 'BOLIVIA', 'TARIJA'
    UNION ALL SELECT 'BOLIVIA', 'BENI'
    UNION ALL SELECT 'BOLIVIA', 'PANDO'
    -- Argentina
    UNION ALL SELECT 'ARGENTINA', 'BUENOS AIRES'
    UNION ALL SELECT 'ARGENTINA', 'CIUDAD AUTONOMA DE BUENOS AIRES'
    UNION ALL SELECT 'ARGENTINA', 'CORDOBA'
    UNION ALL SELECT 'ARGENTINA', 'SANTA FE'
    UNION ALL SELECT 'ARGENTINA', 'MENDOZA'
    UNION ALL SELECT 'ARGENTINA', 'TUCUMAN'
    UNION ALL SELECT 'ARGENTINA', 'SALTA'
    UNION ALL SELECT 'ARGENTINA', 'ENTRE RIOS'
    -- Brasil
    UNION ALL SELECT 'BRASIL', 'SAO PAULO'
    UNION ALL SELECT 'BRASIL', 'RIO DE JANEIRO'
    UNION ALL SELECT 'BRASIL', 'MINAS GERAIS'
    UNION ALL SELECT 'BRASIL', 'BAHIA'
    UNION ALL SELECT 'BRASIL', 'PARANA'
    UNION ALL SELECT 'BRASIL', 'RIO GRANDE DO SUL'
    UNION ALL SELECT 'BRASIL', 'SANTA CATARINA'
    -- Chile
    UNION ALL SELECT 'CHILE', 'METROPOLITANA DE SANTIAGO'
    UNION ALL SELECT 'CHILE', 'VALPARAISO'
    UNION ALL SELECT 'CHILE', 'BIOBIO'
    UNION ALL SELECT 'CHILE', 'MAULE'
    UNION ALL SELECT 'CHILE', 'ANTOFAGASTA'
    -- Peru
    UNION ALL SELECT 'PERU', 'LIMA'
    UNION ALL SELECT 'PERU', 'AREQUIPA'
    UNION ALL SELECT 'PERU', 'CUSCO'
    UNION ALL SELECT 'PERU', 'LA LIBERTAD'
    UNION ALL SELECT 'PERU', 'PIURA'
    -- Colombia
    UNION ALL SELECT 'COLOMBIA', 'BOGOTA D.C.'
    UNION ALL SELECT 'COLOMBIA', 'ANTIOQUIA'
    UNION ALL SELECT 'COLOMBIA', 'VALLE DEL CAUCA'
    UNION ALL SELECT 'COLOMBIA', 'CUNDINAMARCA'
    UNION ALL SELECT 'COLOMBIA', 'ATLANTICO'
    -- Mexico
    UNION ALL SELECT 'MEXICO', 'CIUDAD DE MEXICO'
    UNION ALL SELECT 'MEXICO', 'JALISCO'
    UNION ALL SELECT 'MEXICO', 'NUEVO LEON'
    UNION ALL SELECT 'MEXICO', 'ESTADO DE MEXICO'
    UNION ALL SELECT 'MEXICO', 'PUEBLA'
    -- Estados Unidos
    UNION ALL SELECT 'ESTADOS UNIDOS', 'CALIFORNIA'
    UNION ALL SELECT 'ESTADOS UNIDOS', 'TEXAS'
    UNION ALL SELECT 'ESTADOS UNIDOS', 'FLORIDA'
    UNION ALL SELECT 'ESTADOS UNIDOS', 'NUEVA YORK'
    UNION ALL SELECT 'ESTADOS UNIDOS', 'ILLINOIS'
    -- Espana
    UNION ALL SELECT 'ESPANA', 'MADRID'
    UNION ALL SELECT 'ESPANA', 'CATALUNA'
    UNION ALL SELECT 'ESPANA', 'ANDALUCIA'
    UNION ALL SELECT 'ESPANA', 'COMUNIDAD VALENCIANA'
    UNION ALL SELECT 'ESPANA', 'PAIS VASCO'
    -- Paraguay
    UNION ALL SELECT 'PARAGUAY', 'CENTRAL'
    UNION ALL SELECT 'PARAGUAY', 'ASUNCION'
    UNION ALL SELECT 'PARAGUAY', 'ALTO PARANA'
    -- Uruguay
    UNION ALL SELECT 'URUGUAY', 'MONTEVIDEO'
    UNION ALL SELECT 'URUGUAY', 'CANELONES'
    -- Ecuador
    UNION ALL SELECT 'ECUADOR', 'PICHINCHA'
    UNION ALL SELECT 'ECUADOR', 'GUAYAS'
    UNION ALL SELECT 'ECUADOR', 'AZUAY'
) x
JOIN pais p ON p.nombre = x.pais AND p.idcompania = 1
WHERE NOT EXISTS (
    SELECT 1 FROM departamento d
    WHERE d.nombre = x.dep AND d.idpais = p.idpais AND d.idcompania = 1
);

UPDATE secuencia
    SET valor = (SELECT COALESCE(MAX(iddepartamento), 0) + 1 FROM departamento)
    WHERE tabla = 'departamento';
