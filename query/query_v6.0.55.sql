-- 24.04.2025
/*
insert into funcionalidad values (309, 'PURCHASEORDER_PURCHASEREPORT', null, 5, 1, 'menu.finances.purchaseOrder.purchaseReport', 1);
insert into funcionalidad values (310, 'BANKBALANCEPANEL', null, 7, 1, 'Dashboard.accessRight.bankBalance', 1);
insert into funcionalidad values (311, 'INCOMEPANEL', null, 7, 1, 'Dashboard.accessRight.income', 1);
insert into funcionalidad values (312, 'WAREHOUSEMONTHLYCLOSEWIDGET', null, 7, 1, 'Dashboard.accessRight.warehouseMonthlyCloseWidget', 1);
insert into funcionalidad values (313, 'FIXEDASSETWIDGET', null, 7, 1, 'Dashboard.accessRight.fixedAssetWidget', 1);
*/

insert into funcionalidad values (314, 'DASHBOARD_VIEW', null, 7, 1, 'Dashboard.accessRight.panelAccess', 1);
insert into funcionalidad values (315, 'PRODUCTION_INPUTS_REPORT', null, 11, 1, 'menu.xproduction.productionInputsReport', 1);

-- 02.05.2025
alter table xpr_produccion modify column fechafin timestamp;

CREATE TABLE `xpr_turno` (
    `idturno` bigint NOT NULL,
    `nombre` varchar(255) NOT NULL,
    `codigo` varchar(50) NOT NULL,
    `version` bigint NOT NULL,
    `idcompania` bigint NOT NULL,
    PRIMARY KEY (`idturno`)
);

-- insertar datos xpr_turno, 3 registros
insert into xpr_turno (idturno, nombre, codigo, version, idcompania) values (1, 'Turno 1', 'T1', 1, 1);
insert into xpr_turno (idturno, nombre, codigo, version, idcompania) values (2, 'Turno 2', 'T2', 1, 1);
insert into xpr_turno (idturno, nombre, codigo, version, idcompania) values (3, 'Turno 3', 'T3', 1, 1);

-- adicionar en xpr_produccion idturno, despues de idlinea
alter table xpr_produccion add column idturno bigint after idlinea;

-- adicionar llave foranea en xpr_produccion idturno con tabla xpr_turno
alter table xpr_produccion add foreign key (idturno) references xpr_turno (idturno);
-- adicion en xpr_produccion tipoturno despues de fechafin
alter table xpr_produccion add column tipoturno varchar(50) after fechafin;

-- Actualizando datos
-- update xpr_produccion set idturno = 1 where idturno is null;
-- update xpr_produccion set tipoturno = 'DAY' where tipoturno is null;

-- añadir observacion en xpr_produccion tipo texto despues de id_tmpenc
alter table xpr_produccion add column observacion longtext after id_tmpenc;

-- modificar columna glosa, descri de sf_tmpenc a varchar(1500)
alter table sf_tmpenc modify column glosa varchar(1500);
alter table sf_tmpenc modify column descri varchar(1500);

--

select xp.fecha, pr.codigo, pr.tipoturno, p.cod_art, p.cantidad
from xpr_producto p
join xpr_produccion pr on p.idproduccion = pr.idproduccion
join xpr_plan xp       on pr.idplan      = xp.idplan
;







