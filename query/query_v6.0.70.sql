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
-- idmodulo = 5 (finances) para alinearse con las demas funcionalidades
-- WAREHOUSE* (WAREHOUSEVOUCHER, WAREHOUSEPURCHASEORDER*, etc.).
-- Columnas de la tabla: (idfuncionalidad, codigo, descripcion, idmodulo, permiso,
--                        nombrerecurso, idcompania).
-- ----------------------------------------------------------------------------
insert into funcionalidad values (318, 'WAREHOUSEPURCHASEORDERREVERSE', null, 5, 1, 'menu.warehouse.purchaseOrder.reverse', 1);
insert into funcionalidad values (319, 'WAREHOUSEVOUCHERREVERSE',      null, 5, 1, 'menu.warehouse.voucher.reverse',      1);

-- Asignacion por defecto al rol Administrador (idrol=1).
-- derechoacceso tiene PK compuesta idfuncionalidad+idrol y NO requiere secuencia.
-- idmodulo debe coincidir con el de la funcionalidad (5) porque el named query
-- AccessRight.findByUser filtra por ar.companyModule.active y requiere join no-null.
insert into derechoacceso(idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (318, 1, 1, 1, 5);
insert into derechoacceso(idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (319, 1, 1, 1, 5);

-- Actualizar secuencia de funcionalidad al ultimo id usado
update secuencia set valor=(select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla='funcionalidad';
