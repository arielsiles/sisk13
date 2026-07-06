-- ============================================================================
-- v6.0.98 :: Cliente - clase_cliente (persona/institucion) estilo Odoo
-- ============================================================================
--
--  Reemplaza el manejo de tipo_persona (String hardcodeado, redundante con
--  espersona) por el patron de Odoo:
--    - espersona (personFlag) = UNICA fuente de verdad.
--    - clase_cliente          = valor derivado, sincronizado por el entity
--                               (Client @PrePersist/@PreUpdate), sin literales
--                               dispersos. Se conserva la columna por si algun
--                               reporte/BI externo la consume.
--
--  Cambios:
--    1) Renombra la columna tipo_persona -> clase_cliente. Se renombra porque
--       "tipo_persona" era ambiguo y "tipo_cliente" chocaria con el catalogo
--       ClientType existente (idtipocliente / "Tipo Cliente").
--    2) Normaliza los datos existentes en base a espersona (fuente de verdad):
--       antes: 'cliente'/'institucion'  ->  ahora: 'persona'/'institucion'.
--
--  NOTA: el mapeo Java ya usa 'clase_cliente'; MySQL no distingue mayus/minus
--  en identificadores, asi que el rename no rompe consultas. Verificar que el
--  tipo de la columna sea VARCHAR antes de ejecutar en produccion.
-- ----------------------------------------------------------------------------

ALTER TABLE personacliente
    CHANGE tipo_persona clase_cliente VARCHAR(45) NULL;

-- Normaliza segun la fuente de verdad (espersona): 0 = institucion, resto = persona.
UPDATE personacliente
    SET clase_cliente = CASE WHEN espersona = 0 THEN 'institucion' ELSE 'persona' END;
