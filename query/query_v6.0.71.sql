-- ============================================================================
-- v6.0.71 :: Eliminar columnas cu y ct de inv_articulos
--            y limpiar el reporte muerto productItemCostUnitReport.
-- ============================================================================
--
-- Motivo:
--  Los campos cu/ct de inv_articulos eran acumuladores paralelos a
--  costo_uni/saldo_mon con formulas distintas, que en salidas genericas no se
--  descontaban nunca (bug heredado). Resultado: valores desalineados y
--  decenas/cientos de articulos con ct negativo. Toda la logica de calculo
--  correcto vive en costo_uni (ProductItem.unitCost) y saldo_mon
--  (ProductItem.investmentAmount), que se conservan.
--
--  El reporte ProductItemCostUnitReport estaba oculto del menu (todas sus
--  entradas comentadas) y era el unico que visualizaba cu. Se elimina su
--  funcionalidad/permiso.
-- ----------------------------------------------------------------------------

alter table inv_articulos drop column cu;
alter table inv_articulos drop column ct;

-- Limpieza del permiso del reporte eliminado
delete from derechoacceso where idfuncionalidad
    in (select idfuncionalidad from funcionalidad where codigo = 'PRODUCTITEMCOSTUNITREPORT');
delete from funcionalidad where codigo = 'PRODUCTITEMCOSTUNITREPORT';
