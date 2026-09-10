package com.encens.khipus.util.employees.attendance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Una jornada con las sesiones que le tocaron.
 * <p/>
 * Puede quedar sin ninguna sesion, y ese es el <b>unico</b> caso que despues se evalua como
 * falta. Es la diferencia de fondo con el motor viejo, donde tambien era falta no encontrar
 * dos marcas dentro de una ventana.
 *
 * @author
 * @version 6.1.0
 */
public class JourneyAssignment {

    private final ScheduledJourney journey;
    private final List<WorkSession> sessions = new ArrayList<WorkSession>();

    public JourneyAssignment(ScheduledJourney journey) {
        this.journey = journey;
    }

    public void add(WorkSession session) {
        sessions.add(session);
    }

    public ScheduledJourney getJourney() {
        return journey;
    }

    public List<WorkSession> getSessions() {
        return sessions;
    }

    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    /** La primera entrada del dia, que es contra la que se mide el atraso. */
    public Date getFirstEntry() {
        Date first = null;
        for (WorkSession session : sessions) {
            if (null != session.getEntry() && (null == first || session.getEntry().before(first))) {
                first = session.getEntry();
            }
        }
        return first;
    }

    /**
     * La ultima salida, para medir la salida anticipada.
     * <p/>
     * Si alguna sesion quedo sin salida, se cierra en el fin de la jornada y nunca mas alla:
     * no se puede saber hasta que hora se quedo, pero tampoco corresponde castigarlo como si
     * se hubiera ido temprano.
     */
    public Date getLastExit() {
        Date last = null;
        for (WorkSession session : sessions) {
            Date exit = null != session.getExit() ? session.getExit() : journey.getEnd();
            if (null == last || exit.after(last)) {
                last = exit;
            }
        }
        return last;
    }

    /**
     * La ultima salida REALMENTE marcada, o null si ninguna sesion la tiene.
     * <p/>
     * `getLastExit` cierra en el fin de la jornada cuando falta la salida, y eso esta bien para
     * evaluar. Pero para MOSTRAR hay que distinguir: una hora que puso el motor no puede
     * presentarse como si la persona la hubiera marcado.
     */
    public Date getLastMarkedExit() {
        Date last = null;
        for (WorkSession session : sessions) {
            if (null != session.getExit() && (null == last || session.getExit().after(last))) {
                last = session.getExit();
            }
        }
        return last;
    }

    /** true si alguna sesion quedo sin su salida: el cierre de arriba fue asumido. */
    public boolean hasAssumedExit() {
        for (WorkSession session : sessions) {
            if (session.isEntryWithoutExit()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "JourneyAssignment{" + journey + ", sesiones=" + sessions.size() + "}";
    }
}
