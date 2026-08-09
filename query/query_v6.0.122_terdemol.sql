-- ============================================================================
-- query_v6.0.122_terdemol.sql
-- ============================================================================
-- Permiso del CRUD de cuentas bancarias (ck_ctas_bco / FinancesBankAccount):
-- /finances/financesBankAccountList.xhtml y /finances/financesBankAccount.xhtml.
--
-- Bitmask permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. 15 = CRUD completo.
-- idmodulo = 5 (finances). idfuncionalidad NO es auto_increment: se calcula
-- MAX+1 (hardcodear un id choca en otras bases).
--
-- El grant a roles se hace desde Administracion > Roles; hasta entonces la
-- opcion queda oculta para todos. No se auto-asigna a ningun rol.
-- ----------------------------------------------------------------------------

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'FINANCESBANKACCOUNT', 'Cuentas bancarias (ck_ctas_bco)', 5, 15, 'Functionality.finances.financesBankAccount', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'FINANCESBANKACCOUNT');


-- ============================================================================
-- sf_tmpenc.open / sf_tmpenc.close NOT NULL  (aplica a TODOS los clientes)
-- ============================================================================
-- Sintoma: al anular (o aprobar, o modificar) un asiento contable:
--
--   org.hibernate.PropertyValueException: not-null property references a null
--   or transient value: com.encens.khipus.model.finances.Voucher.closingSeat
--
-- Causa: Voucher mapea las columnas como obligatorias
--
--   @Column(name = "open",  nullable = false)  private Boolean openingSeat;
--   @Column(name = "close", nullable = false)  private Boolean closingSeat;
--
-- pero en la BD son NULLABLE y sin DEFAULT. Hibernate valida la propiedad antes
-- de cualquier UPDATE (Nullability.checkNullability), asi que un asiento cuya
-- fila tiene close/open en NULL NO se puede actualizar por ninguna via.
--
-- Las columnas se crearon en query_v6.0.53.sql con su relleno a 0, pero ese
-- relleno no llego a todas las bases: donde falto, quedaron TODAS las filas en
-- NULL y por lo tanto todos los asientos bloqueados.
--
-- La funcionalidad de asientos de apertura/cierre esta pendiente, pero estas
-- columnas afectan a todo asiento que se guarde, no solo a esa funcionalidad.
--
-- Poner 0 no pierde informacion: donde estan en NULL nunca se marco ningun
-- asiento como apertura ni cierre. 0 = "no es asiento de apertura/cierre".
-- ----------------------------------------------------------------------------

-- 1) Diagnostico previo. Si open_null/close_null son 0, no hay nada que hacer.
SELECT COUNT(*)             AS total,
       SUM(`open`  IS NULL) AS open_null,
       SUM(`close` IS NULL) AS close_null
  FROM sf_tmpenc;

-- 2) Relleno de las filas existentes. Idempotente.
UPDATE sf_tmpenc SET `open`  = 0 WHERE `open`  IS NULL;
UPDATE sf_tmpenc SET `close` = 0 WHERE `close` IS NULL;

-- 3) Blindaje: que ninguna insercion futura pueda dejar NULL, venga de la
--    aplicacion, de SQL nativo o de cualquier otra herramienta.
--    Debe ejecutarse DESPUES del paso 2 o falla.
ALTER TABLE sf_tmpenc MODIFY `open`  int NOT NULL DEFAULT 0;
ALTER TABLE sf_tmpenc MODIFY `close` int NOT NULL DEFAULT 0;

-- 4) Verificacion. Esperado: open_null = 0 y close_null = 0.
SELECT COUNT(*)             AS total,
       SUM(`open`  IS NULL) AS open_null,
       SUM(`close` IS NULL) AS close_null
  FROM sf_tmpenc;


-- Tipos de cambio de los cierres de mes 03/2025 a 07/2026 a 6,96 (la provision los exige por fecha exacta).
INSERT INTO arcgtc (clase_cambio, fecha, tipo_cambio)
SELECT m.clase_cambio, f.fecha, 6.960000
  FROM (SELECT clase_cambio FROM cg_moneda WHERE cod_mon = 'D') m
  JOIN (SELECT '2025-03-31' fecha UNION ALL SELECT '2025-04-30' UNION ALL SELECT '2025-05-31' UNION ALL
        SELECT '2025-06-30' UNION ALL SELECT '2025-07-31' UNION ALL SELECT '2025-08-31' UNION ALL
        SELECT '2025-09-30' UNION ALL SELECT '2025-10-31' UNION ALL SELECT '2025-11-30' UNION ALL
        SELECT '2025-12-31' UNION ALL SELECT '2026-01-31' UNION ALL SELECT '2026-02-28' UNION ALL
        SELECT '2026-03-31' UNION ALL SELECT '2026-04-30' UNION ALL SELECT '2026-05-31' UNION ALL
        SELECT '2026-06-30' UNION ALL SELECT '2026-07-31') f
 WHERE NOT EXISTS (SELECT 1 FROM arcgtc t WHERE t.fecha = f.fecha AND t.clase_cambio = m.clase_cambio);


-- Permiso del CRUD de tipos de cuenta de ahorro (tipocuenta), en Atencion al cliente > Configuracion.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'ACCOUNTTYPE', 'Tipos de cuenta de ahorro (tipocuenta)', 1, 15, 'Functionality.customers.accountType', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'ACCOUNTTYPE');


-- Un tipocuenta.activo en NULL desapareceria de los combos de alta y renovacion, que ahora filtran por activos.
UPDATE tipocuenta SET activo = 1 WHERE activo IS NULL;


-- El alta de tipos de cuenta usa @TableGenerator sobre `secuencia`, donde `valor` es el
-- PROXIMO id a entregar, no el ultimo entregado: MultipleHiLoPerTableGenerator devuelve lo
-- que leyo y recien despues incrementa. OJO que no es la misma semantica que la fila de
-- `funcionalidad` de mas abajo, que va por funcion almacenada (nextValue = valor + 1).
-- Sin fila, Hibernate la crea en 0, descarta el 0 y el primer alta intenta el id 1, que ya
-- existe. Por eso se siembra en MAX + 1.
INSERT INTO secuencia (tabla, valor)
SELECT 'tipocuenta', (SELECT COALESCE(MAX(idtipocuenta), 0) + 1 FROM tipocuenta)
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'tipocuenta');

-- Verificacion. Esperado: secuencia = max_id + 1.
SELECT (SELECT valor FROM secuencia WHERE tabla = 'tipocuenta') AS secuencia,
       (SELECT MAX(idtipocuenta) FROM tipocuenta)               AS max_id;




-- Caja general en moneda extranjera. La MN ya existia (cajagral1mn = 1110110100); esta
-- faltaba y por eso el retiro parcial de la renovacion tenia el 1110220000 hardcodeado.
ALTER TABLE configuracion
    ADD COLUMN cajagral1me varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'Caja general ME';

ALTER TABLE configuracion
    ADD CONSTRAINT fk_configuracion_cajagral1me FOREIGN KEY (cajagral1me) REFERENCES arcgms (cuenta);

-- 1110220000 Billetes y Monedas Extranjeras: es la que se venia usando a mano.
UPDATE configuracion SET cajagral1me = '1110220000';


-- Permiso del cierre de DPF (boton "Cerrar DPF" de la ficha de la cuenta).
-- Solo consultar el calculo (VIEW=1) y registrar el cierre (CREATE=2): permiso = 3.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'DPFCLOSE', 'Cierre de Depositos a Plazo Fijo', 1, 3, 'Functionality.customers.dpfClose', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'DPFCLOSE');


-- Tipo de comprobante con el que se registra la apertura de un DPF al aprobarlo.
-- CI es el criterio historico: debita la caja general y acredita el capital del certificado.
ALTER TABLE configuracion
    ADD COLUMN tipo_doc_dpf varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Comprobante de apertura DPF';

ALTER TABLE configuracion
    ADD CONSTRAINT fk_configuracion_tipo_doc_dpf FOREIGN KEY (tipo_doc_dpf) REFERENCES tipodoc (nombre);

UPDATE configuracion SET tipo_doc_dpf = 'CI';


-- Codigo de DPF autogenerado: una secuencia por moneda (gensecuencia.valor = ultimo entregado).
-- Se siembra con el MAX real de cada serie, asi vale igual en local que en produccion.
-- La secuencia vieja ACCOUNT_DPF_CODE era la de ME sin decirlo: se renombra, no se pierde.
UPDATE gensecuencia SET nombre = 'ACCOUNT_DPF_CODE_ME' WHERE nombre = 'ACCOUNT_DPF_CODE';

SET @max_me = (SELECT COALESCE(MAX(CAST(SUBSTRING(c.codigo, 3) AS UNSIGNED)), 0)
                 FROM cuenta c JOIN tipocuenta t ON t.idtipocuenta = c.idtipocuenta
                WHERE t.tipo = 'DPF' AND c.codigo REGEXP '^ME[0-9]+$');
SET @max_mn = (SELECT COALESCE(MAX(CAST(SUBSTRING(c.codigo, 3) AS UNSIGNED)), 0)
                 FROM cuenta c JOIN tipocuenta t ON t.idtipocuenta = c.idtipocuenta
                WHERE t.tipo = 'DPF' AND c.codigo REGEXP '^MN[0-9]+$');

SET @next_id = (SELECT COALESCE(MAX(idgensecuencia), 0) + 1 FROM gensecuencia);
INSERT INTO gensecuencia (idgensecuencia, nombre, valor, idcompania)
SELECT @next_id, 'ACCOUNT_DPF_CODE_ME', 0, 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM gensecuencia WHERE nombre = 'ACCOUNT_DPF_CODE_ME');

SET @next_id = (SELECT COALESCE(MAX(idgensecuencia), 0) + 1 FROM gensecuencia);
INSERT INTO gensecuencia (idgensecuencia, nombre, valor, idcompania)
SELECT @next_id, 'ACCOUNT_DPF_CODE_MN', 0, 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM gensecuencia WHERE nombre = 'ACCOUNT_DPF_CODE_MN');

UPDATE gensecuencia SET valor = GREATEST(COALESCE(valor, 0), @max_me) WHERE nombre = 'ACCOUNT_DPF_CODE_ME';
UPDATE gensecuencia SET valor = GREATEST(COALESCE(valor, 0), @max_mn) WHERE nombre = 'ACCOUNT_DPF_CODE_MN';

-- secuencia.gensecuencia entrega el id de las filas nuevas de gensecuencia: no puede quedar atras.
UPDATE secuencia
   SET valor = GREATEST(COALESCE(valor, 0), (SELECT MAX(idgensecuencia) + 1 FROM gensecuencia))
 WHERE tabla = 'gensecuencia';


-- Permiso del boton Anular de la ficha de la cuenta. Solo UPDATE = 4.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'ACCOUNTANNUL', 'Anulacion de cuentas de ahorro y DPF', 1, 4, 'Functionality.customers.accountAnnul', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'ACCOUNTANNUL');


-- secuencia.funcionalidad quedo en 18 y el MAX real es 516: un alta desde la aplicacion chocaria.
UPDATE secuencia
   SET valor = GREATEST(COALESCE(valor, 0), (SELECT MAX(idfuncionalidad) FROM funcionalidad))
 WHERE tabla = 'funcionalidad';


-- Fila huerfana: RevisionEntityInfo numera por secuencia/revisionentidad, no por gensecuencia.
-- Solo local
-- DELETE FROM gensecuencia WHERE nombre = 'RevisionEntityInfo';
