-- ============================================================================
-- v6.0.114 :: Asientos contables - secuencias (id, no_trans, no_doc) por JPA
-- ============================================================================
--
--  Ejecutar JUSTO ANTES del deploy del nuevo codigo, con el sistema DETENIDO.
--
--  Migra la generacion de correlativos de asientos de las funciones almacenadas
--  a Hibernate. Las funciones NO se eliminan: quedan como respaldo.
--
--  IMPORTANTE: setear @company_id con la compania de esta base (la del login,
--  currentCompany.id) tomandola de la tabla 'compania'. Puede ser cualquier id
--  (1, 8, 10, ...). Todo el script usa esta variable; no hay valores fijos.
-- ----------------------------------------------------------------------------

SET @company_id := 1;   -- <<< AJUSTAR al id real de la compania (ver tabla compania)


-- ----------------------------------------------------------------------------
-- A) id_tmpenc / id_tmpdet  ->  @TableGenerator sobre 'secuencia'
-- ----------------------------------------------------------------------------
--  El id de sf_tmpenc / sf_tmpdet pasa a generarse con @TableGenerator de
--  Hibernate en vez de newId_sf_tmpenc()/newId_sf_tmpdet().
--
--  Ajuste (+1): el generador entrega 'valor' y luego lo incrementa, mientras la
--  funcion entregaba 'valor+1'. Sin este ajuste el primer asiento reutilizaria el
--  ultimo id existente y chocaria con la clave primaria.
--
--  ROLLBACK: UPDATE secuencia SET valor = valor - 1 WHERE tabla IN ('sf_tmpenc','sf_tmpdet');

UPDATE secuencia
   SET valor = valor + 1
 WHERE tabla IN ('sf_tmpenc', 'sf_tmpdet');


-- ----------------------------------------------------------------------------
-- B) no_trans / no_doc  ->  '_sequence' por compania (idcompania + version)
-- ----------------------------------------------------------------------------
--  Evoluciona '_sequence' al estilo de 'gensecuencia': se le agrega la compania
--  (idcompania, FK a compania) y el control optimista (version). La clave pasa a
--  ser (seq_name, idcompania). no_trans y no_doc de asientos se generan desde
--  Hibernate (entidad FinancesSequence) por compania real.
--
--  Las filas actuales de '_sequence' CONSERVAN su seq_val; solo se les asigna la
--  compania (no hay seeding de valores). Todos los correlativos que antes daba
--  getNextSeq (asientos, VALE, ventas) ahora se generan por JPA (FinancesSequence),
--  que siempre informa idcompania: por eso la columna NO necesita DEFAULT.
--
--  ROLLBACK:
--    ALTER TABLE _sequence DROP FOREIGN KEY fk_sequence_compania;
--    ALTER TABLE _sequence DROP PRIMARY KEY, ADD PRIMARY KEY (seq_name);
--    ALTER TABLE _sequence DROP COLUMN idcompania, DROP COLUMN version;

-- B.1) Agregar columnas y asignar la compania a las filas existentes.
--      seq_val se amplia a BIGINT: al mapear '_sequence' como entidad JPA
--      (FinancesSequence.value = long), Hibernate valida el tipo y 'int' no cuadraria.
ALTER TABLE _sequence
    MODIFY COLUMN seq_val  BIGINT NOT NULL,
    ADD COLUMN    idcompania BIGINT NULL,
    ADD COLUMN    version    BIGINT NOT NULL DEFAULT 0;

UPDATE _sequence SET idcompania = @company_id WHERE idcompania IS NULL;

-- B.2) idcompania obligatoria. Sin DEFAULT: el unico escritor de '_sequence' es la
--      entidad JPA FinancesSequence, que siempre informa idcompania.
ALTER TABLE _sequence
    MODIFY COLUMN idcompania BIGINT NOT NULL;

-- B.3) Clave por (seq_name, idcompania)
ALTER TABLE _sequence
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (seq_name, idcompania);

-- B.4) Integridad referencial con compania
ALTER TABLE _sequence
    ADD CONSTRAINT fk_sequence_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania);


-- ----------------------------------------------------------------------------
-- C) sf_tmpenc: idcompania (FK a compania) -> asiento ligado a la entidad Company
-- ----------------------------------------------------------------------------
--  Se agrega la compania real al asiento (Voucher @ManyToOne Company), reemplazando
--  al no_cia legacy (que se mantiene por compatibilidad). Las filas existentes se
--  asignan a @company_id. La estampa CompanyListener con la compania de la sesion.
--
--  ROLLBACK:
--    ALTER TABLE sf_tmpenc DROP FOREIGN KEY fk_sf_tmpenc_compania;
--    ALTER TABLE sf_tmpenc DROP COLUMN idcompania;

ALTER TABLE sf_tmpenc
    ADD COLUMN idcompania BIGINT NULL;

UPDATE sf_tmpenc SET idcompania = @company_id WHERE idcompania IS NULL;

ALTER TABLE sf_tmpenc
    MODIFY COLUMN idcompania BIGINT NOT NULL;

ALTER TABLE sf_tmpenc
    ADD CONSTRAINT fk_sf_tmpenc_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania);


-- ----------------------------------------------------------------------------
-- D) sf_tmpdet: idcompania (FK a compania) -> detalle del asiento con Company
-- ----------------------------------------------------------------------------
--  Igual que el encabezado: el detalle (VoucherDetail @ManyToOne Company) lleva la
--  compania real. Las filas existentes se asignan a @company_id; la estampa
--  CompanyListener al persistir.
--
--  ROLLBACK:
--    ALTER TABLE sf_tmpdet DROP FOREIGN KEY fk_sf_tmpdet_compania;
--    ALTER TABLE sf_tmpdet DROP COLUMN idcompania;

ALTER TABLE sf_tmpdet
    ADD COLUMN idcompania BIGINT NULL;

UPDATE sf_tmpdet SET idcompania = @company_id WHERE idcompania IS NULL;

ALTER TABLE sf_tmpdet
    MODIFY COLUMN idcompania BIGINT NOT NULL;

ALTER TABLE sf_tmpdet
    ADD CONSTRAINT fk_sf_tmpdet_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania);


-- ----------------------------------------------------------------------------
-- E) sf_tmpenc: version -> bloqueo optimista (convencion de la arquitectura)
-- ----------------------------------------------------------------------------
--  Control de concurrencia de EDICION: dos usuarios editando/aprobando el mismo
--  asiento ya no se pisan en silencio; el segundo recibe OptimisticLockException.
--  DEFAULT 0 deja las filas existentes consistentes.
--
--  ROLLBACK: ALTER TABLE sf_tmpenc DROP COLUMN version;

ALTER TABLE sf_tmpenc
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


-- ----------------------------------------------------------------------------
-- F) sf_tmpdet: version -> bloqueo optimista
-- ----------------------------------------------------------------------------
--  ROLLBACK: ALTER TABLE sf_tmpdet DROP COLUMN version;

ALTER TABLE sf_tmpdet
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


-- ----------------------------------------------------------------------------
-- G) LIMPIEZA de funciones almacenadas (EJECUTAR SOLO DESPUES DE VALIDAR)
-- ----------------------------------------------------------------------------
--  Tras esta migracion, la generacion de ids y correlativos de asientos, vales y
--  ventas pasa a Hibernate/JPA. Estas funciones quedan SIN llamadores. Se conservan
--  como respaldo; descomentar y ejecutar recien cuando la migracion este validada
--  en marcha.
--
-- DROP FUNCTION IF EXISTS getNextSeq;         -- asientos/vales/ventas -> FinancesSequence
-- DROP FUNCTION IF EXISTS newId_sf_tmpenc;    -- id_tmpenc -> @TableGenerator
-- DROP FUNCTION IF EXISTS newId_sf_tmpdet;    -- id_tmpdet -> @TableGenerator
-- DROP FUNCTION IF EXISTS next_tmpenc;        -- sin uso (solo llamadores comentados)
-- DROP FUNCTION IF EXISTS sigte_trans;        -- sin uso
--
--  NO borrar (siguen en uso, otra etapa):
--    sigte_conci                  -> conciliacion (PayableDocumentServiceBean)
--    newId_inv_inventario_detalle -> PK detalle de inventario (ApprovalWarehouseVoucher)
