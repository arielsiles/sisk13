-- Alinea haberbasicolaboral con el sueldo del puesto: solo lo escribia la integracion Wise, que no corre.
-- Es un campo derivado y de solo lectura; ninguna planilla paga con el.
UPDATE contrato c
  JOIN (SELECT cp.idcontrato, SUM(s.cantidad) suma
          FROM contratopuesto cp
          JOIN puesto pu ON pu.idpuesto = cp.idpuesto
          JOIN sueldo s  ON s.idsueldo  = pu.idsueldo
         GROUP BY cp.idcontrato) t ON t.idcontrato = c.idcontrato
   SET c.haberbasicolaboral = t.suma;

-- 2) Permite cambiar la modalidad del contrato desde la edicion, sin pasar por Cambiar condicion.
-- Es para la etapa de pruebas: en produccion el cambio va por Cambiar condicion, que deja rastro.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'JOBCONTRACTSPECIALUPDATE', 'Cambiar modalidad al editar el contrato', 4, 1, 'Functionality.employees.jobContractSpecialUpdate', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'JOBCONTRACTSPECIALUPDATE');
