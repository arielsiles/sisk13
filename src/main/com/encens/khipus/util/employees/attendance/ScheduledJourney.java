package com.encens.khipus.util.employees.attendance;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * La ventana: hasta donde puede estar una marca y seguir siendo de esta jornada.
     * <p/>
     * Arranca en `inicio - margen antes` y termina en `fin + margen despues`, y despues se
     * <b>recorta en el punto medio</b> hacia la jornada vecina. El recorte es lo que hace
     * funcionar el turno partido: sin el, la ventana del bloque de la mañana se mete adentro del
     * bloque de la tarde y una marca del mediodia no se puede atribuir.
     * <p/>
     * Entre dias consecutivos el punto medio cae mas lejos que el margen, asi que no recorta
     * nada y <b>queda hueco</b>. Ese hueco es lo que permite que una marca pueda ser trabajo
     * fuera de horario: si las ventanas se tocaran siempre, ninguna podria serlo.
     */
    private Date windowStart;
    private Date windowEnd;

    /* Los permisos por horas que caen sobre esta jornada. Viven aca y no en la evaluacion
       porque son un hecho del dia, igual que el horario: la evaluacion los consulta. */
    private List<ExcusedInterval> excusedIntervals = new ArrayList<ExcusedInterval>();

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
        return of(day, startHour, endHour, entryToleranceMinutes, earlyExitToleranceMinutes,
                sourceId, 0, 0);
    }

    public static ScheduledJourney of(Date day, Date startHour, Date endHour,
                                      int entryToleranceMinutes, int earlyExitToleranceMinutes,
                                      Long sourceId, int beforeMarginMinutes,
                                      int afterMarginMinutes) {
        Calendar start = at(day, startHour);
        Calendar end = at(day, endHour);
        if (!end.after(start)) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
        ScheduledJourney journey = new ScheduledJourney(start.getTime(), end.getTime(),
                entryToleranceMinutes, earlyExitToleranceMinutes, sourceId);
        journey.windowStart = shifted(start.getTime(), -beforeMarginMinutes);
        journey.windowEnd = shifted(end.getTime(), afterMarginMinutes);
        return journey;
    }

    private static Date shifted(Date moment, int minutes) {
        return new Date(moment.getTime() + minutes * 60000L);
    }

    /**
     * Recorta las ventanas de una lista de jornadas en el punto medio entre cada una y la
     * siguiente. La lista tiene que venir ordenada por inicio.
     * <p/>
     * Solo recorta cuando hace falta: si las ventanas ya no se tocan -el caso de dos dias
     * consecutivos- no toca nada y el hueco entre ellas se conserva.
     */
    public static void clipWindows(List<ScheduledJourney> journeys) {
        for (int i = 0; i + 1 < journeys.size(); i++) {
            ScheduledJourney current = journeys.get(i);
            ScheduledJourney next = journeys.get(i + 1);
            if (null == current.windowEnd || null == next.windowStart) {
                continue;
            }
            if (!current.windowEnd.after(next.windowStart)) {
                continue;
            }
            long midpoint = (current.end.getTime() + next.start.getTime()) / 2;
            current.windowEnd = new Date(midpoint);
            next.windowStart = new Date(midpoint);
        }
    }

    /**
     * Si el momento cae dentro de la ventana. El limite de arriba es exclusivo para que en el
     * punto medio exacto gane la jornada anterior: ya esta abierta y necesita su salida,
     * mientras que la siguiente todavia no empezo.
     */
    public boolean windowContains(Date moment) {
        if (null == windowStart || null == windowEnd) {
            return false;
        }
        return !moment.before(windowStart) && !moment.after(windowEnd);
    }

    /** Minutos hasta el borde mas cercano de la jornada. Cero si el momento cae adentro. */
    public long minutesToNearestEdge(Date moment) {
        if (!moment.before(start) && !moment.after(end)) {
            return 0;
        }
        long toStart = Math.abs(moment.getTime() - start.getTime()) / 60000L;
        long toEnd = Math.abs(moment.getTime() - end.getTime()) / 60000L;
        return Math.min(toStart, toEnd);
    }

    /** Si el momento esta mas cerca del inicio que del fin: sirve para leer una marca suelta. */
    public boolean closerToStart(Date moment) {
        long toStart = Math.abs(moment.getTime() - start.getTime());
        long toEnd = Math.abs(moment.getTime() - end.getTime());
        return toStart <= toEnd;
    }

    public List<ExcusedInterval> getExcusedIntervals() {
        return excusedIntervals;
    }

    public void setExcusedIntervals(List<ExcusedInterval> excusedIntervals) {
        this.excusedIntervals = null == excusedIntervals
                ? new ArrayList<ExcusedInterval>() : excusedIntervals;
    }

    public Date getWindowStart() {
        return windowStart;
    }

    public Date getWindowEnd() {
        return windowEnd;
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
