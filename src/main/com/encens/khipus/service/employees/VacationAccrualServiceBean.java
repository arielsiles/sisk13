package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.VacationPlanning;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import java.util.ArrayList;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("vacationAccrualService")
@AutoCreate
public class VacationAccrualServiceBean extends GenericServiceBean implements VacationAccrualService {

    @In
    private VacationPlanningService vacationPlanningService;

    /* Un plan queda afuera solo si las tres cosas coinciden con lo esperado. Cualquier
       diferencia -antiguedad desactualizada, una gestion que falta, un devengo que falta- lo
       mete en la lista. Los que tienen saldo inicial entran siempre: ahi las gestiones previas
       al corte no existen a proposito y la cuenta no cierra por definicion. */
    private static final String IDS_TO_REVIEW =
            "select p.idplanvacacion from planvacacion p"
                    + " where p.fechasaldoinicial is not null"
                    /* Contra la fecha de cierre y no contra hoy: un plan cerrado quedo al dia
                       el ultimo dia de trabajo y no tiene nada mas que devengar. Sin esto
                       aparece en la lista de pendientes en cada corrida, para siempre. */
                    + "    or p.aniosantiguedad <> timestampdiff(YEAR, p.fechainicio, coalesce(p.fechacierre, curdate()))"
                    + "    or p.aniosantiguedad <> (select count(*) from gestionvacacion g"
                    + "                              where g.idplanvacacion = p.idplanvacacion)"
                    + "    or (select count(*) from gestionvacacion g"
                    + "         where g.idplanvacacion = p.idplanvacacion)"
                    + "       <> (select count(*) from movimientovacacion m"
                    + "            where m.idplanvacacion = p.idplanvacacion and m.tipo = 'ACCRUAL')"
                    + " order by p.idplanvacacion";

    private static final String EMPLOYEES_WITHOUT_PLANNING =
            "select count(distinct c.idempleado) from contratopuesto cp"
                    + " join contrato c on c.idcontrato = cp.idcontrato"
                    + " join empleado e on e.idempleado = c.idempleado"
                    + " where e.fechasalida is null"
                    /* Solo el contrato principal genera vacaciones: los secundarios son trabajo
                       eventual acotado. Sin esto, un contrato adicional aparece como una persona
                       a la que le falta el plan, y nadie deberia crearselo. */
                    + "   and c.principal = 1"
                    /* La FK en planvacacion se llama idconTRACTOpuesto -con typo- y la PK
                       real es idconTRATOpuesto. */
                    + "   and not exists (select 1 from planvacacion p"
                    + "                    where p.idcontractopuesto = cp.idcontratopuesto"
                    + "                      and p.fechacierre is null)";

    @SuppressWarnings({"unchecked"})
    public List<Long> findPlanningIdsToReview() {
        List<Object> rows = getEntityManager().createNativeQuery(IDS_TO_REVIEW).getResultList();
        List<Long> result = new ArrayList<Long>();
        for (Object row : rows) {
            result.add(((Number) row).longValue());
        }
        return result;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public String accrue(Long vacationPlanningId) {
        try {
            VacationPlanning planning = getEntityManager().find(VacationPlanning.class, vacationPlanningId);
            if (null == planning) {
                return "el plan ya no existe";
            }
            /* Se usa la version que exige la regla de antiguedad: si falta el tramo hay que
               enterarse, no seguir de largo. */
            vacationPlanningService.synchronizeVacationDays(planning);
            return null;
        } catch (Exception e) {
            log.error("No se pudo devengar el plan de vacacion #0", e, vacationPlanningId);
            return describe(e);
        }
    }

    public Long countEmployeesWithoutPlanning() {
        Object result = getEntityManager().createNativeQuery(EMPLOYEES_WITHOUT_PLANNING).getSingleResult();
        return null == result ? 0L : ((Number) result).longValue();
    }

    private String describe(Exception e) {
        Throwable cause = e;
        while (null != cause.getCause() && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return (null == message || message.trim().length() == 0)
                ? cause.getClass().getSimpleName() : message;
    }
}
