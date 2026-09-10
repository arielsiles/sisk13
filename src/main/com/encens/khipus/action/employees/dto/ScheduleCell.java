package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.ScheduleState;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkShift;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Una celda de la grilla: un grupo en un dia.
 * <p/>
 * Existe solo para la vista. La grilla necesita mostrar los siete dias de la semana aunque en la
 * base haya cargados dos: las celdas que faltan no son un hueco, son dias sin planificar, y hay
 * que poder hacerles clic igual. Por eso la fila siempre trae siete celdas y la que no tiene
 * registro detras lleva el dia y nada mas.
 *
 * @author
 * @version 6.1.0
 */
public class ScheduleCell implements Serializable {

    private WorkGroup workGroup;
    private Date day;
    private WorkShift workShift;
    private ScheduleState state;
    private boolean persisted;
    private boolean today;

    public ScheduleCell(WorkGroup workGroup, Date day, GroupScheduleDay scheduleDay) {
        this.workGroup = workGroup;
        this.day = day;
        this.today = isSameDay(day, new Date());
        if (null != scheduleDay) {
            this.workShift = scheduleDay.getWorkShift();
            this.state = scheduleDay.getState();
            this.persisted = true;
        } else {
            this.state = ScheduleState.DRAFT;
        }
    }

    /** Hay jornada ese dia. Sin turno la celda queda vacia: descanso, feriado o sin planificar. */
    public boolean isWorking() {
        return null != workShift;
    }

    public boolean isPublished() {
        return ScheduleState.PUBLISHED.equals(state);
    }

    public String getShiftName() {
        return null == workShift ? "" : workShift.getName();
    }

    public int getMinutes() {
        return null == workShift ? 0 : workShift.getDurationMinutes();
    }

    public Double getHours() {
        return getMinutes() / 60d;
    }

    /**
     * Las clases CSS de la celda se arman aca y no en la vista: son cuatro condiciones
     * combinadas y en el xhtml quedarian como un rendered por cada variante.
     */
    public String getStyleClass() {
        StringBuilder styleClass = new StringBuilder("app-cal__cell");
        if (isWorking()) {
            styleClass.append(workShift.isCrossingMidnight()
                    ? " app-cal__cell--night" : " app-cal__cell--day");
        } else {
            styleClass.append(persisted ? " app-cal__cell--rest" : " app-cal__cell--empty");
        }
        if (!isPublished()) {
            styleClass.append(" app-cal__cell--draft");
        }
        if (today) {
            styleClass.append(" app-cal__cell--today");
        }
        return styleClass.toString();
    }

    private static boolean isSameDay(Date one, Date other) {
        Calendar a = Calendar.getInstance();
        a.setTime(one);
        Calendar b = Calendar.getInstance();
        b.setTime(other);
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    public WorkGroup getWorkGroup() {
        return workGroup;
    }

    public Date getDay() {
        return day;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public ScheduleState getState() {
        return state;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public boolean isToday() {
        return today;
    }
}
