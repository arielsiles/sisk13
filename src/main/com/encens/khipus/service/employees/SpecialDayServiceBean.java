package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.SpecialDate;
import com.encens.khipus.model.employees.SpecialDateReason;
import com.encens.khipus.model.employees.SpecialDateTarget;
import com.encens.khipus.model.employees.SpecialDateType;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ExcusedDay;
import com.encens.khipus.util.employees.attendance.ExcusedInterval;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
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
     * `allDay` viene como parametro: las de dia completo suspenden o perdonan la jornada entera,
     * las parciales justifican un tramo de horas.
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

    /** Los permisos por horas: los que NO son de dia completo. El feriado no entra: es de dia. */
    private static final String PARTIAL = TARGETED;

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

    @SuppressWarnings({"unchecked"})
    public Map<Long, List<ExcusedInterval>> excusedIntervalsBetween(Contract contract, Date from, Date to) {
        Map<Long, List<ExcusedInterval>> intervals = new HashMap<Long, List<ExcusedInterval>>();
        if (!addressable(contract, from, to)) {
            return intervals;
        }
        List<SpecialDate> specialDates = em.createQuery(PARTIAL)
                .setParameter("allDay", Boolean.FALSE)
                .setParameter("from", startOfDay(from))
                .setParameter("to", startOfDay(to))
                .setParameter("targetEmployee", SpecialDateTarget.EMPLOYEE)
                .setParameter("employee", contract.getEmployee())
                .setParameter("targetBusinessUnit", SpecialDateTarget.BUSINESSUNIT)
                .setParameter("businessUnit", contract.getEmployee().getBusinessUnit())
                .setParameter("targetUnit", SpecialDateTarget.ORGANIZATIONALUNIT)
                .setParameter("contract", contract)
                .getResultList();

        for (SpecialDate specialDate : specialDates) {
            if (null == specialDate.getStartTime() || null == specialDate.getEndTime()) {
                /* Sin horas no hay tramo que perdonar. Una fecha parcial sin horario es un dato
                   incompleto, y adivinar cual era el tramo seria regalar minutos. */
                continue;
            }
            for (Long key : daysOf(specialDate, from, to)) {
                List<ExcusedInterval> ofDay = intervals.get(key);
                if (null == ofDay) {
                    ofDay = new ArrayList<ExcusedInterval>();
                    intervals.put(key, ofDay);
                }
                Date start = at(new Date(key), specialDate.getStartTime());
                Date end = at(new Date(key), specialDate.getEndTime());
                if (!end.after(start)) {
                    /* El fin antes que el inicio solo puede significar que el tramo termina al dia
                       siguiente: 23:30 a 03:30 del turno de noche. Sin esto el tramo quedaba vacio
                       y el permiso no perdonaba nada. */
                    end = addDays(end, 1);
                }
                ofDay.add(new ExcusedInterval(start, end));
            }
        }
        return intervals;
    }

    /**
     * La hora de un permiso, puesta sobre el dia que corresponde.
     * <p/>
     * `fechaespecial` guarda el horario como hora suelta y el rango como fechas: un permiso de
     * 14:00 a 18:00 del lunes al miercoles son tres tramos, uno por dia.
     */
    private static Date at(Date day, Date hour) {
        Calendar result = Calendar.getInstance();
        result.setTime(startOfDay(day));
        Calendar time = Calendar.getInstance();
        time.setTime(hour);
        result.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        result.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);
        return result.getTime();
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
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
