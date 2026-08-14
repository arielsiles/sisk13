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


-- Baja de permisos de pantallas eliminadas del menu: Despacho de pedidos, Aprobar asientos contables y Pendientes de entrega.

delete da from derechoacceso da
  join funcionalidad f on f.idfuncionalidad = da.idfuncionalidad
 where f.codigo in ('PREPAREDELIVERY', 'APPROVEDALLACCOUNTENTRIES', 'PENDINGSOLDPRODUCTDELIVERYREPORT');

delete from funcionalidad
 where codigo in ('PREPAREDELIVERY', 'APPROVEDALLACCOUNTENTRIES', 'PENDINGSOLDPRODUCTDELIVERYREPORT');

-- Las tablas inv_ventart y entregaarticulo NO se tocan: conservan sus datos historicos.


-- Permisos propios de Configuracion al pasar del tab legacy a Contabilidad. idmodulo 5 (finances), permiso 15 (CRUD). TAXPERCENTAGETYPE (13) y DOSAGETYPE (14) ya existen.

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'TAXRULE', 'Reglas tributarias', 5, 15, 'Functionality.finances.taxRule', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'TAXPERCENTAGE', 'Porcentajes tributarios', 5, 15, 'Functionality.finances.taxPercentage', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'CASHACCOUNTGROUP', 'Grupos de cuenta contable', 5, 15, 'Functionality.finances.cashAccountGroup', 1);

update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- Permisos propios de Presupuestos, Tesoreria y Cuentas por cobrar al pasar del tab legacy a Contabilidad. Son reportes: idmodulo 5, permiso 1 (VIEW).

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'ENTRYBUDGETREPORT', 'Reporte de presupuesto de ingresos', 5, 1, 'Functionality.finances.budget.entryBudgetReport', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'EXPENSEBUDGETREPORT', 'Reporte de presupuesto de gastos', 5, 1, 'Functionality.finances.budget.expenseBudgetReport', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'EXPENSEBUDGETEXECUTIONREPORT', 'Reporte de ejecucion de presupuesto de gasto', 5, 1, 'Functionality.finances.budget.expenseBudgetExecutionReport', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'EXPENSEBUDGETCONSOLIDATEDEXECREPORT', 'Reporte de ejecucion de presupuesto de gasto consolidado', 5, 1, 'Functionality.finances.budget.expenseBudgetConsolidatedExecutionReport', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'EXPENSEBUDGETGLOBALEXECUTIONREPORT', 'Reporte de ejecucion de presupuesto de gasto global', 5, 1, 'Functionality.finances.budget.expenseBudgetGlobalExecutionReport', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTROTATORYFUND', 'Reporte de fondo rotatorio', 5, 1, 'Functionality.finances.report.rotatoryFund', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTROTATORYFUNDBALANCES', 'Reporte de saldos de fondos rotatorios', 5, 1, 'Functionality.finances.report.rotatoryFundBalances', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTROTATORYFUNDBALANCESDETAIL', 'Reporte de detalle de saldos de fondos rotatorios', 5, 1, 'Functionality.finances.report.rotatoryFundBalancesDetail', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTROTATORYFUNDPAYRECEIVABLE', 'Reporte de pagos de fondos rotatorios por cobrar', 5, 1, 'Functionality.finances.report.rotatoryFundPaymentReceivable', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTRETRIEVEBYDEBTOR', 'Reporte de cobros por deudor', 5, 1, 'Functionality.finances.report.retrieveByDebtor', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTROTATORYFUNDCOLLECTIONDETAIL', 'Reporte de detalle de cobros de fondos rotatorios', 5, 1, 'Functionality.finances.report.rotatoryFundCollectionDetail', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'REPORTROTATORYFUNDINVOICECOLLECTION', 'Reporte de cobro de facturas de fondos rotatorios', 5, 1, 'Functionality.finances.report.rotatoryFundInvoiceCollection', 1);

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
insert into funcionalidad values (@nuevo_id, 'ROTFUNDBYACCOUNTREPORT', 'Reporte de fondos rotatorios por cuenta contable', 5, 1, 'Functionality.finances.report.rotatoryFundByAccount', 1);

update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- Baja del permiso del tab legacy financiero: todas sus opciones ya migraron a Contabilidad.

delete da from derechoacceso da
  join funcionalidad f on f.idfuncionalidad = da.idfuncionalidad
 where f.codigo = 'LEGACYFINANCIALADMINMANAGEMENT';

delete from funcionalidad where codigo = 'LEGACYFINANCIALADMINMANAGEMENT';
