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


-- ############################################################################
-- INICIO — SOLO TERDEMOL. No aplicar en otros clientes.
-- ############################################################################

-- 3) Borrado del ciclo de planillas: se empieza de cero.
DELETE FROM detregcontable;
DELETE FROM registrocontable;
DELETE FROM reportecontrol;
DELETE FROM detplanilladocentelaboral;
DELETE FROM planilladocentelaboral;
DELETE FROM planillaaguinaldo;
DELETE FROM planillageneral;
DELETE FROM planillaadministrativos;
DELETE FROM planillafiscalporcategoria;
DELETE FROM planillatributariaporcategoria;
DELETE FROM planillafiscal;
DELETE FROM planillatributaria;
DELETE FROM planillagenerada;
DELETE FROM movimientosueldo;
DELETE FROM previsionretiro;
DELETE FROM horasextra;
DELETE FROM bonoconseguido;
DELETE FROM gestionplanillaadm;
DELETE FROM gestionplanilla;
DELETE FROM ciclogeneracionplanilla;

-- 4) Marcaciones: hay que reimportar el biometrico antes de evaluar asistencia.
DELETE FROM rh_marcado;
DELETE FROM marca;
DELETE FROM loteimportmarcado;

-- 5) Banco de horas.
DELETE FROM movimientobancohoras;

-- 6) Vacaciones. `reglavacacion` NO se toca: es la configuracion 15/20/30 dias.
DELETE FROM movimientovacacion;
DELETE FROM gestionvacacion;
DELETE FROM planvacacion;
DELETE FROM fechaespecial;

-- 7) Nivel AREA: es el nombre que el sistema busca para llenar el campo `area`
-- de la planilla. Sin el, los reportes por area salen vacios.
INSERT INTO nivelorganizacional (idnivelorganizacional, sigla, nombre, version, idcompania)
SELECT 3, 'AREA', 'AREA', 0, 1
  FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM nivelorganizacional WHERE nombre = 'AREA');

-- 8) Las areas nuevas. MATERIAS PRIMAS va al mismo nivel que PRODUCCION, no dentro:
-- responde por sustancias controladas y no debe heredar la politica de atrasos de planta.
INSERT INTO unidadorganizacional
  (idunidadorganizacional, sigla, numerocompania, codigocencos, nombre, version,
   idunidadnegocio, idcompania, idnivelorganizacional, unidadorganizacionalraiz, idsector)
VALUES
  (6, 'MP',   '01', '0111', 'MATERIAS PRIMAS', 0, 2, 1, 3, NULL, 1),
  (7, 'PLTA', '01', '0111', 'PLANTA',          0, 2, 1, 2, 5,    1),
  (8, 'MTTO', '01', '0111', 'MANTENIMIENTO',   0, 2, 1, 2, 5,    1),
  (9, 'LAB',  '01', '0111', 'LABORATORIO',     0, 2, 1, 2, 5,    1);

-- 9) Las cuatro existentes pasan a nivel AREA y EJECUTIVO queda sin uso.
UPDATE unidadorganizacional SET idnivelorganizacional = 3
 WHERE idunidadorganizacional IN (2, 3, 4, 5);
DELETE FROM nivelorganizacional WHERE idnivelorganizacional = 1;

-- 10) Las secuencias: `nivelorganizacional` venia en 1 con los ids 1 y 2 ya usados,
-- asi que crear un nivel desde la pantalla chocaba la clave primaria.
UPDATE secuencia SET valor = 4  WHERE tabla = 'nivelorganizacional';
UPDATE secuencia SET valor = 10 WHERE tabla = 'unidadorganizacional';

-- 11) Reasignacion de las personas. Solo contratos ACTIVOS: los puestos historicos
-- quedan donde estaban, que es donde trabajaba esa persona entonces.
UPDATE puesto pu
  JOIN contratopuesto cp ON cp.idpuesto = pu.idpuesto
  JOIN contrato c        ON c.idcontrato = cp.idcontrato
  JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato AND ec.nombre = 'ACTIVO'
   SET pu.idunidadorganizacional = 6
 WHERE pu.idcargo IN (16);

UPDATE puesto pu
  JOIN contratopuesto cp ON cp.idpuesto = pu.idpuesto
  JOIN contrato c        ON c.idcontrato = cp.idcontrato
  JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato AND ec.nombre = 'ACTIVO'
   SET pu.idunidadorganizacional = 7
 WHERE pu.idcargo IN (42, 66, 15, 46, 41, 53, 60);

UPDATE puesto pu
  JOIN contratopuesto cp ON cp.idpuesto = pu.idpuesto
  JOIN contrato c        ON c.idcontrato = cp.idcontrato
  JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato AND ec.nombre = 'ACTIVO'
   SET pu.idunidadorganizacional = 8
 WHERE pu.idcargo IN (68, 12, 33, 69, 49);

UPDATE puesto pu
  JOIN contratopuesto cp ON cp.idpuesto = pu.idpuesto
  JOIN contrato c        ON c.idcontrato = cp.idcontrato
  JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato AND ec.nombre = 'ACTIVO'
   SET pu.idunidadorganizacional = 9
 WHERE pu.idcargo IN (45, 43, 9, 52);

-- 12) Limpieza. Los otros 34 cargos sin gente activa NO se borran: tienen puestos
-- historicos colgando y borrarlos romperia el historico.
DELETE FROM cargo WHERE idcargo = 58;
DELETE FROM sueldo WHERE idsueldo IN (37, 71, 92);

-- 13) Todos los contratos pasan a EVENTUAL, activos e inactivos.
UPDATE contrato SET idmodalidadcontrato = 10
 WHERE idmodalidadcontrato <> 10;

-- ############################################################################
-- FIN — SOLO TERDEMOL.
-- ############################################################################
