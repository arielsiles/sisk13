-- ============================================================================
-- v6.0.100 :: personacliente - normalizar TODOS los nombres de columna a minuscula
-- ============================================================================
--
--  Renombra a minuscula todas las columnas de personacliente que estaban en
--  mayuscula, INCLUYENDO la PK (idpersonacliente) y la columna con FK propia
--  (iddepartamento). Es cosmetico (MySQL es case-insensitive en identificadores)
--  pero deja el esquema consistente con el mapeo del entity.
--
--  Este script fue PROBADO y VERIFICADO en el entorno local (mismo esquema que
--  produccion). Para la PK se sueltan y recrean las 6 FKs de tablas hijas que
--  la referencian; para iddepartamento se suelta y recrea su FK.
--
--  >>> HACER BACKUP ANTES:  CREATE TABLE personacliente_bak LIKE personacliente;
--                           INSERT INTO personacliente_bak SELECT * FROM personacliente;
--  Ejecutar el archivo COMPLETO (mismo orden).
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) Columnas simples (sin PK/FK)
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente
    CHANGE `AM` `am` varchar(255) NULL,
    CHANGE `AP` `ap` varchar(255) NULL,
    CHANGE `CEM_COD` `cem_cod` varchar(255) NULL,
    CHANGE `CODMETODOPAGOSIN` `codmetodopagosin` int NULL,
    CHANGE `COMP` `comp` varchar(10) NULL,
    CHANGE `DESCUENTO` `descuento` decimal(10,2) NULL,
    CHANGE `DESCUENTOPROD` `descuentoprod` decimal(10,2) NULL,
    CHANGE `DIRECCION` `direccion` varchar(100) NULL,
    CHANGE `ESPERSONA` `espersona` int NULL,
    CHANGE `EST_CIVIL` `est_civil` varchar(255) NULL,
    CHANGE `FECHA_NAC` `fecha_nac` date NULL,
    CHANGE `IDRETENCION` `idretencion` bigint NULL,
    CHANGE `IDTERRITORIOTRABAJO` `idterritoriotrabajo` bigint NULL,
    CHANGE `IDTIPOCLIENTE` `idtipocliente` bigint NULL,
    CHANGE `NIT` `nit` varchar(20) NULL,
    CHANGE `NOM` `nom` varchar(255) NULL,
    CHANGE `NRO_DOC` `nro_doc` varchar(255) NULL,
    CHANGE `OBSERVACION` `observacion` varchar(100) NULL,
    CHANGE `OCU_COD` `ocu_cod` varchar(255) NULL,
    CHANGE `PORCENTAJECOMISION` `porcentajecomision` double NULL,
    CHANGE `PORCENTAJEGARANTIA` `porcentajegarantia` double NULL,
    CHANGE `RAZONSOCIAL` `razonsocial` varchar(100) NULL,
    CHANGE `SEXO` `sexo` varchar(255) NULL,
    CHANGE `SIS_COD` `sis_cod` varchar(255) NULL,
    CHANGE `TDO_COD` `tdo_cod` varchar(255) NULL,
    CHANGE `TELEFONO` `telefono` int NULL;


-- ----------------------------------------------------------------------------
-- 2) iddepartamento (tiene FK propia)
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente DROP FOREIGN KEY fk_personacli_departamento;
ALTER TABLE personacliente CHANGE `IDDEPARTAMENTO` `iddepartamento` bigint NULL;
ALTER TABLE personacliente
    ADD CONSTRAINT fk_personacli_departamento FOREIGN KEY (iddepartamento) REFERENCES departamento(iddepartamento);


-- ----------------------------------------------------------------------------
-- 3) idpersonacliente (PK referenciada por 6 tablas hijas)
-- ----------------------------------------------------------------------------
ALTER TABLE contactocliente DROP FOREIGN KEY fk_contactocli_cliente;
ALTER TABLE pago            DROP FOREIGN KEY pago_persona_FK;
ALTER TABLE pedidos         DROP FOREIGN KEY pedidos_ibfk_1;
ALTER TABLE sf_tmpdet       DROP FOREIGN KEY sf_tmpdet_ibfk_1;
ALTER TABLE ventacliente    DROP FOREIGN KEY VentaCliente_CLIENTE_FK;
ALTER TABLE ventadirecta    DROP FOREIGN KEY fk_ventdir_cliente;

ALTER TABLE personacliente CHANGE `IDPERSONACLIENTE` `idpersonacliente` bigint NOT NULL;

ALTER TABLE contactocliente ADD CONSTRAINT fk_contactocli_cliente   FOREIGN KEY (idpersonacliente) REFERENCES personacliente(idpersonacliente);
ALTER TABLE pago            ADD CONSTRAINT pago_persona_FK          FOREIGN KEY (IDPERSONACLIENTE) REFERENCES personacliente(idpersonacliente);
ALTER TABLE pedidos         ADD CONSTRAINT pedidos_ibfk_1          FOREIGN KEY (IDCLIENTE)        REFERENCES personacliente(idpersonacliente);
ALTER TABLE sf_tmpdet       ADD CONSTRAINT sf_tmpdet_ibfk_1        FOREIGN KEY (idpersonacliente) REFERENCES personacliente(idpersonacliente);
ALTER TABLE ventacliente    ADD CONSTRAINT VentaCliente_CLIENTE_FK FOREIGN KEY (IDCLIENTE)        REFERENCES personacliente(idpersonacliente);
ALTER TABLE ventadirecta    ADD CONSTRAINT fk_ventdir_cliente      FOREIGN KEY (IDCLIENTE)        REFERENCES personacliente(idpersonacliente);
