-- query_v6.0.111_terdemol.sql
-- Permiso CASHACCOUNTLEVEL: boton "Analizar niveles" (VIEW) y "Corregir todo"
-- (UPDATE) de /finances/cashAccountList.xhtml. El grant a roles se hace desde
-- Administracion > Roles.

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad VALUES
    (@nuevo_id, 'CASHACCOUNTLEVEL', 'Analisis y correccion de niveles de cuentas (cta_raiz / cta_niv3)', 5, 5, 'Functionality.finances.cashAccountLevelAnalysis', 1);
