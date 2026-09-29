package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.WorkGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Una fila de la grilla: un grupo y sus siete dias de la semana.
 *
 * @author
 * @version 6.1.0
 */
public class ScheduleRow implements Serializable {

    private WorkGroup workGroup;
    private List<ScheduleCell> cells = new ArrayList<ScheduleCell>();
    private int monthMinutes;

    public ScheduleRow(WorkGroup workGroup, List<Date> days, List<GroupScheduleDay> scheduleDays) {
        this.workGroup = workGroup;
        Map<Long, GroupScheduleDay> byDay = new HashMap<Long, GroupScheduleDay>();
        for (GroupScheduleDay scheduleDay : scheduleDays) {
            byDay.put(startOfDay(scheduleDay.getDay()), scheduleDay);
        }
        for (Date day : days) {
            cells.add(new ScheduleCell(workGroup, day, byDay.get(startOfDay(day))));
        }
    }

    /** Horas planificadas en la semana. Es el numero que el planificador vigila mientras carga. */
    public int getMinutes() {
        int minutes = 0;
        for (ScheduleCell cell : cells) {
            minutes += cell.getMinutes();
        }
        return minutes;
    }

    public Double getHours() {
        return getMinutes() / 60d;
    }

    /**
     * Horas ya planificadas en el mes de la semana en pantalla. Es contra este numero que el
     * planificador compara el objetivo mensual mientras carga; sin el habria que sumar semana
     * por semana a mano.
     */
    public Double getMonthHours() {
        return monthMinutes / 60d;
    }

    public void setMonthMinutes(int monthMinutes) {
        this.monthMinutes = monthMinutes;
    }

    public int getWorkedDays() {
        int days = 0;
        for (ScheduleCell cell : cells) {
            if (cell.isWorking()) {
                days++;
            }
        }
        return days;
    }

    /**
     * La semana esta publicada cuando lo estan sus siete dias. Si falta uno, la fila sigue en
     * borrador: publicar a medias no existe.
     */
    public boolean isPublished() {
        for (ScheduleCell cell : cells) {
            if (!cell.isPublished()) {
                return false;
            }
        }
        return true;
    }

    /** Nadie toco esta fila todavia. Sirve para avisar antes de publicar una semana en blanco. */
    public boolean isUntouched() {
        for (ScheduleCell cell : cells) {
            if (cell.isPersisted()) {
                return false;
            }
        }
        return true;
    }

    private static Long startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public WorkGroup getWorkGroup() {
        return workGroup;
    }

    public List<ScheduleCell> getCells() {
        return cells;
    }
}
