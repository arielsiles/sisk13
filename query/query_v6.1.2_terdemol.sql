-- Alinea haberbasicolaboral con el sueldo del puesto: solo lo escribia la integracion Wise, que no corre.
-- Es un campo derivado y de solo lectura; ninguna planilla paga con el.
UPDATE contrato c
  JOIN (SELECT cp.idcontrato, SUM(s.cantidad) suma
          FROM contratopuesto cp
          JOIN puesto pu ON pu.idpuesto = cp.idpuesto
          JOIN sueldo s  ON s.idsueldo  = pu.idsueldo
         GROUP BY cp.idcontrato) t ON t.idcontrato = c.idcontrato
   SET c.haberbasicolaboral = t.suma;
