-- ============================================================================
-- v6.0.70 :: Anulacion/Reversion de Ordenes de Compra y Vales de Almacen
-- Fase 0 - Preparacion de esquema, permisos e i18n
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0.1 Columnas de auditoria de anulacion en com_encoc (PurchaseOrder)
-- ----------------------------------------------------------------------------
alter table com_encoc add column fecha_anul   datetime     null;
alter table com_encoc add column usuario_anul varchar(4)   null;
alter table com_encoc add column motivo_anul  varchar(250) null;

-- ----------------------------------------------------------------------------
-- 0.2 Columnas de auditoria de anulacion en inv_vales (WarehouseVoucher)
-- ----------------------------------------------------------------------------
alter table inv_vales add column fecha_anul   datetime     null;
alter table inv_vales add column usuario_anul varchar(4)   null;
alter table inv_vales add column motivo_anul  varchar(250) null;

-- ----------------------------------------------------------------------------
-- 0.4 Permisos nuevos (funcionalidades) para anular/revertir OC y Vale
-- Uso: s:hasPermission('<codigo>', 'VIEW')
-- ----------------------------------------------------------------------------
insert into funcionalidad values (318, 'WAREHOUSEPURCHASEORDERREVERSE', null, 7, 1, 'menu.warehouse.purchaseOrder.reverse', 1);
insert into funcionalidad values (319, 'WAREHOUSEVOUCHERREVERSE',      null, 7, 1, 'menu.warehouse.voucher.reverse',      1);

-- Asignacion por defecto al rol Administrador (idrol=1)
insert into derechoacceso(idfuncionalidad, idrol, permiso, idcompania) values (318, 1, 7, 1);
insert into derechoacceso(idfuncionalidad, idrol, permiso, idcompania) values (319, 1, 7, 1);
