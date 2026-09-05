package com.encens.khipus.util.employees.attendance;

import java.util.Date;

/**
 * Algo que el motor no pudo resolver y RRHH tiene que decidir.
 *
 * @author
 * @version 6.1.0
 */
public class AttendanceIncidence {

    private final AttendanceIncidenceType type;
    private final Date moment;
    private final String detail;

    public AttendanceIncidence(AttendanceIncidenceType type, Date moment, String detail) {
        this.type = type;
        this.moment = moment;
        this.detail = detail;
    }

    public AttendanceIncidenceType getType() {
        return type;
    }

    public Date getMoment() {
        return moment;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "AttendanceIncidence{" + type + " @ " + moment + (null == detail ? "" : " - " + detail) + "}";
    }
}
