package com.encens.khipus.action.employees;

import com.encens.khipus.service.employees.VacationAccrualService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Actualizacion de devengos, lanzada a mano por RRHH.
 * <p/>
 * El avance lo maneja la propia barra de progreso: cada tic procesa un lote y devuelve el
 * porcentaje. No hay hilos ni tareas programadas, asi que no hay nada que se pueda colgar ni
 * quedar a medias; si el usuario cierra la pantalla, lo procesado ya esta guardado y lo que
 * falta se toma en la corrida siguiente.
 *
 * @author
 * @version 6.1.0
 */
@Name("vacationAccrualAction")
@Scope(ScopeType.SESSION)
public class VacationAccrualAction implements Serializable {

    /* Lote por tic. Chico a proposito: cada tic es una transaccion corta y la barra avanza. */
    private static final int BATCH_SIZE = 20;

    @In
    private VacationAccrualService vacationAccrualService;

    private List<Long> pending = new ArrayList<Long>();
    private int total = 0;
    private int done = 0;
    private int updated = 0;
    private List<String> failures = new ArrayList<String>();
    private Long employeesWithoutPlanning = 0L;
    private boolean running = false;
    private boolean finished = false;

    @Restrict("#{s:hasPermission('VACATIONPLANNING','UPDATE')}")
    public void start() {
        pending = vacationAccrualService.findPlanningIdsToReview();
        employeesWithoutPlanning = vacationAccrualService.countEmployeesWithoutPlanning();
        total = pending.size();
        done = 0;
        updated = 0;
        failures = new ArrayList<String>();
        finished = total == 0;
        running = total > 0;
    }

    /**
     * Un lote por invocacion. La barra de progreso llama a esto hasta que no queda nada.
     */
    @Restrict("#{s:hasPermission('VACATIONPLANNING','UPDATE')}")
    public void step() {
        if (!running) {
            return;
        }
        int processed = 0;
        while (!pending.isEmpty() && processed < BATCH_SIZE) {
            Long id = pending.remove(0);
            String failure = vacationAccrualService.accrue(id);
            if (null == failure) {
                updated++;
            } else if (failures.size() < 100) {
                failures.add(id + ": " + failure);
            }
            processed++;
            done++;
        }
        if (pending.isEmpty()) {
            running = false;
            finished = true;
        }
    }

    public void clear() {
        pending = new ArrayList<Long>();
        total = 0;
        done = 0;
        updated = 0;
        failures = new ArrayList<String>();
        running = false;
        finished = false;
    }

    public int getProgress() {
        return total == 0 ? 100 : (done * 100) / total;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getTotal() {
        return total;
    }

    public int getDone() {
        return done;
    }

    public int getUpdated() {
        return updated;
    }

    public List<String> getFailures() {
        return failures;
    }

    public int getFailureCount() {
        return failures.size();
    }

    public Long getEmployeesWithoutPlanning() {
        return employeesWithoutPlanning;
    }
}
