-- ============================================================================
-- v6.0.74 :: Tabla de auditoria de ajustes de inventario
--             para la nueva pantalla "Almacenes > Configuracion >
--             Actualizar Inventario".
-- ============================================================================
--
-- Registra cada ajuste manual aplicado sobre inv_inventario / inv_articulos
-- por la herramienta de reconciliacion de saldos. Guarda valores antes/despues
-- de las 3 magnitudes (saldo_uni, costo_uni, saldo_mon), motivo, usuario,
-- fechas del calculo y fecha del ajuste.
-- ----------------------------------------------------------------------------

CREATE TABLE inv_ajuste_saldo (
    id_ajuste            BIGINT       NOT NULL AUTO_INCREMENT,
    no_cia               VARCHAR(2)   NOT NULL,
    cod_alm              VARCHAR(6)   NOT NULL,
    cod_art              VARCHAR(6)   NOT NULL,
    fecha_ajuste         DATETIME     NOT NULL,
    fecha_inicio_calc    DATE         NOT NULL,
    fecha_fin_calc       DATE         NOT NULL,
    saldo_uni_anterior   DECIMAL(16,2),
    saldo_uni_nuevo      DECIMAL(16,2),
    costo_uni_anterior   DECIMAL(16,6),
    costo_uni_nuevo      DECIMAL(16,6),
    saldo_mon_anterior   DECIMAL(20,6),
    saldo_mon_nuevo      DECIMAL(20,6),
    motivo               VARCHAR(500) NOT NULL,
    no_usr               VARCHAR(4),
    PRIMARY KEY (id_ajuste),
    KEY ix_ajuste_art (no_cia, cod_alm, cod_art, fecha_ajuste)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Permiso nuevo INVENTORYRECONCILIATION
-- Uso: s:hasPermission('INVENTORYRECONCILIATION', 'VIEW' | 'UPDATE')
-- idmodulo = 5 (warehouse, alineado con PRODUCTINVENTORYSTART, GROUP, etc.)
-- permiso bitmask = VIEW(1) + UPDATE(4) = 5
-- Columnas funcionalidad: (idfuncionalidad, codigo, descripcion, idmodulo,
--                          permiso, nombrerecurso, idcompania)
-- ----------------------------------------------------------------------------
insert into funcionalidad values (452, 'INVENTORYRECONCILIATION', 'Actualizar Inventario - reconciliar saldos y costo promedio por articulo', 5, 5, 'menu.warehouse.configuration.inventoryReconciliation', 1);

delete from inv_inicio where idinvinicio = 2061;

update inv_inventario i set i.cod_alm = 1 where i.cod_art = 1476;
update inv_inventario_detalle i set i.cod_alm = 1 where i.cod_art = 1476;

-- ----------------------------------------------------------------------------
-- Correccion datos OC-INV-1236 (id_com_encoc=1344, vale no_trans=10193,
-- asiento id_tmpenc=4572) - articulo 1476 BIDON 60LITROS:
-- cantidad y costo unitario estaban invertidos (cant=220 cost=1 -> cant=1 cost=220).
-- Los totales monetarios no cambian porque cantidad x costo = 220 en ambos casos.
-- La compra fue CON FACTURA, por eso costounitario es el neto (* 0.87).
-- ----------------------------------------------------------------------------

-- 1) Detalle de la orden de compra
update com_detoc
   set cant_sol  = 1,
       costo_uni = 220.000000
 where id_com_detoc = 2251;

-- 2) Detalle del vale de almacen (entrada)
update inv_movdet
   set cantidad         = 1,
       preciounitcompra = 220.000000,
       costounitario    = 191.400000
 where id_inv_movdet = 11585;

-- 3) Detalle del asiento contable
update sf_tmpdet
   set cant_art = 1
 where id_tmpdet = 33651;

-- ----------------------------------------------------------------------------
-- Correccion datos OC-INV-1238 (id_com_encoc=1347, vale no_trans=10245,
-- movdet id=11692) - articulo 1477 PECERA DE VIDRIO:
-- La OC y el vale se generaron contra cod_alm=2, pero el articulo vive en
-- cod_alm=1 (LABORATORIO) segun inv_articulos / inv_inicio / inv_inventario.
-- Hay que reasignar cod_alm de 2 -> 1 en las 3 tablas que registran el almacen.
-- ----------------------------------------------------------------------------

-- 1) Header de la orden de compra
update com_encoc
   set cod_alm = 1
 where id_com_encoc = 1347;

-- 2) Header del vale de almacen
update inv_vales
   set cod_alm = 1
 where no_trans = '10245' and no_cia = '01';

-- 3) Detalle del movimiento
update inv_movdet
   set cod_alm = 1
 where id_inv_movdet = 11692;

-- ----------------------------------------------------------------------------
-- Falta inv_inicio[2026] para articulo 1477 en alm=1.
-- Cuando se corrio "Iniciar Inventario Anual" al cierre 2025, el sistema vio
-- saldo 0 para 1477 en alm=1 porque el vale 10245 estaba mal asignado a alm=2.
-- Con la correccion anterior (vale movido de alm=2 -> alm=1), el saldo real
-- al cierre 2025 es 1 unidad a costo 102.0000.
-- Sin esta fila, el reporte general no incluye al articulo cuando se filtra
-- desde 2026, generando alerta de divergencia en la pantalla de reconciliacion.
-- ----------------------------------------------------------------------------
insert into inv_inicio (idinvinicio, cod_art, nombre, cantidad, und, alm, costo_uni, gestion, no_cia, cantx)
values (939, '1477', 'PECERA DE VIDRIO', 1.00, 'UN', '1', 102.000000, '2026', '01', NULL);



