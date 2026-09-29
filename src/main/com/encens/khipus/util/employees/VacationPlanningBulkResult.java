package com.encens.khipus.util.employees;

import com.encens.khipus.model.employees.VacationPlanning;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de analizar la generacion de planes de vacacion, antes de confirmarla.
 * <p/>
 * El analisis no escribe nada: deja armados los planes que se crearian y separa los que no se
 * pueden crear, con el motivo. RRHH ve que va a pasar y recien despues confirma.
 *
 * @author
 * @version 6.1.0
 */
public class VacationPlanningBulkResult {

    /** Planes listos para persistir. */
    private List<VacationPlanning> newPlannings = new ArrayList<VacationPlanning>();

    /** Empleados que ya tenian plan: no se tocan. */
    private int alreadyPlanned = 0;

    /** Los que quedan afuera, con el motivo, para poder corregirlos. */
    private List<String> excluded = new ArrayList<String>();

    public boolean hasSomethingToCreate() {
        return !newPlannings.isEmpty();
    }

    public void addNewPlanning(VacationPlanning planning) {
        newPlannings.add(planning);
    }

    public void addAlreadyPlanned() {
        alreadyPlanned++;
    }

    public void addExcluded(String employeeName, String reason) {
        excluded.add(employeeName + " - " + reason);
    }

    public List<VacationPlanning> getNewPlannings() {
        return newPlannings;
    }

    public int getNewPlanningCount() {
        return newPlannings.size();
    }

    public int getAlreadyPlanned() {
        return alreadyPlanned;
    }

    public List<String> getExcluded() {
        return excluded;
    }

    public int getExcludedCount() {
        return excluded.size();
    }
}
