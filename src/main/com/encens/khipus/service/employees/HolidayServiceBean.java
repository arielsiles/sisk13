package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.SpecialDate;
import com.encens.khipus.model.employees.SpecialDateReason;
import com.encens.khipus.model.employees.SpecialDateTarget;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("holidayService")
@AutoCreate
public class HolidayServiceBean implements HolidayService {

    /**
     * Los feriados que alcanzan a un contrato en un rango.
     * <p/>
     * Se contemplan los tres destinos que maneja `fechaespecial`: la unidad de negocio -el
     * feriado nacional-, la unidad organizacional a la que pertenece su puesto, y la persona.
     * Dejar alguno afuera seria un agujero silencioso: el feriado existiria en la pantalla y el
     * motor lo ignoraria.
     * <p/>
     * La unidad de negocio se busca por DOS caminos: el campo del empleado y, sobre todo, el que
     * sale de su puesto -puesto, unidad organizacional, unidad de negocio-, que es el que usa el
     * resto del sistema. En terdemol los 245 empleados tienen `empleado.idunidadnegocio` en
     * nulo, asi que mirar solo ese campo hacia que un feriado nacional no alcanzara a nadie.
     * <p/>
     * Solo cuentan los de dia completo. Un feriado de medio dia no suspende la jornada; eso es
     * un permiso y se resuelve por otro lado.
     */
    private static final String QUERY =
            "select o from SpecialDate o"
                    + " where o.reason = :reason"
                    + " and o.allDay = :allDay"
                    + " and o.initPeriod <= :to"
                    + " and (o.endPeriod is null or o.endPeriod >= :from)"
                    + " and ("
                    + "   (o.specialDateTarget = :targetEmployee and o.employee = :employee)"
                    + "   or (o.specialDateTarget = :targetBusinessUnit and ("
                    + "       o.businessUnit = :businessUnit"
                    + "       or o.businessUnit in"
                    + "          (select unit.job.organizationalUnit.businessUnit from JobContract unit"
                    + "            where unit.contract = :contract)))"
                    + "   or (o.specialDateTarget = :targetUnit and o.organizationalUnit in"
                    + "       (select jobContract.job.organizationalUnit from JobContract jobContract"
                    + "         where jobContract.contract = :contract))"
                    + " )";

    @In(value = "#{entityManager}")
    private EntityManager em;

    public boolean isHoliday(Contract contract, Date day) {
        if (null == contract || null == day) {
            return false;
        }
        return holidaysBetween(contract, day, day).contains(startOfDay(day).getTime());
    }

    @SuppressWarnings({"unchecked"})
    public Set<Long> holidaysBetween(Contract contract, Date from, Date to) {
        Set<Long> holidays = new HashSet<Long>();
        if (null == contract || null == contract.getEmployee() || null == from || null == to) {
            return holidays;
        }
        Date start = startOfDay(from);
        Date end = startOfDay(to);

        List<SpecialDate> specialDates = em.createQuery(QUERY)
                .setParameter("reason", SpecialDateReason.HOLIDAY)
                .setParameter("allDay", Boolean.TRUE)
                .setParameter("from", start)
                .setParameter("to", end)
                .setParameter("targetEmployee", SpecialDateTarget.EMPLOYEE)
                .setParameter("employee", contract.getEmployee())
                .setParameter("targetBusinessUnit", SpecialDateTarget.BUSINESSUNIT)
                .setParameter("businessUnit", contract.getEmployee().getBusinessUnit())
                .setParameter("targetUnit", SpecialDateTarget.ORGANIZATIONALUNIT)
                .setParameter("contract", contract)
                .getResultList();

        /* Un feriado puede durar mas de un dia -un fin de semana largo-, asi que se expanden
           los dias del rango en lugar de guardar solo la fecha de inicio. */
        for (SpecialDate specialDate : specialDates) {
            Date current = startOfDay(specialDate.getInitPeriod());
            Date last = null == specialDate.getEndPeriod()
                    ? current : startOfDay(specialDate.getEndPeriod());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            while (!calendar.getTime().after(last)) {
                Date day = calendar.getTime();
                if (!day.before(start) && !day.after(end)) {
                    holidays.add(day.getTime());
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return holidays;
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
