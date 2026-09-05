package com.encens.khipus.util.employees.attendance;

import java.util.Calendar;
import java.util.Date;

/**
 * La jornada que le tocaba a la persona ese dia, con fecha y hora absolutas.
 * <p/>
 * En la base el horario guarda solo la hora -07:30 a 19:30- y el motor viejo le imponia el dia
 * de la iteracion. Por eso una jornada nocturna era imposible: la salida de las 07:30 del dia
 * siguiente terminaba comparandose contra el dia anterior. Aca la jornada se arma una sola vez
 * con sus dos instantes completos, y si el fin es anterior al inicio se entiende que cruza la
 * medianoche y el fin cae al dia siguiente. El cruce deja de ser un caso especial.
 * <p/>
 * Lleva sus dos tolerancias y nada mas. Las ocho de hoy -cuatro de `Limit` y cuatro de
 * `Tolerance`- sobran: `Limit` desaparece porque ya no hay ventana que descarte marcas, y de
 * `Tolerance` solo quedan la de entrada y la de salida anticipada. Quedarse despues de hora no
 * necesita tolerancia porque no se penaliza.
 *
 * @author
 * @version 6.1.0
 */
public class ScheduledJourney {

    private final Date start;
    private final Date end;
    private final int entryToleranceMinutes;
    private final int earlyExitToleranceMinutes;
    private final Long sourceId;

    public ScheduledJourney(Date start, Date end, int entryToleranceMinutes,
                            int earlyExitToleranceMinutes, Long sourceId) {
        this.start = start;
        this.end = end;
        this.entryToleranceMinutes = entryToleranceMinutes;
        this.earlyExitToleranceMinutes = earlyExitToleranceMinutes;
        this.sourceId = sourceId;
    }

    /**
     * Arma la jornada de un dia a partir de las horas del horario.
     *
     * @param day       el dia al que pertenece la jornada
     * @param startHour hora de inicio; solo se usa su parte horaria
     * @param endHour   hora de fin; si es menor o igual al inicio, la jornada cruza la medianoche
     */
    public static ScheduledJourney of(Date day, Date startHour, Date endHour,
                                      int entryToleranceMinutes, int earlyExitToleranceMinutes,
                                      Long sourceId) {
        Calendar start = at(day, startHour);
        Calendar end = at(day, endHour);
        if (!end.after(start)) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new ScheduledJourney(start.getTime(), end.getTime(),
                entryToleranceMinutes, earlyExitToleranceMinutes, sourceId);
    }

    @SuppressWarnings({"deprecation"})
    private static Calendar at(Date day, Date hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, hour.getHours());
        calendar.set(Calendar.MINUTE, hour.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public boolean crossesMidnight() {
        Calendar s = Calendar.getInstance();
        s.setTime(start);
        Calendar e = Calendar.getInstance();
        e.setTime(end);
        return s.get(Calendar.DAY_OF_YEAR) != e.get(Calendar.DAY_OF_YEAR);
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public int getEntryToleranceMinutes() {
        return entryToleranceMinutes;
    }

    public int getEarlyExitToleranceMinutes() {
        return earlyExitToleranceMinutes;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public long getMinutes() {
        return (end.getTime() - start.getTime()) / 60000L;
    }

    @Override
    public String toString() {
        return "ScheduledJourney{" + start + " -> " + end + "}";
    }
}
