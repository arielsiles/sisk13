-- ============================================================================
-- v6.0.82 :: Codigo CLIENTE / TRANSBORDO movido de Cliente al Despacho
-- ============================================================================
--
--  Correccion de enfoque respecto a v6.0.81:
--  El codigo "CLIENTE / TRANSBORDO" que se imprime en el Certificado VARIA
--  por cada despacho (no es un atributo fijo del cliente). Por lo tanto:
--
--    1) Se REVIERTE la columna agregada en v6.0.81 a personacliente
--       (codigo_transbordo + su indice unico).
--    2) Se AGREGA codigo_transbordo VARCHAR(100) a inv_valedespacho.
--
--  El campo es opcional (NULL permitido) y hasta 100 caracteres.
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) Revertir columna en personacliente (introducida en v6.0.81)
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente
    DROP INDEX uq_personacliente_transbordo;

ALTER TABLE personacliente
    DROP COLUMN codigo_transbordo;


-- ----------------------------------------------------------------------------
-- 2) Agregar codigo de transbordo al despacho (por vale, hasta 100 chars)
-- ----------------------------------------------------------------------------
ALTER TABLE inv_valedespacho
    ADD COLUMN codigo_transbordo VARCHAR(100) NULL AFTER idcliente;
