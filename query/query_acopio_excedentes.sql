-- =============================================================================
-- query_acopio_excedentes.sql
-- Consultas utiles de ACOPIO (planillas, descuentos/arrastre de deuda) y EXCEDENTES.
-- Solo LECTURA. Ajustar las fechas '2026-06-01'..'2026-06-30' y el productor segun el caso.
-- Quincena = rango de fechas: 1ra = dia 01 al 15, 2da = dia 16 al fin de mes.
-- =============================================================================


-- #############################################################################
-- 1) DESCUENTOS Y ARRASTRE DE DEUDA
-- #############################################################################

-- 1.1) CRUCE PRINCIPAL: productor + descuento + aplicacion.
--      Que descuento hay, cuanto se cobro y EN QUE QUINCENA se cobro.
--      LEFT JOIN: los descuentos aun no cobrados salen con cobro/quincena en NULL.
--      Filtrar por rango de fechas (de la DEUDA) y opcionalmente por productor.
SELECT m.idproductormateriaprima                                              AS id_productor,
       CONCAT(per.nombres,' ',per.apellidopaterno,' ',IFNULL(per.apellidomaterno,'')) AS productor,
       t.nombre                        AS descuento,
       m.fecha                         AS fecha_deuda,
       m.valor                         AS monto_deuda,
       m.saldo                         AS saldo_pendiente,
       m.estado                        AS estado_deuda,
       a.montoaplicado                 AS cobrado,
       p.fechainicio                   AS quincena_cobro_ini,
       p.fechafin                      AS quincena_cobro_fin,
       p.estado                        AS estado_planilla
FROM movimientosalarioproductor m
JOIN persona                     per ON per.idpersona                 = m.idproductormateriaprima
JOIN tipomovimientoproductor     t   ON t.idtipomovimientoproductor   = m.idtipomovimientoproductor
LEFT JOIN aplicacion_descuento_productor a ON a.idmovimientosalarioproductor = m.idmovimientosalarioproductor
LEFT JOIN registropagomateriaprima       r ON r.idregistropagomateriaprima   = a.idregistropagomateriaprima
LEFT JOIN planillapagomateriaprima       p ON p.idplanillapagomateriaprima   = r.idplanillapagomateriaprima
WHERE m.fecha BETWEEN '2026-06-01' AND '2026-06-30'
   AND m.idproductormateriaprima = 3      -- descomentar para un productor puntual
ORDER BY m.idproductormateriaprima, m.fecha, p.fechainicio;


-- 1.2) TRAZABILIDAD DE ARRASTRE: de cuando es la deuda vs en que quincena se cobra.
--      Marca ARRASTRADO cuando se cobra en una quincena posterior al origen de la deuda.
SELECT m.idproductormateriaprima AS id_productor,
       t.nombre                  AS descuento,
       m.fecha                   AS fecha_deuda,
       m.valor                   AS monto_deuda,
       a.montoaplicado           AS cobrado,
       p.fechainicio             AS quincena_cobro_ini,
       p.fechafin                AS quincena_cobro_fin,
       CASE WHEN p.fechainicio > m.fecha THEN 'ARRASTRADO' ELSE 'mismo periodo' END AS tipo_cobro
FROM aplicacion_descuento_productor a
JOIN movimientosalarioproductor m ON m.idmovimientosalarioproductor = a.idmovimientosalarioproductor
JOIN tipomovimientoproductor    t ON t.idtipomovimientoproductor    = m.idtipomovimientoproductor
JOIN registropagomateriaprima   r ON r.idregistropagomateriaprima   = a.idregistropagomateriaprima
JOIN planillapagomateriaprima   p ON p.idplanillapagomateriaprima   = r.idplanillapagomateriaprima
ORDER BY m.idproductormateriaprima, m.fecha, p.fechainicio;


-- 1.3) DEUDAS CON SALDO PENDIENTE a una fecha (lo que arrastrara a la proxima quincena).
SELECT m.idproductormateriaprima AS id_productor,
       CONCAT(per.nombres,' ',per.apellidopaterno,' ',IFNULL(per.apellidomaterno,'')) AS productor,
       t.nombre AS descuento, m.fecha AS fecha_deuda, m.valor AS monto_deuda, m.saldo AS saldo
FROM movimientosalarioproductor m
JOIN persona                 per ON per.idpersona               = m.idproductormateriaprima
JOIN tipomovimientoproductor t   ON t.idtipomovimientoproductor = m.idtipomovimientoproductor
WHERE m.saldo > 0
  AND m.fecha <= '2026-06-30'
ORDER BY m.idproductormateriaprima, m.fecha;


-- #############################################################################
-- 2) INTEGRIDAD / RECONCILIACION (auditoria del saldo)
--    Regla: saldo = valor - SUMA(montoaplicado en planillas CONTABILIZADO).
-- #############################################################################

-- 2.1) RECONCILIACION: movimientos cuyo saldo NO coincide con lo realmente cobrado.
--      Debe devolver 0 filas. Si aparece alguno, el saldo esta corrompido.
SELECT m.idmovimientosalarioproductor AS id_deuda, m.descripcion, m.valor,
       m.saldo AS saldo_actual,
       m.valor - COALESCE(SUM(CASE WHEN p.estado='CONTABILIZADO'
                                   THEN a.montoaplicado ELSE 0 END),0) AS saldo_correcto
FROM movimientosalarioproductor m
JOIN aplicacion_descuento_productor a ON a.idmovimientosalarioproductor = m.idmovimientosalarioproductor
JOIN registropagomateriaprima     r ON r.idregistropagomateriaprima     = a.idregistropagomateriaprima
JOIN planillapagomateriaprima     p ON p.idplanillapagomateriaprima     = r.idplanillapagomateriaprima
GROUP BY m.idmovimientosalarioproductor, m.descripcion, m.valor, m.saldo
HAVING m.saldo <> saldo_correcto
ORDER BY m.idmovimientosalarioproductor;

-- 2.2) Saldos fuera de rango: negativo o mayor que el valor de la deuda. Debe dar 0 filas.
SELECT idmovimientosalarioproductor, descripcion, valor, saldo, estado
FROM movimientosalarioproductor
WHERE saldo < 0 OR saldo > valor;

-- 2.3) Aplicaciones huerfanas (apuntan a un registro inexistente). Debe dar 0 filas.
SELECT a.idaplicacion_descuento_productor, a.idmovimientosalarioproductor, a.montoaplicado
FROM aplicacion_descuento_productor a
LEFT JOIN registropagomateriaprima r ON r.idregistropagomateriaprima = a.idregistropagomateriaprima
WHERE r.idregistropagomateriaprima IS NULL;

-- 2.4) Sobre-cobro: suma de lo cobrado (CONTABILIZADO) supera el valor de la deuda. Debe dar 0 filas.
SELECT m.idmovimientosalarioproductor AS id_deuda, m.valor, SUM(a.montoaplicado) AS total_cobrado
FROM movimientosalarioproductor m
JOIN aplicacion_descuento_productor a ON a.idmovimientosalarioproductor = m.idmovimientosalarioproductor
JOIN registropagomateriaprima     r ON r.idregistropagomateriaprima     = a.idregistropagomateriaprima
JOIN planillapagomateriaprima     p ON p.idplanillapagomateriaprima     = r.idplanillapagomateriaprima
WHERE p.estado = 'CONTABILIZADO'
GROUP BY m.idmovimientosalarioproductor, m.valor
HAVING SUM(a.montoaplicado) > m.valor + 0.01;


-- #############################################################################
-- 3) PLANILLAS DE ACOPIO
-- #############################################################################

-- 3.1) Planillas de un periodo con estado y totales, por zona y tipo (NORMAL/EXCEDENTE, HABIL/DOMINGO).
SELECT z.nombre AS zona, p.fechainicio, p.fechafin, p.tipoplanilla, p.tipodia, p.estado,
       p.preciounitario AS precio, p.totalacopiadoxgab AS litros, p.totalveterinarioxgab AS veterinario,
       p.totaliquidoxgab AS liquido, p.idcomprobante AS comprobante
FROM planillapagomateriaprima p
JOIN zonaproductiva z ON z.idzonaproductiva = p.idzonaproductiva
WHERE p.fechainicio = '2026-06-16' AND p.fechafin = '2026-06-30'
ORDER BY z.nombre, p.tipoplanilla, p.tipodia;

-- 3.2) Detalle de un productor en una quincena (ganado, ajuste, descuentos, liquido).
SELECT p.fechainicio, p.fechafin, p.tipoplanilla, p.tipodia, p.estado,
       r.cantidadtotal AS litros, r.totalganado, r.ajustezonaproductiva AS ajuste,
       d.veterinario, d.credito, d.comision, r.liquidopagable AS liquido
FROM registropagomateriaprima r
JOIN planillapagomateriaprima     p ON p.idplanillapagomateriaprima     = r.idplanillapagomateriaprima
JOIN descuentproductmateriaprima  d ON d.iddescuentproductmateriaprima  = r.iddescuentproductmateriaprima
WHERE d.idproductormateriaprima = 3
  AND p.fechainicio >= '2026-06-01' AND p.fechafin <= '2026-06-30'
ORDER BY p.fechainicio, p.tipoplanilla, p.tipodia;


-- #############################################################################
-- 4) EXCEDENTES (cupo por productor y planillas de excedente)
-- #############################################################################

-- 4.1) Productores con restriccion de acopio (cupo litros/dia + precio excedente) vigente a una fecha.
SELECT rap.idproductormateriaprima AS id_productor,
       CONCAT(per.nombres,' ',per.apellidopaterno,' ',IFNULL(per.apellidomaterno,'')) AS productor,
       rap.cupolitrosdia AS cupo_litros_dia,
       rap.precioexcedentehabil AS precio_exc_habil, rap.precioexcedentedomingo AS precio_exc_domingo,
       rap.fechaini, rap.fechafin, rap.estado
FROM restriccion_acopio_productor rap
JOIN persona per ON per.idpersona = rap.idproductormateriaprima
WHERE '2026-06-16' BETWEEN rap.fechaini AND rap.fechafin
ORDER BY productor;

-- 4.2) Planillas de EXCEDENTE de un periodo (litros y liquido por zona/tipo de dia).
SELECT z.nombre AS zona, p.fechainicio, p.fechafin, p.tipodia, p.estado,
       p.preciounitario AS precio_excedente, p.totalacopiadoxgab AS litros_excedente,
       p.totaliquidoxgab AS liquido_excedente
FROM planillapagomateriaprima p
JOIN zonaproductiva z ON z.idzonaproductiva = p.idzonaproductiva
WHERE p.tipoplanilla = 'EXCEDENTE'
  AND p.fechainicio = '2026-06-16' AND p.fechafin = '2026-06-30'
ORDER BY z.nombre, p.tipodia;
