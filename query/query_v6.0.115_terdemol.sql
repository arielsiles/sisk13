-- ============================================================================
-- v6.0.115 :: Eliminacion del feature "Solicitudes de pago de Materia Prima"
-- ============================================================================
--
--  Feature antiguo y sin uso (RawMaterialPayment). Se eliminan sus tablas,
--  permisos y filas de secuencia. NO afecta a "Acopio de Materia Prima":
--  las tablas de acopio (acopiomp/CollectMaterial, productormateriaprima/
--  RawMaterialProducer, precioproductormp/ProducerPrice) NO se tocan; solo se
--  borran las tablas de pago, que dependian de ellas (FK saliente).
--
--  Ejecutar con el sistema DETENIDO, junto con el deploy del nuevo codigo.
--  Feature nunca usado: DROP directo (sin respaldo).
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- A) Tablas del feature (orden hijo -> padre por las FK a pagoacopiomp)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS detallepagoacopiomp;   -- RawMaterialPaymentDetail
DROP TABLE IF EXISTS pagoparcialacopiomp;   -- PartialPaymentRawMaterial
DROP TABLE IF EXISTS descuentoacopiomp;     -- RawMaterialDiscount   (FK -> tipodescuentoacopiomp)
DROP TABLE IF EXISTS tipodescuentoacopiomp; -- RawMaterialDiscountT
DROP TABLE IF EXISTS pagoacopiomp;          -- RawMaterialPayment


-- ----------------------------------------------------------------------------
-- B) Permisos / funcionalidades (y sus grants por rol en derechoacceso)
-- ----------------------------------------------------------------------------
--  RAWMATERIALPAYMENTREQUEST  = funcionalidad 284 (query_v5.8.4)
--  PAYMENT_RAWMATERIALPRODUCER = funcionalidad 288 (query_v5.8.7)
--  Se borra por codigo (robusto ante ids distintos entre bases).

DELETE FROM derechoacceso
 WHERE idfuncionalidad IN (
       SELECT idfuncionalidad FROM funcionalidad
        WHERE codigo IN ('RAWMATERIALPAYMENTREQUEST', 'PAYMENT_RAWMATERIALPRODUCER')
 );

DELETE FROM funcionalidad
 WHERE codigo IN ('RAWMATERIALPAYMENTREQUEST', 'PAYMENT_RAWMATERIALPRODUCER');


-- ----------------------------------------------------------------------------
-- C) Filas de correlativos en 'secuencia' (las creaba el @TableGenerator)
-- ----------------------------------------------------------------------------
DELETE FROM secuencia
 WHERE tabla IN (
       'pagoacopiomp',
       'detallepagoacopiomp',
       'pagoparcialacopiomp',
       'descuentoacopiomp',
       'tipodescuentoacopiomp'
 );
