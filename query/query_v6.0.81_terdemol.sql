-- ============================================================================
-- v6.0.81 :: Cliente - Campo Código Transbordo
--             Nuevo campo `codigo_transbordo` en personacliente para identificar
--             al cliente con un código operativo distinto al codigocliente
--             interno. Va impreso en la celda "CLIENTE / TRANSBORDO" del
--             Certificado de Despacho.
-- ============================================================================
--
--   - Es opcional (NULL permitido). Clientes ocasionales pueden no tenerlo.
--   - UNIQUE global (la tabla personacliente no tiene idcompania, asi que la
--     unicidad no puede ser por empresa). MySQL permite multiples NULLs en
--     columnas UNIQUE, por lo que los clientes sin código no conflictún.
--   - Longitud 30 caracteres (ej. "AR0001-30-71401255" = 18 chars, dejamos margen).
-- ----------------------------------------------------------------------------

ALTER TABLE personacliente
    ADD COLUMN codigo_transbordo VARCHAR(30) NULL AFTER codigocliente,
    ADD UNIQUE KEY uq_personacliente_transbordo (codigo_transbordo);

-- ----------------------------------------------------------------------------
-- Observacion general del despacho: ampliar de VARCHAR(500) a VARCHAR(1000).
-- ----------------------------------------------------------------------------
ALTER TABLE inv_valedespacho
    MODIFY COLUMN observacion VARCHAR(1000) NULL;
