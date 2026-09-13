package com.encens.khipus.util.employees.attendance;

import java.util.Calendar;
import java.util.Date;

/**
 * Un tramo de trabajo: una entrada y su salida.
 * <p/>
 * Se arma sin conocer el horario. Esa es la idea del motor nuevo: primero se reconstruye lo
 * que la persona hizo, y recien despues se lo compara con lo que tenia que hacer. El motor
 * viejo mezclaba las dos cosas -preguntaba si cada marca "pertenecia" a una banda- y por eso
 * un fallo de emparejamiento terminaba en falta.
 * <p/>
 * Una sesion puede quedar <b>incompleta</b>: hay entrada y falta la salida, o al reves. No se
 * descarta. Una sesion incompleta sigue solapando con su jornada, asi que no genera falta; la
 * falta se reserva para la jornada que no tiene ninguna sesion.
 *
 * @author
 * @version 6.1.0
 */
public class WorkSession {

    /** Como se llego a emparejar esta sesion. */
    public enum Pairing {
        /** Por el indicador entrada/salida del dispositivo. */
        INDICATOR,
        /** Cronologicamente: primera con segunda, tercera con cuarta. */
        CHRONOLOGICAL,
        /** Dentro de la ventana de una jornada: la mas temprana entra, la mas tardia sale. */
        JOURNEY
    }

    private final Date entry;
    private final Date exit;
    private final Pairing pairing;

    /**
     * La jornada para la que se armo esta sesion, cuando el emparejamiento fue guiado por el
     * horario. Se guarda para que la etapa siguiente <b>respete esa decision</b> en lugar de
     * volver a deducirla por solapamiento: calcular una cosa y despues recalcularla distinto es
     * como se cuelan las inconsistencias.
     */
    private ScheduledJourney journey;

    public WorkSession(Date entry, Date exit, Pairing pairing) {
        this.entry = entry;
        this.exit = exit;
        this.pairing = pairing;
    }

    public Date getEntry() {
        return entry;
    }

    public Date getExit() {
        return exit;
    }

    public Pairing getPairing() {
        return pairing;
    }

    public ScheduledJourney getJourney() {
        return journey;
    }

    public void setJourney(ScheduledJourney journey) {
        this.journey = journey;
    }

    /**
     * Si la sesion empieza un dia y termina otro.
     * <p/>
     * Una sesion asi <b>no puede pertenecer a una jornada que no cruza la medianoche</b>, con
     * cualquier emparejamiento: es lo que dejaba a un 17:09 emparejado con el 07:45 del dia
     * siguiente dentro de una jornada de oficina. Una sesion incompleta no cuenta: el tramo que
     * se le presta para medir el solapamiento es artificial.
     */
    public boolean crossesMidnight() {
        if (!isComplete()) {
            return false;
        }
        Calendar from = Calendar.getInstance();
        from.setTime(getEntry());
        Calendar to = Calendar.getInstance();
        to.setTime(getExit());
        return from.get(Calendar.YEAR) != to.get(Calendar.YEAR)
                || from.get(Calendar.DAY_OF_YEAR) != to.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isComplete() {
        return null != entry && null != exit;
    }

    /** Marco la entrada y se olvido de marcar la salida. */
    public boolean isEntryWithoutExit() {
        return null != entry && null == exit;
    }

    /** Hay salida sin entrada: pasa cuando la jornada empezo el dia anterior. */
    public boolean isExitWithoutEntry() {
        return null == entry && null != exit;
    }

    /** Momento en que empieza a contar, sea la entrada o -si falta- la salida. */
    public Date getStart() {
        return null != entry ? entry : exit;
    }

    public Date getEnd() {
        return null != exit ? exit : entry;
    }

    /**
     * @return duracion en minutos, o -1 si la sesion esta incompleta y no se puede medir
     */
    public long getMinutes() {
        if (!isComplete()) {
            return -1;
        }
        return (exit.getTime() - entry.getTime()) / 60000L;
    }

    @Override
    public String toString() {
        return "WorkSession{" + entry + " -> " + exit + ", " + pairing + "}";
    }
}
