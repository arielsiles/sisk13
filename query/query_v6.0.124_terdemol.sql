-- Permiso propio para el reporte "Recaudacion diaria" (antes usaba ACCOUNTING/VIEW). idmodulo 5 (finances), permiso 1 (VIEW).

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);

insert into funcionalidad
values (@nuevo_id, 'DAILYCOLLECTIONREPORT', 'Reporte de Recaudacion Diaria (CISC)',
        5, 1, 'Functionality.accounting.dailyCollectionReport', 1);

update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';
