package com.encens.khipus.util.employees.attendance;

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
        CHRONOLOGICAL
    }

    private final Date entry;
    private final Date exit;
    private final Pairing pairing;

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
