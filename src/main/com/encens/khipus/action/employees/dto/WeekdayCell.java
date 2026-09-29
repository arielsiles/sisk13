package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.employees.ContractWorkShift;
import com.encens.khipus.model.employees.WorkShift;

import java.io.Serializable;
import java.util.Date;

/**
 * Un dia de la semana en el horario fijo de un contrato.
 * <p/>
 * Trae una fecha de muestra -un dia cualquiera que caiga en ese dia de la semana- solo para que
 * la vista pueda escribir "lunes" con el formateador de fechas y no haga falta un juego de
 * etiquetas traducidas para los siete dias.
 *
 * @author
 * @version 6.1.0
 */
public class WeekdayCell implements Serializable {

    private int dayOfWeek;
    private Date sample;
    private WorkShift workShift;
    private boolean persisted;

    public WeekdayCell(int dayOfWeek, Date sample, ContractWorkShift contractShift) {
        this.dayOfWeek = dayOfWeek;
        this.sample = sample;
        if (null != contractShift) {
            this.workShift = contractShift.getWorkShift();
            this.persisted = true;
        }
    }

    public boolean isWorking() {
        return null != workShift;
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

    public String getStyleClass() {
        StringBuilder styleClass = new StringBuilder("app-cal__cell");
        if (isWorking()) {
            styleClass.append(workShift.isCrossingMidnight()
                    ? " app-cal__cell--night" : " app-cal__cell--day");
        } else {
            styleClass.append(persisted ? " app-cal__cell--rest" : " app-cal__cell--empty");
        }
        return styleClass.toString();
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public Date getSample() {
        return sample;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public boolean isPersisted() {
        return persisted;
    }
}
