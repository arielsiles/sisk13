package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.ScheduleState;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkShift;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
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
@Name("groupScheduleService")
@AutoCreate
public class GroupScheduleServiceBean extends GenericServiceBean implements GroupScheduleService {

    @In
    private ScheduleLockService scheduleLockService;

    @SuppressWarnings({"unchecked"})
    public List<GroupScheduleDay> findDays(WorkGroup workGroup, Date from, Date to) {
        if (null == workGroup || null == workGroup.getId()) {
            return new ArrayList<GroupScheduleDay>();
        }
        List<GroupScheduleDay> resultList = getEntityManager()
                .createNamedQuery("GroupScheduleDay.findByGroupAndRange")
                .setParameter("workGroup", workGroup)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return null == resultList ? new ArrayList<GroupScheduleDay>() : resultList;
    }

    @SuppressWarnings({"unchecked"})
    public List<GroupScheduleDay> findPublishedDays(WorkGroup workGroup, Date from, Date to) {
        if (null == workGroup || null == workGroup.getId()) {
            return new ArrayList<GroupScheduleDay>();
        }
        List<GroupScheduleDay> resultList = getEntityManager()
                .createNamedQuery("GroupScheduleDay.findPublishedByGroupAndRange")
                .setParameter("workGroup", workGroup)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("state", ScheduleState.PUBLISHED)
                .getResultList();
        return null == resultList ? new ArrayList<GroupScheduleDay>() : resultList;
    }

    public void setShift(WorkGroup workGroup, Date day, WorkShift workShift) throws Exception {
        /* Un dia ya pagado no se toca: el calculo que se hizo con el horario de entonces tiene
           que poder reproducirse. */
        scheduleLockService.checkOpen(day);
        GroupScheduleDay scheduleDay = findDay(workGroup, day);
        if (null == scheduleDay) {
            scheduleDay = new GroupScheduleDay();
            scheduleDay.setWorkGroup(workGroup);
            scheduleDay.setDay(day);
            scheduleDay.setWorkShift(workShift);
            scheduleDay.setState(ScheduleState.DRAFT);
            create(scheduleDay);
        } else {
            scheduleDay.setWorkShift(workShift);
            /* Tocar un dia publicado lo devuelve a borrador: el cambio no puede llegar al motor
               sin que alguien lo publique a proposito. */
            scheduleDay.setState(ScheduleState.DRAFT);
            update(scheduleDay);
        }
    }

    public void setShiftRange(WorkGroup workGroup, Date from, Date to, WorkShift workShift) throws Exception {
        for (Date day : daysBetween(from, to)) {
            setShift(workGroup, day, workShift);
        }
    }

    public void copyRange(WorkGroup source, Date sourceFrom, Date sourceTo,
                          WorkGroup target, Date targetFrom, boolean invert) throws Exception {
        Map<Integer, WorkShift> byOffset = new HashMap<Integer, WorkShift>();
        List<Date> sourceDays = daysBetween(sourceFrom, sourceTo);
        Map<Long, GroupScheduleDay> loaded = byDay(findDays(source, sourceFrom, sourceTo));
        for (int i = 0; i < sourceDays.size(); i++) {
            GroupScheduleDay day = loaded.get(startOfDay(sourceDays.get(i)));
            byOffset.put(i, null == day ? null : day.getWorkShift());
        }

        Calendar target0 = Calendar.getInstance();
        target0.setTime(targetFrom);
        for (int i = 0; i < sourceDays.size(); i++) {
            Calendar day = (Calendar) target0.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            WorkShift shift = byOffset.get(i);
            setShift(target, day.getTime(), invert ? opposite(shift) : shift);
        }
    }

    /**
     * El turno contrario dentro del catalogo: para un turno de dia devuelve el de noche y al
     * reves. Se resuelve por el cruce de medianoche y no por el nombre, que es texto libre y no
     * se puede depender de que diga "NOCHE".
     * <p/>
     * Si no hay exactamente un candidato opuesto, devuelve el mismo turno: mejor copiar igual
     * que adivinar mal.
     */
    @SuppressWarnings({"unchecked"})
    private WorkShift opposite(WorkShift shift) {
        if (null == shift) {
            return null;
        }
        List<WorkShift> active = getEntityManager()
                .createNamedQuery("WorkShift.findActive")
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        WorkShift best = null;
        for (WorkShift candidate : active) {
            if (candidate.getId().equals(shift.getId())) {
                continue;
            }
            /* Uno cruza la medianoche y el otro no: son el par dia/noche. */
            if (candidate.isCrossingMidnight() != shift.isCrossingMidnight()) {
                if (null != best) {
                    return shift; /* mas de un candidato: no se adivina */
                }
                best = candidate;
            }
        }
        return null == best ? shift : best;
    }

    public void publish(WorkGroup workGroup, Date from, Date to) throws Exception {
        changeState(workGroup, from, to, ScheduleState.PUBLISHED);
    }

    public void unpublish(WorkGroup workGroup, Date from, Date to) throws Exception {
        changeState(workGroup, from, to, ScheduleState.DRAFT);
    }

    private void changeState(WorkGroup workGroup, Date from, Date to, ScheduleState state) throws Exception {
        /* Publicar alcanza tambien a los dias sin turno: un descanso planificado es una decision
           tomada, y el motor tiene que poder distinguirlo de un dia que nadie miro. */
        for (Date day : daysBetween(from, to)) {
            scheduleLockService.checkOpen(day);
            GroupScheduleDay scheduleDay = findDay(workGroup, day);
            if (null == scheduleDay) {
                scheduleDay = new GroupScheduleDay();
                scheduleDay.setWorkGroup(workGroup);
                scheduleDay.setDay(day);
                scheduleDay.setState(state);
                create(scheduleDay);
            } else if (!state.equals(scheduleDay.getState())) {
                scheduleDay.setState(state);
                update(scheduleDay);
            }
        }
    }

    public Long countByWorkShift(WorkShift workShift) {
        if (null == workShift || null == workShift.getId()) {
            return 0L;
        }
        return (Long) getEntityManager().createNamedQuery("GroupScheduleDay.countByWorkShift")
                .setParameter("workShift", workShift).getSingleResult();
    }

    @SuppressWarnings({"unchecked"})
    private GroupScheduleDay findDay(WorkGroup workGroup, Date day) {
        List<GroupScheduleDay> resultList = getEntityManager()
                .createNamedQuery("GroupScheduleDay.findByGroupAndDay")
                .setParameter("workGroup", workGroup)
                .setParameter("day", startOfDayDate(day))
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    private Map<Long, GroupScheduleDay> byDay(List<GroupScheduleDay> days) {
        Map<Long, GroupScheduleDay> result = new HashMap<Long, GroupScheduleDay>();
        for (GroupScheduleDay day : days) {
            result.put(startOfDay(day.getDay()), day);
        }
        return result;
    }

    private List<Date> daysBetween(Date from, Date to) {
        List<Date> result = new ArrayList<Date>();
        Calendar current = Calendar.getInstance();
        current.setTime(startOfDayDate(from));
        Calendar last = Calendar.getInstance();
        last.setTime(startOfDayDate(to));
        while (!current.after(last)) {
            result.add(current.getTime());
            current.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }

    private static Date startOfDayDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Long startOfDay(Date date) {
        return startOfDayDate(date).getTime();
    }
}
