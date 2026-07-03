-- ============================================================================
-- v6.0.96 :: Personas de contacto de Cliente (contactocliente)
-- ============================================================================
--
--  Se agrega el manejo de PERSONAS DE CONTACTO por Cliente, estilo Odoo pero
--  contenido en el dominio de ventas (no altera la jerarquia global Entity/
--  Person). Un Cliente (personacliente) puede tener 0..N contactos, cada uno
--  con sus canales de contacto a nivel empresarial: telefono, celular,
--  telefono de trabajo, fax, email, web, cargo, departamento y observaciones.
--
--  Cambio ADITIVO: tabla nueva 'contactocliente'. NO se toca ninguna columna
--  de 'personacliente'; el flujo de ventas/facturacion actual queda intacto.
--
--  Costura opcional hacia el modelo unificado: la columna 'idpersona' (NULL por
--  defecto) permite, a futuro, reconciliar el contacto con un Person real de la
--  jerarquia (entidad/persona) sin obligar a nada hoy.
--
--  IMPORTANTE: 'principal' y 'activo' deben ser INT (no TINYINT(1)). El entity
--  las mapea con IntegerBooleanUserType y el validador de Hibernate exige tipo
--  'integer'; TINYINT(1) el driver MySQL lo reporta como 'bit' y el despliegue
--  falla con "Wrong column type ... Found: bit, expected: integer". Mismo
--  criterio que las demas columnas booleanas del sistema.
-- ----------------------------------------------------------------------------

CREATE TABLE contactocliente (
    idcontactocliente BIGINT        NOT NULL,
    idpersonacliente  BIGINT        NOT NULL,          -- cliente dueño (parent)
    idpersona         BIGINT        NULL,              -- costura opcional a persona
    nombres           VARCHAR(200)  NOT NULL,
    apellidos         VARCHAR(200)  NULL,
    cargo             VARCHAR(150)  NULL,
    departamento      VARCHAR(150)  NULL,
    email             VARCHAR(150)  NULL,
    telefono          VARCHAR(30)   NULL,
    celular           VARCHAR(30)   NULL,
    telefonotrabajo   VARCHAR(30)   NULL,
    fax               VARCHAR(30)   NULL,
    web               VARCHAR(200)  NULL,
    direccion         VARCHAR(300)  NULL,
    observaciones     VARCHAR(1000) NULL,
    principal         INT           NOT NULL DEFAULT 0,
    activo            INT           NOT NULL DEFAULT 1,
    PRIMARY KEY (idcontactocliente),
    KEY ix_contactocli_cliente (idpersonacliente),
    KEY ix_contactocli_persona (idpersona),
    CONSTRAINT fk_contactocli_cliente
        FOREIGN KEY (idpersonacliente) REFERENCES personacliente (idpersonacliente),
    CONSTRAINT fk_contactocli_persona
        FOREIGN KEY (idpersona) REFERENCES persona (idpersona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- Semilla de secuencia (GenerationType.TABLE, pkColumnValue = 'contactocliente')
-- El id lo genera Hibernate leyendo/incrementando 'secuencia'. Sembramos la
-- fila en 0 para arrancar. Si ya existiera, no se duplica.
-- ----------------------------------------------------------------------------
INSERT INTO secuencia (tabla, valor)
    SELECT 'contactocliente', 0
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'contactocliente');
