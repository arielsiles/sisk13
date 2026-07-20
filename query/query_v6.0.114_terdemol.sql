-- ============================================================================
-- v6.0.114 :: Asientos contables - id_tmpenc / id_tmpdet generados por JPA
-- ============================================================================
--
--  IMPORTANTE: hibernate.hbm2ddl.auto = validate. Ejecutar este script
--  JUSTO ANTES del deploy del nuevo codigo, con el sistema DETENIDO (sin
--  usuarios creando asientos), porque ajusta los contadores de secuencia.
--
--  CONTEXTO
--  --------
--  Antes: el id de sf_tmpenc (id_tmpenc) y de sf_tmpdet (id_tmpdet) se asignaba
--  a mano llamando a las funciones almacenadas newId_sf_tmpenc() /
--  newId_sf_tmpdet(). Esas funciones NO son seguras ante concurrencia
--  (leen el valor y lo actualizan en dos pasos sin bloqueo), por lo que dos
--  usuarios simultaneos podian obtener el mismo id.
--
--  Ahora: el id lo genera Hibernate via @TableGenerator sobre la tabla
--  'secuencia' (el mismo mecanismo que usan las otras ~333 entidades del
--  sistema), con compare-and-swap y reintento. Es seguro entre modulos,
--  usuarios y sedes.
--
--  POR QUE ESTE AJUSTE (+1)
--  ------------------------
--  Las dos mecanicas interpretan la columna 'valor' de forma distinta:
--    * La funcion:  valor = ULTIMO id ya usado.  Devuelve valor+1.
--    * Hibernate:   valor = PROXIMO id a asignar. Devuelve valor, luego +1.
--  Es una diferencia de 1. Si no se ajusta, el primer asiento que se guarde
--  con el nuevo codigo intentaria reusar el ultimo id ya existente y chocaria
--  con la clave primaria. Por eso se incrementa 'valor' en 1, una sola vez.
--
--  Las funciones almacenadas NO se eliminan: quedan definidas en la base como
--  respaldo hasta confirmar la migracion. Simplemente el codigo de asientos
--  ya no las llama.
--
--  ROLLBACK (si se revierte el codigo al esquema anterior):
--    UPDATE terdemol.secuencia SET valor = valor - 1
--     WHERE tabla IN ('sf_tmpenc','sf_tmpdet');
--  (y volver a la version anterior de Voucher.java / VoucherDetail.java /
--   VoucherAccoutingServiceBean.java / WarehouseAccountEntryServiceBean.java)
-- ----------------------------------------------------------------------------

-- 0) DIAGNOSTICO (ejecutar y REVISAR antes de continuar) --------------------
--    Debe devolver exactamente una fila por cada tabla, y valor >= al maximo
--    id realmente usado. Si aparece 'REVISAR: valor < max' NO continuar:
--    corregir el valor manualmente a max_id antes de aplicar el paso 1.

SELECT s.tabla,
       COUNT(*)                         AS filas_en_secuencia,
       MAX(s.valor)                     AS valor_actual,
       (SELECT MAX(e.id_tmpenc) FROM terdemol.sf_tmpenc e) AS max_id_encabezado,
       (SELECT MAX(d.id_tmpdet) FROM terdemol.sf_tmpdet d) AS max_id_detalle
  FROM terdemol.secuencia s
 WHERE s.tabla IN ('sf_tmpenc','sf_tmpdet')
 GROUP BY s.tabla;

--    Verificacion de coherencia (no debe devolver ninguna fila):
SELECT 'sf_tmpenc' AS tabla, s.valor, m.max_id
  FROM terdemol.secuencia s
  JOIN (SELECT MAX(id_tmpenc) AS max_id FROM terdemol.sf_tmpenc) m
 WHERE s.tabla = 'sf_tmpenc' AND s.valor < m.max_id
UNION ALL
SELECT 'sf_tmpdet' AS tabla, s.valor, m.max_id
  FROM terdemol.secuencia s
  JOIN (SELECT MAX(id_tmpdet) AS max_id FROM terdemol.sf_tmpdet) m
 WHERE s.tabla = 'sf_tmpdet' AND s.valor < m.max_id;

--    Verificacion de fila unica (no debe haber duplicados de 'tabla';
--    'secuencia' no tiene indice unico). Si devuelve conteo > 1, consolidar
--    a una sola fila antes de continuar:
SELECT tabla, COUNT(*) AS veces
  FROM terdemol.secuencia
 WHERE tabla IN ('sf_tmpenc','sf_tmpdet')
 GROUP BY tabla
HAVING COUNT(*) > 1;


-- 1) AJUSTE (+1) -- aplicar SOLO si el diagnostico anterior salio limpio ----
UPDATE terdemol.secuencia
   SET valor = valor + 1
 WHERE tabla IN ('sf_tmpenc','sf_tmpdet');


-- 2) VERIFICACION FINAL ------------------------------------------------------
--    'valor' debe quedar en max_id + 1 para ambas tablas.
SELECT s.tabla, s.valor AS proximo_id
  FROM terdemol.secuencia s
 WHERE s.tabla IN ('sf_tmpenc','sf_tmpdet');
