/** 27.03.2020 **/
update funcionalidad f set f.`permiso` = 3, f.`nombrerecurso` = 'Functionality.finances.fixedassets.purchaseOrder' where f.`idfuncionalidad` = 100;

-- Functionality.finances.fixedassets.purchaseOrderCause=Motivo de orden de compra
insert into funcionalidad values (259, 'PURCHASEORDERCAUSE', null, 5, 15, 'Functionality.finances.fixedassets.purchaseOrderCause', 1);

-- FIXEDASSETPURCHASEORDERFINALIZE
-- FIXEDASSETPURCHASEORDERAPPROVE
-- FIXEDASSETPURCHASEORDERANNUL
-- FIXEDASSETPURCHASEORDERLIQUIDATE
insert into funcionalidad values (260, 'FIXEDASSETPURCHASEORDERLIQUIDATE', null, 5, 1, 'Functionality.finances.fixedassets.liquidatePurchaseOrder', 1);
-- For ILVA or ? - Caja General CISC M.N.
delete from arcgms where cuenta = '1110110101';
-- 
alter table configuracion add column ctaprovaf varchar(20);
update configuracion set ctaprovaf = '2429910700';

alter table `com_af_detoc` add column idcontratopuesto bigint(20);

