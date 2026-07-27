-- ============================================================================
-- v6.0.117 :: Cuenta por cobrar del proveedor (CRUD de Proveedores estilo Odoo)
-- ============================================================================
--
--  El proveedor ya tenia la Cuenta por pagar (columna `ctaxpagar`). Se agrega la
--  Cuenta por cobrar como columna nueva `ctaxcobrar` en `cxp_proveedores`, para
--  poder configurarla desde la pantalla de alta/edicion de proveedor
--  (provider.xhtml). Es opcional (NULL) y sin default: el usuario la elige a mano.
--
--  El mapeo JPA de Provider ya referencia `ctaxcobrar`; sin esta columna, hasta el
--  listado y la edicion de proveedores fallarian. EJECUTAR ESTE SCRIPT ANTES de
--  desplegar el nuevo codigo (o junto al deploy, con el sistema detenido).
-- ----------------------------------------------------------------------------

ALTER TABLE `cxp_proveedores`
    ADD COLUMN `ctaxcobrar` VARCHAR(20) NULL AFTER `ctaxpagar`;
