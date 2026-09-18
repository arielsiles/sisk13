package com.encens.khipus.util.employees.attendance;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Un tramo de tiempo justificado dentro de un dia: un permiso por horas.
 * <p/>
 * El motor viejo si los leia, y no era un caso de borde: en la base del cliente que lleva cinco
 * anios con bandas, 8.398 de sus 14.819 fechas especiales son parciales. El motor nuevo los
 * ignoraba, asi que a esa gente le habrian aparecido atrasos y salidas anticipadas que hasta ayer
 * estaban justificadas.
 * <p/>
 * <b>Perdona solo los minutos que cubre.</b> El criterio viejo era grueso: si el permiso tocaba la
 * banda por un extremo, perdonaba la banda entera -un permiso de diez minutos borraba una jornada
 * de ocho horas-. Aca se descuenta el solape exacto y ni un minuto mas.
 *
 * @author
 * @version 6.1.0
 */
public final class ExcusedInterval implements Serializable {

    private final Date start;
    private final Date end;

    public ExcusedInterval(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    /** Los minutos de [from, to] que este tramo cubre. Cero si no se tocan. */
    public long overlapMinutes(Date from, Date to) {
        if (null == start || null == end || null == from || null == to || !from.before(to)) {
            return 0;
        }
        long a = Math.max(from.getTime(), start.getTime());
        long b = Math.min(to.getTime(), end.getTime());
        return b <= a ? 0 : (b - a) / 60000L;
    }

    /**
     * Los minutos de [from, to] que cubre un conjunto de tramos.
     * <p/>
     * Se suma tramo por tramo. Dos permisos que se pisen contarian dos veces el solape, pero eso
     * seria un dato contradictorio -dos permisos para la misma hora- y no algo que el motor deba
     * interpretar: el tope de abajo evita que, por ese camino, se perdone mas tiempo del que hay.
     */
    public static long excusedMinutes(List<ExcusedInterval> intervals, Date from, Date to) {
        if (null == intervals || intervals.isEmpty() || null == from || null == to) {
            return 0;
        }
        long total = 0;
        for (ExcusedInterval interval : intervals) {
            total += interval.overlapMinutes(from, to);
        }
        long span = (to.getTime() - from.getTime()) / 60000L;
        return Math.min(total, Math.max(span, 0));
    }
}
