package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionBaritina;
import com.encens.khipus.model.xproduction.XProductionBaritinaZona;

import javax.ejb.Local;
import java.util.List;

/**
 * Servicio para datos especificos de produccion BARITINA.
 *
 * Encapsula carga y persistencia de la cabecera (1 a 1 con la produccion) y de
 * la distribucion por zonas productivas (N filas por produccion).
 */
@Local
public interface XProductionBaritinaService {

    /** Busca la cabecera BARITINA asociada a una produccion. null si no existe. */
    XProductionBaritina findByProduction(XProduction production);

    /** Crea o actualiza la cabecera BARITINA. */
    void save(XProductionBaritina baritina);

    /** Lista las filas de distribucion por zona de una produccion. */
    List<XProductionBaritinaZona> findZonasByProduction(XProduction production);

    /** Crea o actualiza una fila de distribucion por zona. */
    void saveZona(XProductionBaritinaZona zona);

    /** Elimina una fila de distribucion por zona (si esta persistida). */
    void removeZona(XProductionBaritinaZona zona);

    /** Elimina cabecera y zonas de una produccion (limpieza al borrar la orden). */
    void deleteByProduction(XProduction production);
}
