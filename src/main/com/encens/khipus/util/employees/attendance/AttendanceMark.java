package com.encens.khipus.util.employees.attendance;

import com.encens.khipus.model.employees.RHMark;
import com.encens.khipus.model.employees.RH_Mark;
import com.encens.khipus.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Una marca con su fecha y hora ya combinadas.
 * <p/>
 * Existe porque en la base la fecha y la hora viven en columnas separadas, y el motor viejo
 * imponia el dia de la iteracion sobre la hora de la marca. Eso hacia imposible una jornada
 * que cruza la medianoche: la salida de las 07:30 del dia siguiente terminaba comparandose
 * contra el dia anterior. Aca el momento es uno solo y no se toca.
 *
 * @author
 * @version 6.1.0
 */
public class AttendanceMark {

    private final Date moment;
    private final int control;

    public AttendanceMark(Date moment, int control) {
        this.moment = moment;
        this.control = control;
    }

    public static AttendanceMark of(RHMark mark) {
        return new AttendanceMark(
                DateUtils.joinDateAndTime(mark.getMarDate(), mark.getMarTime()).getTime(),
                null == mark.getControl() ? WorkSessionBuilder.CONTROL_ENTRY : mark.getControl());
    }

    /**
     * Las marcas cargadas desde el biometrico viven en `RH_Mark` -tabla `rh_marcado`- y no en
     * `RHMark`, que mapea la vista `vmarcado`. El motor tiene que poder leer las dos.
     */
    public static AttendanceMark of(RH_Mark mark) {
        return new AttendanceMark(
                DateUtils.joinDateAndTime(mark.getMarDate(), mark.getMarTime()).getTime(),
                null == mark.getControl() ? WorkSessionBuilder.CONTROL_ENTRY : mark.getControl());
    }

    public static List<AttendanceMark> ofImported(List<RH_Mark> marks) {
        List<AttendanceMark> result = new ArrayList<AttendanceMark>();
        for (RH_Mark mark : marks) {
            result.add(of(mark));
        }
        return result;
    }

    public static List<AttendanceMark> of(List<RHMark> marks) {
        List<AttendanceMark> result = new ArrayList<AttendanceMark>();
        for (RHMark mark : marks) {
            result.add(of(mark));
        }
        return result;
    }

    public Date getMoment() {
        return moment;
    }

    public int getControl() {
        return control;
    }

    @Override
    public String toString() {
        return "AttendanceMark{" + moment + ", control=" + control + "}";
    }
}
