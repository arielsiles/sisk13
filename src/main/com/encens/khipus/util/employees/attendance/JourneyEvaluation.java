package com.encens.khipus.util.employees.attendance;

import java.util.Date;

/**
 * El resultado de una jornada: si se cumplio, con cuanto atraso y con cuanto tiempo de mas.
 * <p/>
 * Vivia dentro del DTO de la pantalla de verificacion, y eso era un problema: la planilla no lo
 * podia usar, asi que al conectarla habria habido dos copias de la misma formula y con el tiempo
 * habrian dicho cosas distintas. Aca hay una sola, y lo que se ve en la pantalla es exactamente
 * lo que se va a pagar.
 * <p/>
 * No decide nada de dinero: cuenta minutos. Como se descuenta un atraso o como se paga un
 * tiempo adicional es del calculo de planilla, no de acá.
 *
 * @author
 * @version 6.1.0
 */
public class JourneyEvaluation {

    private final ScheduledJourney journey;
    private final JourneyAssignment assignment;

    public JourneyEvaluation(JourneyAssignment assignment) {
        this.assignment = assignment;
        this.journey = null == assignment ? null : assignment.getJourney();
    }

    public static JourneyEvaluation of(JourneyAssignment assignment) {
        return new JourneyEvaluation(assignment);
    }

    /** Le tocaba trabajar y no hay ninguna marca asociada a la jornada. */
    public boolean isAbsent() {
        return null == assignment || assignment.isEmpty();
    }

    public Date getEntry() {
        return null == assignment ? null : assignment.getFirstEntry();
    }

    public Date getExit() {
        return null == assignment ? null : assignment.getLastExit();
    }

    /** La salida tal como se marco. Null si no se marco: no se inventa una hora. */
    public Date getMarkedExit() {
        return null == assignment ? null : assignment.getLastMarkedExit();
    }

    /** La salida no se marco y se dio por cerrada al fin de la jornada. */
    public boolean hasAssumedExit() {
        return null != assignment && assignment.hasAssumedExit();
    }

    /** La entrada no se marco: se sabe cuando se fue, no cuando llego. */
    public boolean hasAssumedEntry() {
        return null != assignment && assignment.hasAssumedEntry();
    }

    /**
     * Minutos de atraso. Cero si entro en hora, antes, o dentro de la tolerancia.
     * <p/>
     * La tolerancia es un <b>umbral, no un descuento</b>: quien la pasa acumula el atraso
     * completo desde su hora de entrada, no el excedente. Con 10 minutos de tolerancia, entrar
     * 08:12 a una jornada de 07:30 son <b>42 minutos</b> y no 32.
     * <p/>
     * Es lo que dice el SPEC -"pasada la tolerancia se descuenta el atraso completo: quien marca
     * al minuto 11 acumula 11 minutos, no 1"- y lo que ya hace el motor viejo de bandas. Si no
     * coincidieran, al conectar la planilla en E2.9 le cambiaria el atraso a todo el mundo.
     */
    public int getLatenessMinutes() {
        if (isAbsent() || null == getEntry()) {
            return 0;
        }
        long minutes = minutesBetween(journey.getStart(), getEntry());
        return minutes <= journey.getEntryToleranceMinutes() ? 0 : (int) minutes;
    }

    /**
     * Minutos de salida anticipada. Mismo criterio que el atraso: la tolerancia es un umbral y
     * pasada esa raya cuenta el tiempo completo desde el fin de la jornada.
     * <p/>
     * Va igual que la entrada porque asi lo trata el motor viejo -"para entrada y para salida,
     * por separado", con la misma regla- y porque dos criterios distintos para el mismo numero
     * en la misma pantalla no se pueden explicar.
     * <p/>
     * Una salida asumida no cuenta: no se marco, asi que no se sabe hasta que hora se quedo, y
     * castigarlo como si se hubiera ido temprano seria inventar.
     */
    public int getEarlyExitMinutes() {
        if (isAbsent() || null == getExit() || hasAssumedExit()) {
            return 0;
        }
        long minutes = minutesBetween(getExit(), journey.getEnd());
        return minutes <= journey.getEarlyExitToleranceMinutes() ? 0 : (int) minutes;
    }

    /**
     * Minutos que se quedo despues del fin de la jornada. No se descuenta tolerancia: quedarse
     * de mas no se penaliza, se registra. Es el insumo del registro de horas extra.
     */
    public int getExtraMinutes() {
        if (isAbsent() || null == getExit() || hasAssumedExit()) {
            return 0;
        }
        long minutes = minutesBetween(journey.getEnd(), getExit());
        return minutes <= 0 ? 0 : (int) minutes;
    }

    /**
     * Minutos efectivamente trabajados, sumando todas las sesiones de la jornada.
     * <p/>
     * Una sesion incompleta no suma: `getMinutes` devuelve -1 cuando falta una punta, y sumarlo
     * daba <b>tiempo trabajado negativo</b>. No se sabe cuanto estuvo, y no saberlo no es lo
     * mismo que haber estado menos que cero. Cuando la planilla consuma esto -E2.9- ese dia
     * tiene que resolverse antes de pagar, no calcularse con un numero inventado.
     */
    public long getWorkedMinutes() {
        if (isAbsent()) {
            return 0;
        }
        long minutes = 0;
        for (WorkSession session : assignment.getSessions()) {
            if (session.isComplete()) {
                minutes += session.getMinutes();
            }
        }
        return minutes;
    }

    /** Minutos que la jornada exigia. */
    public long getScheduledMinutes() {
        return null == journey ? 0 : journey.getMinutes();
    }

    public boolean isLate() {
        return getLatenessMinutes() > 0;
    }

    public boolean isEarlyExit() {
        return getEarlyExitMinutes() > 0;
    }

    /**
     * Sin novedades: trabajo, entro en hora, salio en hora y marco <b>las dos puntas</b>.
     * <p/>
     * Faltaba exigir la entrada. Un dia con la salida marcada y sin entrada no tiene atraso
     * -no hay con que medirlo- ni salida anticipada, asi que se colaba como "En orden" teniendo
     * la mitad del dato. Ese dia no esta en orden: esta incompleto.
     */
    public boolean isOk() {
        return !isAbsent() && !isLate() && !isEarlyExit()
                && !hasAssumedExit() && !hasAssumedEntry();
    }

    public ScheduledJourney getJourney() {
        return journey;
    }

    public JourneyAssignment getAssignment() {
        return assignment;
    }

    private static long minutesBetween(Date from, Date to) {
        return (to.getTime() - from.getTime()) / 60000L;
    }
}
