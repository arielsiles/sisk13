-- Permisos unicos de los tabs legacy del menu (Gestion Administrativa Financiera y Procesos Productivos). idmodulo 2 (admin), permiso 1 (VIEW).

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);

insert into funcionalidad
values (@nuevo_id, 'LEGACYFINANCIALADMINMANAGEMENT', 'Legacy Gestion Administrativa Financiera',
        2, 1, 'Functionality.admin.legacyFinancialAdminManagement', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);

insert into funcionalidad
values (@nuevo_id, 'LEGACYPRODUCTIONPROCESSES', 'Legacy Procesos Productivos',
        2, 1, 'Functionality.admin.legacyProductionProcesses', 1);

update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';

-- Otorgar al rol Administrador (idrol=1). Descomentar segun el cliente; idmodulo es obligatorio o el grant se ignora.
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo)
-- select f.idfuncionalidad, 1, 1, 1, 2 from funcionalidad f
--  where f.codigo in ('LEGACYFINANCIALADMINMANAGEMENT', 'LEGACYPRODUCTIONPROCESSES');

-- Requisitos: modulocompania (idcompania, idmodulo=2) con activo=1, y volver a iniciar sesion.
