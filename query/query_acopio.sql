    -- Q1. Descuentos productores
SELECT *
FROM movimientosalarioproductor
WHERE `fecha` BETWEEN '2026-02-01' AND '2026-02-28'
AND idtipomovimientoproductor = 3
;

    -- Q2. Movimientos de productores con datos personales
    -- Nota: LEFT JOIN porque existen 3 registros huerfanos en movimientosalarioproductor
    -- (idproductormateriaprima 739, 745, 746) que no tienen correspondencia en productormateriaprima.
SELECT p.idpersona,
       m.idproductormateriaprima,
       p.nombres,
       p.apellidopaterno,
       p.apellidomaterno,
       m.fecha,
       m.estado,
       m.valor,
       m.descripcion
FROM movimientosalarioproductor m
    LEFT JOIN productormateriaprima mp ON mp.idproductormateriaprima = m.idproductormateriaprima
    LEFT JOIN persona p ON p.idpersona = mp.idproductormateriaprima
WHERE m.fecha BETWEEN '2026-02-01' AND '2026-02-28'
AND m.idtipomovimientoproductor = 3
ORDER BY p.apellidopaterno, p.apellidomaterno, p.nombres
;