package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.ContractWorkShift;
import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.ScheduleException;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;
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
    private HolidayService holidayService;

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
            if (holidayService.isHoliday(contract, date)) {
                return null;
            }
            return journeyOf(date, contractShift.getWorkShift());
        }

        /* 4. Nada: ese dia no se evalua asistencia. */
        return null;
    }

    public List<ScheduledJourney> resolve(Contract contract, Date from, Date to) {
        List<ScheduledJourney> journeys = new ArrayList<ScheduledJourney>();
        if (null == contract || null == from || null == to) {
            return journeys;
        }

        /* El periodo se resuelve con tres consultas y no con tres por dia: para un mes de 31
           dias eso serian 93 viajes a la base por persona, y la planilla toca cientos. */
        Date start = startOfDay(from);
        Date end = startOfDay(to);
        Map<Long, ScheduleException> exceptions = exceptionsOf(contract, start, end);
        Set<Long> holidays = holidayService.holidaysBetween(contract, start, end);
        Map<String, GroupScheduleDay> scheduleDays = new HashMap<String, GroupScheduleDay>();
        Map<Long, Boolean> inGroup = new HashMap<Long, Boolean>();
        List<ContractWorkShift> contractShifts = currentContractShifts(contract);

        Calendar current = Calendar.getInstance();
        current.setTime(start);
        Calendar last = Calendar.getInstance();
        last.setTime(end);
        while (!current.after(last)) {
            Date date = current.getTime();
            Long key = date.getTime();

            ScheduleException exception = exceptions.get(key);
            if (null != exception) {
                addIfWorking(journeys, date, exception.getWorkShift());
                current.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            /* La pertenencia se consulta por dia porque puede cambiar dentro del periodo; el
               cronograma del grupo, en cambio, se carga entero la primera vez que hace falta. */
            WorkGroupMembership membership = workGroupService.findMembership(contract, date);
            if (null != membership) {
                Long groupId = membership.getWorkGroup().getId();
                if (!Boolean.TRUE.equals(inGroup.get(groupId))) {
                    loadGroupDays(scheduleDays, membership, start, end);
                    inGroup.put(groupId, Boolean.TRUE);
                }
                GroupScheduleDay scheduleDay = scheduleDays.get(groupKey(groupId, date));
                if (null != scheduleDay && scheduleDay.isPublished()) {
                    addIfWorking(journeys, date, scheduleDay.getWorkShift());
                }
                current.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            ContractWorkShift contractShift = shiftOfDay(contractShifts, date);
            if (null != contractShift && !holidays.contains(key)) {
                addIfWorking(journeys, date, contractShift.getWorkShift());
            }
            current.add(Calendar.DAY_OF_MONTH, 1);
        }
        return journeys;
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
            return holidayService.isHoliday(contract, date)
                    ? JourneySource.HOLIDAY : JourneySource.CONTRACT_SCHEDULE;
        }
        return JourneySource.NONE;
    }

    // ------------------------------------------------------------------ apoyo

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
    private List<ContractWorkShift> currentContractShifts(Contract contract) {
        List<ContractWorkShift> result = em
                .createNamedQuery("ContractWorkShift.findCurrentByContract")
                .setParameter("contract", contract)
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
