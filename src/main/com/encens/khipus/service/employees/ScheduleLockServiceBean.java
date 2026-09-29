package com.encens.khipus.service.employees;

import com.encens.khipus.exception.employees.ClosedPeriodException;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.WorkShift;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("scheduleLockService")
@AutoCreate
public class ScheduleLockServiceBean implements ScheduleLockService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    /**
     * Se toma el fin de periodo mas alto entre las planillas oficiales. Los periodos de planilla
     * son mensuales y consecutivos, asi que ese maximo es la frontera del cierre: preguntar por
     * cada periodo por separado daria el mismo resultado con muchas mas consultas.
     */
    public Date lastClosedDate() {
        return (Date) em.createQuery(
                "select max(gestionPayroll.endDate) from GeneratedPayroll generatedPayroll"
                        + " join generatedPayroll.gestionPayroll gestionPayroll"
                        + " where generatedPayroll.generatedPayrollType = :type")
                .setParameter("type", GeneratedPayrollType.OFFICIAL)
                .getSingleResult();
    }

    public boolean isClosed(Date day) {
        if (null == day) {
            return false;
        }
        Date closedUntil = lastClosedDate();
        return null != closedUntil && !day.after(closedUntil);
    }

    public void checkOpen(Date day) throws ClosedPeriodException {
        Date closedUntil = lastClosedDate();
        if (null != closedUntil && null != day && !day.after(closedUntil)) {
            throw new ClosedPeriodException(closedUntil);
        }
    }

    public boolean isWorkShiftLocked(WorkShift workShift) {
        if (null == workShift || null == workShift.getId()) {
            return false;
        }
        Date closedUntil = lastClosedDate();
        if (null == closedUntil) {
            return false;
        }
        Long used = (Long) em.createQuery(
                "select count(o) from GroupScheduleDay o"
                        + " where o.workShift = :workShift and o.day <= :closedUntil")
                .setParameter("workShift", workShift)
                .setParameter("closedUntil", closedUntil)
                .getSingleResult();
        if (null != used && used > 0) {
            return true;
        }
        Long inExceptions = (Long) em.createQuery(
                "select count(o) from ScheduleException o"
                        + " where o.workShift = :workShift and o.date <= :closedUntil")
                .setParameter("workShift", workShift)
                .setParameter("closedUntil", closedUntil)
                .getSingleResult();
        return null != inExceptions && inExceptions > 0;
    }
}
