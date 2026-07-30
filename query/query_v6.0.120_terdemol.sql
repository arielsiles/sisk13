-- ============================================================================
-- v6.0.120 :: xpr_linea.merma_factor pasa a ser NULLABLE
-- ============================================================================
--
--  IMPORTANTE: hibernate.hbm2ddl.auto = validate. Ejecutar este script ANTES
--  del deploy: el codigo nuevo guarda NULL en esta columna.
--
--  Motivo
--  ------
--  merma_factor se creo en v6.0.76 como NOT NULL DEFAULT 1.0300, pero ese 1.03
--  es la constante de merma del proceso ULEXITA: no significa nada para las
--  demas lineas (BARITINA, CHANCADO, GENERAL). La pantalla de Linea de
--  Produccion solo muestra el campo cuando el template es ULEXITA, asi que al
--  crear cualquier otra linea el valor viajaba en NULL y la restriccion
--  reventaba con "Column 'merma_factor' cannot be null". El DEFAULT del motor
--  no ayudaba: Hibernate siempre incluye la columna en el INSERT, y un DEFAULT
--  solo se aplica cuando la columna se OMITE.
--
--  Con la columna nullable, NULL pasa a significar "no aplica a esta linea",
--  que es la verdad. El unico consumidor del campo
--  (XProductionUlexitaCalc.getMermaFactor) ya tolera NULL y cae a su propia
--  constante, asi que el default queda en un solo lugar: el codigo de calculo.
--
--  Las lineas existentes conservan su valor actual (1.0300); no se tocan datos.
-- ----------------------------------------------------------------------------

ALTER TABLE xpr_linea
    MODIFY COLUMN merma_factor DECIMAL(10,4) NULL;

-- ----------------------------------------------------------------------------
-- Verificacion
-- ----------------------------------------------------------------------------
-- SHOW COLUMNS FROM xpr_linea LIKE 'merma_factor';   -- Null debe decir YES
-- SELECT idlinea, codigo, report_template_code, merma_factor FROM xpr_linea;
