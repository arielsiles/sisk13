package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;

import javax.ejb.Local;
import java.util.List;

/**
 * Proceso de actualizacion de devengos.
 * <p/>
 * El devengo no puede depender de que alguien abra un plan y lo guarde. Este proceso lo corre
 * RRHH cuando quiere, y su regla es no dejar a nadie afuera: no busca "quien cumplio anios hoy"
 * sino <b>cualquier plan cuyo estado no coincida con lo que deberia ser</b>. Si algo no se puede
 * devengar, se reporta; nunca se saltea en silencio.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface VacationAccrualService extends GenericService {

    /**
     * Planes a revisar. El criterio es conservador a proposito: ante cualquier duda el plan
     * entra. Se saltea uno solo cuando las tres cosas coinciden -antiguedad, cantidad de
     * gestiones y cantidad de devengos-, o sea cuando no hay nada que hacer.
     */
    List<Long> findPlanningIdsToReview();

    /**
     * Sincroniza un plan.
     *
     * @return null si quedo al dia, o el motivo por el que no se pudo devengar
     */
    String accrue(Long vacationPlanningId);

    /**
     * Empleados con contrato de puesto que no tienen plan: no devengan porque no hay donde.
     * Se cuentan para avisar, porque son justamente los que se escaparian.
     */
    Long countEmployeesWithoutPlanning();
}
