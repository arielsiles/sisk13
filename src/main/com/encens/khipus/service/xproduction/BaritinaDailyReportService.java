package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProduction;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Consultas agregadas para el Reporte Diario de Produccion BARITINA.
 *
 * Reune datos de tres fuentes:
 *  - ordenes de produccion BARITINA (xpr_produccion de la linea)
 *  - acopio de materia prima (CollectMaterial / acopiomp) por articulo
 *  - despachos (WarehouseVoucherDispatch / inv_valedespacho) por articulo
 */
@Local
public interface BaritinaDailyReportService {

    /** Ordenes de la linea con initDate en [from, to). */
    List<XProduction> findProductions(ProductionLine line, Date from, Date to);

    /** Acopio (KG, estados APR/CONTA) del articulo agrupado por dia en [from, to). Filas [Date, BigDecimal]. */
    List<Object[]> sumAcopioByDay(String codArt, Date from, Date to);

    /** Total acopiado (KG) del articulo antes de :before. */
    BigDecimal sumAcopioBefore(String codArt, Date before);

    /** Despachos (cantidad, estados APROBADO/FINALIZADO) de los articulos en [from, to). Filas [Date fechaDespacho, BigDecimal cantidad]. */
    List<Object[]> dispatchRows(Collection<String> codArts, Date from, Date to);

    /** Total despachado de los articulos antes de :before. */
    BigDecimal sumDispatchBefore(Collection<String> codArts, Date before);

    /**
     * Movimientos de inventario por vale (ajustes) de los articulos en [from, to), EXCLUYENDO
     * los vales generados por un despacho: esos ya se reportan en la columna DESPACHO.
     *
     * El reporte diario no arrastra estos movimientos en sus saldos (sus columnas son acopio,
     * produccion y despacho); se listan para dejarlos visibles en OBSERVACIONES y explicar por
     * que el saldo del reporte puede separarse del de "Saldos de Almacen".
     *
     * Filas [Date fecha, MovementDetailType tipo, BigDecimal cantidad, String codArt, String noTrans].
     */
    List<Object[]> adjustmentRows(Collection<String> codArts, Date from, Date to);
}
