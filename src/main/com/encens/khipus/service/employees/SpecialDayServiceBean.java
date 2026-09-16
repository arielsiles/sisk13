package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.SpecialDate;
import com.encens.khipus.model.employees.SpecialDateReason;
import com.encens.khipus.model.employees.SpecialDateTarget;
import com.encens.khipus.model.employees.SpecialDateType;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ExcusedDay;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("specialDayService")
@AutoCreate
public class SpecialDayServiceBean implements SpecialDayService {

    /**
     * A quien alcanza una fecha especial. Se contemplan los tres destinos que maneja
     * `fechaespecial`: la unidad de negocio -el feriado nacional-, la unidad organizacional a la
     * que pertenece su puesto, y la persona. Dejar alguno afuera seria un agujero silencioso: la
     * fecha existiria en la pantalla y el motor la ignoraria.
     * <p/>
     * La unidad de negocio se busca por DOS caminos: el campo del empleado y, sobre todo, el que
     * sale de su puesto -puesto, unidad organizacional, unidad de negocio-, que es el que usa el
     * resto del sistema. En terdemol los 245 empleados tienen `empleado.idunidadnegocio` en
     * nulo, asi que mirar solo ese campo hacia que un feriado nacional no alcanzara a nadie.
     * <p/>
     * Solo cuentan los de dia completo. Una fecha de medio dia no suspende ni perdona la jornada;
     * eso es un permiso acotado y se resuelve por otro lado -E2.6-.
     */
    private static final String TARGETED =
            "select o from SpecialDate o"
                    + " where o.allDay = :allDay"
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

    private static final String HOLIDAYS = TARGETED + " and o.reason = :reason";

    /**
     * Todo lo que NO es feriado. El motivo nulo entra: las filas cargadas antes de la 6.1.0 no lo
     * tienen, y el motor viejo perdonaba toda fecha especial de dia completo sin mirar el motivo.
     * Excluirlas seria empezar a cobrar dias que hasta ayer estaban perdonados.
     */
    private static final String EXCUSED = TARGETED + " and (o.reason is null or o.reason <> :reason)";

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
        if (!addressable(contract, from, to)) {
            return holidays;
        }
        List<SpecialDate> specialDates = targeted(HOLIDAYS, contract, from, to)
                .setParameter("reason", SpecialDateReason.HOLIDAY)
                .getResultList();
        for (SpecialDate specialDate : specialDates) {
            holidays.addAll(daysOf(specialDate, from, to));
        }
        return holidays;
    }

    @SuppressWarnings({"unchecked"})
    public Map<Long, ExcusedDay> excusedBetween(Contract contract, Date from, Date to) {
        Map<Long, ExcusedDay> excused = new HashMap<Long, ExcusedDay>();
        if (!addressable(contract, from, to)) {
            return excused;
        }
        List<SpecialDate> specialDates = targeted(EXCUSED, contract, from, to)
                .setParameter("reason", SpecialDateReason.HOLIDAY)
                .getResultList();
        for (SpecialDate specialDate : specialDates) {
            /* Sin goce gana: si un dia esta cubierto por dos fechas especiales y una no se paga,
               el resultado no puede ser que se pague. Lo contrario abriria la puerta a tapar una
               licencia sin goce cargando cualquier permiso encima. */
            ExcusedDay day = new ExcusedDay(specialDate.getReason(), specialDate.getTitle(),
                    !SpecialDateType.UNPAID.equals(specialDate.getCredit()));
            for (Long key : daysOf(specialDate, from, to)) {
                ExcusedDay current = excused.get(key);
                if (null == current || (current.isPaid() && !day.isPaid())) {
                    excused.put(key, day);
                }
            }
        }
        return excused;
    }

    private boolean addressable(Contract contract, Date from, Date to) {
        return null != contract && null != contract.getEmployee() && null != from && null != to;
    }

    private Query targeted(String query, Contract contract, Date from, Date to) {
        return em.createQuery(query)
                .setParameter("allDay", Boolean.TRUE)
                .setParameter("from", startOfDay(from))
                .setParameter("to", startOfDay(to))
                .setParameter("targetEmployee", SpecialDateTarget.EMPLOYEE)
                .setParameter("employee", contract.getEmployee())
                .setParameter("targetBusinessUnit", SpecialDateTarget.BUSINESSUNIT)
                .setParameter("businessUnit", contract.getEmployee().getBusinessUnit())
                .setParameter("targetUnit", SpecialDateTarget.ORGANIZATIONALUNIT)
                .setParameter("contract", contract);
    }

    /**
     * Los dias del rango que cubre una fecha especial. Una fecha puede durar mas de un dia -un
     * fin de semana largo, una vacacion de dos semanas-, asi que se expanden en lugar de guardar
     * solo la fecha de inicio.
     */
    private Set<Long> daysOf(SpecialDate specialDate, Date from, Date to) {
        Set<Long> days = new HashSet<Long>();
        Date start = startOfDay(from);
        Date end = startOfDay(to);
        Date current = startOfDay(specialDate.getInitPeriod());
        Date last = null == specialDate.getEndPeriod()
                ? current : startOfDay(specialDate.getEndPeriod());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        while (!calendar.getTime().after(last)) {
            Date day = calendar.getTime();
            if (!day.before(start) && !day.after(end)) {
                days.add(day.getTime());
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
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
