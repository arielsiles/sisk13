package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.ContractWorkShift;
import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.ScheduleException;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ExcusedInterval;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolucion de la jornada por las cuatro capas.
 *
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("journeyResolverService")
@AutoCreate
public class JourneyResolverServiceBean implements JourneyResolverService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private WorkGroupService workGroupService;

    @In
    private SpecialDayService specialDayService;

    public ScheduledJourney resolve(Contract contract, Date day) {
        if (null == contract || null == day) {
            return null;
        }
        Date date = startOfDay(day);

        /* 1. La excepcion gana siempre, incluso sobre un cronograma publicado. */
        ScheduleException exception = findException(contract, date);
        if (null != exception) {
            return journeyOf(date, exception.getWorkShift());
        }

        /* 2. El cronograma del grupo al que pertenecia ESE dia -no al que pertenece hoy-.
              Solo cuentan los dias publicados: uno en borrador todavia no es una decision. */
        WorkGroupMembership membership = workGroupService.findMembership(contract, date);
        if (null != membership) {
            List<GroupScheduleDay> scheduleDays = em
                    .createNamedQuery("GroupScheduleDay.findByGroupAndDay")
                    .setParameter("workGroup", membership.getWorkGroup())
                    .setParameter("day", date)
                    .getResultList();
            if (!scheduleDays.isEmpty()) {
                GroupScheduleDay scheduleDay = (GroupScheduleDay) scheduleDays.get(0);
                if (scheduleDay.isPublished()) {
                    return journeyOf(date, scheduleDay.getWorkShift());
                }
                /* Publicado no esta: no se cae al horario fijo del contrato, porque quien
                   pertenece a un grupo no tiene horario fijo y caer ahi seria inventar una
                   jornada. El dia queda sin evaluar hasta que se publique. */
                return null;
            }
            return null;
        }

        /* 3. El horario fijo del contrato, salvo que sea feriado.
              El feriado suprime lo que sale de una REGLA -"todos los jueves"- pero no lo que
              alguien decidio a mano: la excepcion y el cronograma del grupo ya se resolvieron
              arriba y no llegan aca. Una regla de calendario no puede pisar una decision. */
        ContractWorkShift contractShift = findContractShift(contract, date);
        if (null != contractShift) {
            if (specialDayService.isHoliday(contract, date)) {
                return null;
            }
            return journeyOf(date, contractShift.getWorkShift());
        }

        /* 4. Nada: ese dia no se evalua asistencia. */
        return null;
    }

    public List<ScheduledJourney> resolve(Contract contract, Date from, Date to) {
        return resolvePeriod(contract, from, to).getJourneys();
    }

    public PeriodJourneys resolvePeriod(Contract contract, Date from, Date to) {
        PeriodJourneys period = new PeriodJourneys();
        if (null == contract || null == from || null == to) {
            return period;
        }

        /* El periodo se resuelve con tres consultas y no con tres por dia: para un mes de 31
           dias eso serian 93 viajes a la base por persona, y la planilla toca cientos. */
        Date start = startOfDay(from);
        Date end = startOfDay(to);
        Map<Long, ScheduleException> exceptions = exceptionsOf(contract, start, end);
        Set<Long> holidays = specialDayService.holidaysBetween(contract, start, end);
        /* Los permisos por horas del periodo, de una sola vez. Cuelgan de la jornada porque son
           un hecho del dia, igual que el horario: la evaluacion los consulta desde ahi.
           Se piden con un dia de mas: una jornada de noche empieza el 13 y termina el 14, y su
           permiso de salida puede estar cargado en el 14. */
        Map<Long, List<ExcusedInterval>> excused =
                specialDayService.excusedIntervalsBetween(contract, start, addDays(end, 1));
        Map<String, GroupScheduleDay> scheduleDays = new HashMap<String, GroupScheduleDay>();
        Map<Long, Boolean> inGroup = new HashMap<Long, Boolean>();
        List<ContractWorkShift> contractShifts = contractShiftsOf(contract, start, end);

        Calendar current = Calendar.getInstance();
        current.setTime(start);
        Calendar last = Calendar.getInstance();
        last.setTime(end);
        while (!current.after(last)) {
            Date date = current.getTime();
            Long key = date.getTime();

            /* 1. La excepcion de esa persona ese dia. */
            ScheduleException exception = exceptions.get(key);
            if (null != exception) {
                period.put(key, excusedJourney(journeyOf(date, exception.getWorkShift()), excused, key),
                        JourneySource.EXCEPTION, null, null);
                current.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            /* 2. El cronograma del grupo al que pertenecia -o al que estaba prestada-.
                  La pertenencia se consulta por dia porque puede cambiar dentro del periodo; el
                  cronograma del grupo, en cambio, se carga entero la primera vez que hace falta. */
            WorkGroupMembership membership = workGroupService.findMembership(contract, date);
            if (null != membership) {
                Long groupId = membership.getWorkGroup().getId();
                if (!Boolean.TRUE.equals(inGroup.get(groupId))) {
                    loadGroupDays(scheduleDays, membership, start, end);
                    inGroup.put(groupId, Boolean.TRUE);
                }
                GroupScheduleDay scheduleDay = scheduleDays.get(groupKey(groupId, date));
                ScheduledJourney journey = (null != scheduleDay && scheduleDay.isPublished())
                        ? journeyOf(date, scheduleDay.getWorkShift()) : null;
                period.put(key, excusedJourney(journey, excused, key),
                        JourneySource.GROUP_SCHEDULE, null, membership);
                current.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            /* 3. El horario fijo del contrato, salvo que sea feriado. */
            ContractWorkShift contractShift = shiftOfDay(contractShifts, date);
            if (null != contractShift) {
                ScheduledJourney journey = journeyOf(date, contractShift.getWorkShift());
                if (holidays.contains(key)) {
                    period.put(key, null, JourneySource.HOLIDAY, journey, null);
                } else {
                    period.put(key, excusedJourney(journey, excused, key),
                            JourneySource.CONTRACT_SCHEDULE, null, null);
                }
                current.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            /* 4. Nada: ese dia no se evalua asistencia. */
            period.put(key, null, JourneySource.NONE, null, null);
            current.add(Calendar.DAY_OF_MONTH, 1);
        }
        return period;
    }

    public ScheduledJourney suppressedByHoliday(Contract contract, Date day) {
        if (null == contract || null == day) {
            return null;
        }
        Date date = startOfDay(day);
        if (!JourneySource.HOLIDAY.equals(sourceOf(contract, date))) {
            return null;
        }
        ContractWorkShift contractShift = findContractShift(contract, date);
        return null == contractShift ? null : journeyOf(date, contractShift.getWorkShift());
    }

    public JourneySource sourceOf(Contract contract, Date day) {
        if (null == contract || null == day) {
            return JourneySource.NONE;
        }
        Date date = startOfDay(day);
        if (null != findException(contract, date)) {
            return JourneySource.EXCEPTION;
        }
        if (null != workGroupService.findMembership(contract, date)) {
            return JourneySource.GROUP_SCHEDULE;
        }
        if (null != findContractShift(contract, date)) {
            return specialDayService.isHoliday(contract, date)
                    ? JourneySource.HOLIDAY : JourneySource.CONTRACT_SCHEDULE;
        }
        return JourneySource.NONE;
    }

    // ------------------------------------------------------------------ apoyo

    /**
     * Agrega solo los permisos que se pisan con la jornada.
     * <p/>
     * El filtro importa por el turno de noche: la jornada del 29 termina el 30 a las 07:30, asi
     * que mira tambien los permisos del 30 -ahi puede estar cargada su salida-, pero un permiso
     * del 30 de 19:30 a 07:30 es de la jornada SIGUIENTE y no la toca. Sin el filtro, ese dia
     * mostraba "Permiso 19:30-07:30" al lado de "Falta", que no hay forma de explicar.
     */
    private static void addTouching(List<ExcusedInterval> target, List<ExcusedInterval> candidates,
                                    ScheduledJourney journey) {
        if (null == candidates) {
            return;
        }
        for (ExcusedInterval interval : candidates) {
            if (interval.overlapMinutes(journey.getStart(), journey.getEnd()) > 0) {
                target.add(interval);
            }
        }
    }

    /**
     * Le cuelga a la jornada los permisos por horas que la alcanzan.
     * <p/>
     * Una jornada que cruza la medianoche toma tambien los del dia siguiente: empieza el 13 y
     * termina el 14, asi que un permiso de salida cargado el 14 es suyo. Sin esto, el turno de
     * noche seria el unico al que los permisos por horas no le sirven.
     */
    private static ScheduledJourney excusedJourney(ScheduledJourney journey,
                                                   Map<Long, List<ExcusedInterval>> excused,
                                                   Long key) {
        if (null == journey) {
            return null;
        }
        List<ExcusedInterval> intervals = new ArrayList<ExcusedInterval>();
        addTouching(intervals, excused.get(key), journey);
        if (journey.crossesMidnight()) {
            addTouching(intervals, excused.get(addDays(new Date(key), 1).getTime()), journey);
        }
        journey.setExcusedIntervals(intervals);
        return journey;
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private static void addIfWorking(List<ScheduledJourney> journeys, Date day, WorkShift shift) {
        ScheduledJourney journey = journeyOf(day, shift);
        if (null != journey) {
            journeys.add(journey);
        }
    }

    /** Turno nulo significa que ese dia no hay jornada, en cualquiera de las capas. */
    private static ScheduledJourney journeyOf(Date day, WorkShift shift) {
        if (null == shift) {
            return null;
        }
        return ScheduledJourney.of(day, shift.getStartHour(), shift.getEndHour(),
                shift.getEntryToleranceMinutes(), shift.getEarlyExitToleranceMinutes(),
                shift.getId(),
                null == shift.getBeforeMarginMinutes() ? 0 : shift.getBeforeMarginMinutes(),
                null == shift.getAfterMarginMinutes() ? 0 : shift.getAfterMarginMinutes());
    }

    @SuppressWarnings({"unchecked"})
    private ScheduleException findException(Contract contract, Date date) {
        List<ScheduleException> result = em
                .createNamedQuery("ScheduleException.findByContractAndDate")
                .setParameter("contract", contract)
                .setParameter("date", date)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @SuppressWarnings({"unchecked"})
    private Map<Long, ScheduleException> exceptionsOf(Contract contract, Date from, Date to) {
        Map<Long, ScheduleException> byDay = new HashMap<Long, ScheduleException>();
        List<ScheduleException> result = em
                .createNamedQuery("ScheduleException.findByContractAndRange")
                .setParameter("contract", contract)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        for (ScheduleException exception : result) {
            byDay.put(startOfDay(exception.getDate()).getTime(), exception);
        }
        return byDay;
    }

    @SuppressWarnings({"unchecked"})
    private ContractWorkShift findContractShift(Contract contract, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        List<ContractWorkShift> result = em
                .createNamedQuery("ContractWorkShift.findByContractAndDate")
                .setParameter("contract", contract)
                .setParameter("dayOfWeek", calendar.get(Calendar.DAY_OF_WEEK))
                .setParameter("date", date)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @SuppressWarnings({"unchecked"})
    private List<ContractWorkShift> contractShiftsOf(Contract contract, Date from, Date to) {
        List<ContractWorkShift> result = em
                .createNamedQuery("ContractWorkShift.findByContractAndPeriod")
                .setParameter("contract", contract)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return null == result ? new ArrayList<ContractWorkShift>() : result;
    }

    /**
     * El horario fijo que aplicaba ese dia. Se filtra en memoria sobre los vigentes en lugar de
     * consultar por dia; un contrato tiene a lo sumo siete filas abiertas.
     */
    private static ContractWorkShift shiftOfDay(List<ContractWorkShift> shifts, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        for (ContractWorkShift shift : shifts) {
            if (!shift.getDayOfWeek().equals(dayOfWeek)) {
                continue;
            }
            if (shift.getStartDate().after(date)) {
                continue;
            }
            if (null != shift.getEndDate() && shift.getEndDate().before(date)) {
                continue;
            }
            return shift;
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    private void loadGroupDays(Map<String, GroupScheduleDay> target, WorkGroupMembership membership,
                               Date from, Date to) {
        List<GroupScheduleDay> days = em
                .createNamedQuery("GroupScheduleDay.findByGroupAndRange")
                .setParameter("workGroup", membership.getWorkGroup())
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        for (GroupScheduleDay day : days) {
            target.put(groupKey(membership.getWorkGroup().getId(), day.getDay()), day);
        }
    }

    private static String groupKey(Long groupId, Date day) {
        return groupId + "|" + startOfDay(day).getTime();
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
