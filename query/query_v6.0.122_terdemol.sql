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


-- Permiso del ABM de tipos de cuenta de ahorro (tipocuenta), en Atencion al cliente > Configuracion.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'ACCOUNTTYPE', 'Tipos de cuenta de ahorro (tipocuenta)', 1, 15, 'Functionality.customers.accountType', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'ACCOUNTTYPE');


-- Un tipocuenta.activo en NULL desapareceria de los combos de alta y renovacion, que ahora filtran por activos.
UPDATE tipocuenta SET activo = 1 WHERE activo IS NULL;


-- Diagnostico antes del primer alta desde la aplicacion: si no hay fila o valor < max_id, el @TableGenerator chocaria.
SELECT (SELECT valor FROM secuencia WHERE tabla = 'tipocuenta') AS secuencia,
       (SELECT MAX(idtipocuenta) FROM tipocuenta)               AS max_id;


-- secuencia.funcionalidad quedo en 18 y el MAX real es 516: un alta desde la aplicacion chocaria.
UPDATE secuencia
   SET valor = GREATEST(COALESCE(valor, 0), (SELECT MAX(idfuncionalidad) FROM funcionalidad))
 WHERE tabla = 'funcionalidad';
