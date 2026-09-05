package com.encens.khipus.util.employees.attendance;

/**
 * Lo que el motor no puede resolver solo y RRHH tiene que mirar antes de cerrar el mes.
 * <p/>
 * El motor viejo resolvia todo en silencio y la unica salida posible era la falta. Acá lo
 * dudoso se reporta y se sigue: <b>ninguna incidencia provoca una falta por si misma</b>.
 * <p/>
 * Quedarse despues de hora no esta en esta lista a proposito: es normal y no requiere revision.
 *
 * @author
 * @version 6.1.0
 */
public enum AttendanceIncidenceType {

    /** Marco la entrada y no marco la salida. */
    ENTRY_WITHOUT_EXIT("AttendanceIncidence.entryWithoutExit"),

    /** Hay una salida sin su entrada. */
    EXIT_WITHOUT_ENTRY("AttendanceIncidence.exitWithoutEntry"),

    /** La sesion no se superpone con ninguna jornada: trabajo fuera de horario. */
    SESSION_WITHOUT_SCHEDULE("AttendanceIncidence.sessionWithoutSchedule"),

    /** La jornada no tiene ninguna sesion. Es el unico caso que si es falta. */
    SCHEDULE_WITHOUT_SESSION("AttendanceIncidence.scheduleWithoutSession"),

    /** Una sesion tan larga que casi seguro es un error de marcado. */
    ABNORMALLY_LONG_SESSION("AttendanceIncidence.abnormallyLongSession");

    private String resourceKey;

    AttendanceIncidenceType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
