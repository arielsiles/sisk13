package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionUlexita;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Servicio para datos especificos de produccion ULEXITA.
 *
 * Encapsula carga, persistencia y consultas mensuales para el reporte
 * diario de produccion ULEXITA.
 */
@Local
public interface XProductionUlexitaService {

    /** Busca el registro ULEXITA asociado a una produccion. null si no existe. */
    XProductionUlexita findByProduction(XProduction production);

    /** Crea o actualiza el registro ULEXITA. */
    void save(XProductionUlexita ulexita);

    /**
     * Lista las producciones de una linea ordenadas por fecha de inicio,
     * para el reporte diario mensual.
     */
    List<XProduction> findProductionsByLineAndMonth(ProductionLine line, int year, int month);

    /**
     * Calcula y persiste TODOS los snapshots de calculos en el registro ULEXITA
     * de una produccion. Se invoca al aprobar (o al re-editar lab data con permiso
     * PRODUCTION_LAB_DATA:UPDATE en orden ya aprobada).
     *
     * Garantiza la inmutabilidad historica: cualquier cambio posterior de
     * mermaFactor, fórmula, articulos o cantidades no afectara los valores
     * reportados para ordenes ya snapshoteadas.
     *
     * @param production produccion a snapshotear
     * @param ulexAvailable saldo de inventario al inicio (puede ser null)
     * @param userCode codigo del usuario (financesCode) que dispara el snapshot
     */
    void persistSnapshots(XProduction production, java.math.BigDecimal ulexAvailable, String userCode);
}
