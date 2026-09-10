package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.VacationPlanning;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.VacationPlanningBulkResult;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface VacationPlanningService extends GenericService {

    VacationPlanning load(VacationPlanning vacationPlanning) throws EntryNotFoundException;

    void createVacationPlanning(VacationPlanning vacationPlanning) throws EntryDuplicatedException;

    void updateVacationPlanning(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException;

    void deleteVacationPlanning(VacationPlanning vacationPlanning) throws ConcurrencyException, ReferentialIntegrityException;

    void synchronizeVacationDays(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException, VacationRuleUndefinedYearException;

    void synchronizeVacationDaysIgnoreUndefinedVacationRule(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException;

    /**
     * Crea el plan de vacacion de un contrato de puesto recien dado de alta, si corresponde.
     * No hace nada si la empresa no tiene reglas de vacacion cargadas -no usa el modulo-, si
     * el empleado ya tiene plan, o si la fecha de inicio del contrato no sirve.
     * <p/>
     * La condicion es que no tenga plan <b>el empleado</b> y no <b>este contrato de puesto</b>:
     * de lo contrario un cambio de puesto le crearia un segundo plan, con la antiguedad de
     * vuelta en cero y el saldo partido en dos.
     *
     * @return true si creo el plan
     */
    boolean createIfAbsent(JobContract jobContract);

    /**
     * Arma -sin escribir nada- los planes que faltan: uno por contrato de puesto de empleado
     * activo que todavia no tenga plan, con la fecha de inicio del contrato como arranque de
     * la antiguedad. Los que no se pueden armar salen aparte con el motivo.
     */
    /**
     * Cierra los planes de un contrato al ultimo dia de trabajo, dejandolos al dia a esa fecha.
     * Un plan cerrado deja de devengar: sin esto el plan de alguien que se fue sigue sumando
     * anios para siempre.
     *
     * @return cuantos planes se cerraron
     */
    int closePlansOfContract(Long contractId, java.util.Date closeDate);

    VacationPlanningBulkResult analyzeBulkGeneration();

    /**
     * Persiste lo que devolvio el analisis y sincroniza cada plan, que es lo que devenga los
     * dias de cada gestion.
     *
     * @return cuantos planes se crearon
     */
    int confirmBulkGeneration(VacationPlanningBulkResult result) throws Exception;
}
