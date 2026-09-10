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

    /**
     * Minutos de atraso, ya descontada la tolerancia. Cero si entro en hora o antes.
     */
    public int getLatenessMinutes() {
        if (isAbsent() || null == getEntry()) {
            return 0;
        }
        long minutes = minutesBetween(journey.getStart(), getEntry())
                - journey.getEntryToleranceMinutes();
        return minutes <= 0 ? 0 : (int) minutes;
    }

    /**
     * Minutos de salida anticipada, ya descontada la tolerancia.
     * <p/>
     * Una salida asumida no cuenta: no se marco, asi que no se sabe hasta que hora se quedo, y
     * castigarlo como si se hubiera ido temprano seria inventar.
     */
    public int getEarlyExitMinutes() {
        if (isAbsent() || null == getExit() || hasAssumedExit()) {
            return 0;
        }
        long minutes = minutesBetween(getExit(), journey.getEnd())
                - journey.getEarlyExitToleranceMinutes();
        return minutes <= 0 ? 0 : (int) minutes;
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

    /** Minutos efectivamente trabajados, sumando todas las sesiones de la jornada. */
    public long getWorkedMinutes() {
        if (isAbsent()) {
            return 0;
        }
        long minutes = 0;
        for (WorkSession session : assignment.getSessions()) {
            minutes += session.getMinutes();
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

    /** Sin novedades: trabajo, entro en hora, salio en hora y marco las dos puntas. */
    public boolean isOk() {
        return !isAbsent() && !isLate() && !isEarlyExit() && !hasAssumedExit();
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
