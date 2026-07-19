-- query_v6.0.112_terdemol.sql
-- Integridad referencial del plan de cuentas (etapa 1): FK de arcgms.cta_raiz y
-- arcgms.cta_niv3 hacia arcgms.cuenta. SIN cascada: con ON DELETE RESTRICT /
-- ON UPDATE NO ACTION (por defecto), la base rechaza borrar o renumerar una
-- cuenta que otra use como raiz o como nivel 3.
-- Requisito previo: alinear la collation a utf8mb3_bin, la de arcgms.cuenta.

-- 1) Verificacion previa. Las tres deben devolver 0; si no, corregir antes
--    (una FK acepta NULL, pero NO cadenas vacias ni valores huerfanos).
--   select count(*) from arcgms a where a.cta_raiz is not null and a.cta_raiz <> ''
--     and not exists (select 1 from arcgms p where p.cuenta = a.cta_raiz);
--   select count(*) from arcgms a where a.cta_niv3 is not null and a.cta_niv3 <> ''
--     and not exists (select 1 from arcgms p where p.cuenta = a.cta_niv3);
--   select count(*) from arcgms where cta_raiz = '' or cta_niv3 = '';

-- 2) Alinear collation con arcgms.cuenta (utf8mb3_bin). Hoy estas dos columnas
--    heredan utf8mb3_general_ci de la tabla y MySQL exige charset y collation
--    identicos en ambos lados de la FK (si no, error 3780). Los codigos de
--    cuenta son solo digitos, asi que el cambio no altera ninguna comparacion.

ALTER TABLE arcgms MODIFY cta_raiz varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;
ALTER TABLE arcgms MODIFY cta_niv3 varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL;

-- 3) Crear las FK. MySQL crea solo el indice necesario en cada columna
--    (hoy arcgms solo tiene el PRIMARY sobre cuenta).

ALTER TABLE arcgms
    ADD CONSTRAINT fk_arcgms_cta_raiz FOREIGN KEY (cta_raiz) REFERENCES arcgms (cuenta),
    ADD CONSTRAINT fk_arcgms_cta_niv3 FOREIGN KEY (cta_niv3) REFERENCES arcgms (cuenta);

-- Verificacion: deben aparecer las dos constraints.
--   select constraint_name, column_name, referenced_table_name
--     from information_schema.key_column_usage
--    where table_schema = database() and table_name = 'arcgms'
--      and referenced_table_name is not null;

-- ----------------------------------------------------------------------------
-- Pendiente para etapas siguientes: el resto de las columnas que referencian
-- arcgms.cuenta no se puede aun. Dos motivos, ambos verificados sobre terdemol:
--
--   a) Huerfanos que abortarian el ALTER (hay que limpiarlos primero):
--        inv_movdet.cuenta_art        4025
--        pagoordencompra.cuentacaja     38
--        costosindirectosconf.cuenta    23
--        af_subgrupos.cta_vo            10
--        sf_tmpdet.cuenta                7
--        tipocredito.(ctaeje, ctaven, ctavig, ictaeje, ictaven, ictavig)  4 c/u
--
--   b) Varias columnas estan en latin1 (27 en latin1_bin, 4 en
--      latin1_swedish_ci) y 2 en utf8mb4_0900_ai_ci: ahi no basta cambiar la
--      collation, hay conversion de charset de por medio.
-- ----------------------------------------------------------------------------
