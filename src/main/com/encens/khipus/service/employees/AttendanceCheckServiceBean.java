package com.encens.khipus.service.employees;

import com.encens.khipus.action.employees.dto.AttendanceDay;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.RH_Mark;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.AttendanceEngine;
import com.encens.khipus.util.employees.attendance.AttendanceMark;
import com.encens.khipus.util.employees.attendance.ExcusedDay;
import com.encens.khipus.util.employees.attendance.JourneyAssignment;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;
import com.encens.khipus.util.employees.attendance.SessionScheduleMatcher;
import com.encens.khipus.util.employees.attendance.WorkSessionBuilder;
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

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("attendanceCheckService")
@AutoCreate
public class AttendanceCheckServiceBean implements AttendanceCheckService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private JourneyResolverService journeyResolverService;

    @In
    private SpecialDayService specialDayService;

    @SuppressWarnings({"unchecked"})
    public List<AttendanceDay> check(Contract contract, Date from, Date to) {
        List<AttendanceDay> days = new ArrayList<AttendanceDay>();
        if (null == contract || null == from || null == to) {
            return days;
        }
        Employee employee = contract.getEmployee();
        if (null == employee || null == employee.getMarkCode()) {
            return days;
        }

        Date start = startOfDay(from);
        Date end = startOfDay(to);

        /* Las marcas se leen con un dia de mas de cada lado. Una jornada de noche empieza el 13
           y termina el 14: si se cortara justo en el rango, la salida quedaria afuera y el dia
           parecería sin salida marcada. */
        List<RH_Mark> marks = em.createQuery(
                "select m from RH_Mark m"
                        + " where m.marPerId = :markCode"
                        + " and m.marDate >= :from and m.marDate <= :to"
                        + " order by m.marDate asc, m.marTime asc")
                .setParameter("markCode", markCodeOf(employee))
                .setParameter("from", addDays(start, -1))
                .setParameter("to", addDays(end, 1))
                .getResultList();

        /* El mismo motor que va a usar la planilla: las jornadas salen de las cuatro capas y
           GUIAN el emparejamiento de las marcas. El orden importa: emparejar sin el horario a la
           vista es lo que producia atrasos inventados de horas cuando faltaba una marca. */
        PeriodJourneys period = journeyResolverService.resolvePeriod(contract, start, end);
        List<ScheduledJourney> journeys = period.getJourneys();
        WorkSessionBuilder builder = new WorkSessionBuilder(
                AttendanceMark.ofImported(marks), journeys);
        SessionScheduleMatcher matcher = new SessionScheduleMatcher(
                builder.getSessions(), journeys);

        /* Los feriados del periodo, de una sola vez. Es un hecho del dia: se muestra tanto
           si suspendio la jornada como si la persona igual vino a trabajar. */
        java.util.Set<Long> holidays = specialDayService.holidaysBetween(contract, start, end);

        /* Los dias que no hay que justificar con marcas: permisos, vacaciones, maternidad. Sin
           esto, la primera vacacion cargada le pondria falta a cada dia de vacaciones. */
        Map<Long, ExcusedDay> excused = specialDayService.excusedBetween(contract, start, end);

        Map<Long, JourneyAssignment> assignmentByDay = new HashMap<Long, JourneyAssignment>();
        for (JourneyAssignment assignment : matcher.getAssignments()) {
            assignmentByDay.put(startOfDay(assignment.getJourney().getStart()).getTime(), assignment);
        }
        Map<Long, ScheduledJourney> journeyByDay = new HashMap<Long, ScheduledJourney>();
        for (ScheduledJourney journey : journeys) {
            journeyByDay.put(startOfDay(journey.getStart()).getTime(), journey);
        }
        Map<Long, List<RH_Mark>> marksByDay = new HashMap<Long, List<RH_Mark>>();
        for (RH_Mark mark : marks) {
            Long key = startOfDay(mark.getMarDate()).getTime();
            List<RH_Mark> ofDay = marksByDay.get(key);
            if (null == ofDay) {
                ofDay = new ArrayList<RH_Mark>();
                marksByDay.put(key, ofDay);
            }
            ofDay.add(mark);
        }

        Calendar current = Calendar.getInstance();
        current.setTime(start);
        Calendar last = Calendar.getInstance();
        last.setTime(end);
        while (!current.after(last)) {
            Date date = current.getTime();
            Long key = startOfDay(date).getTime();

            AttendanceDay day = new AttendanceDay(date);
            day.setJourney(journeyByDay.get(key));
            day.setAssignment(assignmentByDay.get(key));
            day.setSource(period.sourceOf(key));
            WorkGroupMembership membership = period.membershipOf(key);
            if (null != membership) {
                day.setGroupName(membership.getWorkGroup().getName());
                day.setLoaned(membership.isLoan());
            }
            day.setHoliday(holidays.contains(key));
            day.setExcused(excused.get(key));
            day.setSuppressedJourney(period.suppressedOf(key));
            if (marksByDay.containsKey(key)) {
                day.setMarks(marksByDay.get(key));
            }
            days.add(day);

            current.add(Calendar.DAY_OF_MONTH, 1);
        }

        /* Marcar los dias que solo reciben la salida de la noche anterior, para que no se
           lean como marcas sueltas. */
        for (int i = 1; i < days.size(); i++) {
            ScheduledJourney previous = days.get(i - 1).getJourney();
            if (null == previous) {
                continue;
            }
            if (startOfDay(previous.getEnd()).getTime() == startOfDay(days.get(i).getDay()).getTime()) {
                days.get(i).setCoveredByPreviousJourney(true);
            }
        }
        return days;
    }

    public boolean usesJourneys(Contract contract, Date date) {
        if (null == contract || null == date) {
            return true;
        }
        for (JobContract jobContract : contract.getJobContractList()) {
            if (null != jobContract.getJob() && null != jobContract.getJob().getJobCategory()) {
                return AttendanceEngine.of(
                        jobContract.getJob().getJobCategory().getJourneysFrom(), date).isJourneys();
            }
        }
        /* Sin categoria no hay fecha de corte que consultar. Se asume el motor nuevo, que es el
           unico que esta pantalla sabe mostrar. */
        return true;
    }

    public boolean hasReadableMarkCode(Employee employee) {
        return null != employee && null != employee.getMarkCode() && markCodeOf(employee) > 0;
    }

    /**
     * El codigo de marcacion se guarda como texto pero el biometrico lo escribe como numero.
     * Si no es numerico no hay marcas que buscar.
     */
    private static Integer markCodeOf(Employee employee) {
        try {
            return Integer.valueOf(employee.getMarkCode().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
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
