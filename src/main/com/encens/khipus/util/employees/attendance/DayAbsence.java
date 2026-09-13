package com.encens.khipus.util.employees.attendance;

import java.util.List;

/**
 * Cuanta falta deja un dia, mirando todas sus jornadas.
 * <p/>
 * <pre>
 * pierde TODAS las jornadas del dia    ->  1 dia
 * pierde ALGUNA pero no todas          ->  medio dia
 * no pierde ninguna                    ->  0
 * </pre>
 * <b>Sin umbral de horas.</b> El motor viejo comparaba la duracion de la banda contra la jornada
 * legal -8 h varon, 7 h mujer- para decidir entre un dia y medio, y eso era una adivinanza: con
 * una sola banda corta no sabia si era el dia completo de esa persona o media jornada.
 * <p/>
 * Aca no hace falta adivinar porque el cronograma dice cuantas jornadas tenia el dia. Y cualquier
 * numero fijo se rompe con un caso real: una mujer que trabaja 07:00-14:00 de lunes a viernes y
 * <b>07:00-12:00 los sabados</b>. Ese sabado de cinco horas no es medio dia: es su dia completo.
 * <p/>
 * Es la regla del motor viejo sin la adivinanza. No se agrega nada: se saca lo que sobraba. Y de
 * paso desaparece la pregunta del genero para este calculo.
 * <p/>
 * Vive en el motor y no en la pantalla porque la planilla va a usar exactamente esto: si se
 * duplicara, con el tiempo dirian cosas distintas.
 *
 * @author
 * @version 6.1.0
 */
public final class DayAbsence {

    public static final double FULL_DAY = 1d;
    public static final double HALF_DAY = 0.5d;

    private final int journeys;
    private final int lost;
    private final long lostMinutes;

    private DayAbsence(int journeys, int lost, long lostMinutes) {
        this.journeys = journeys;
        this.lost = lost;
        this.lostMinutes = lostMinutes;
    }

    /**
     * @param evaluations las jornadas de UN dia. Sin jornadas no hay nada que perder: ese dia no
     *                    le tocaba trabajar.
     */
    public static DayAbsence of(List<JourneyEvaluation> evaluations) {
        int total = 0;
        int lost = 0;
        long minutes = 0;
        if (null != evaluations) {
            for (JourneyEvaluation evaluation : evaluations) {
                if (null == evaluation || null == evaluation.getJourney()) {
                    continue;
                }
                total++;
                if (evaluation.isLost()) {
                    lost++;
                    minutes += evaluation.getLostMinutes();
                }
            }
        }
        return new DayAbsence(total, lost, minutes);
    }

    /** Cuanto dia se perdio: 0, medio o uno. */
    public double getDays() {
        if (0 == journeys || 0 == lost) {
            return 0d;
        }
        return lost == journeys ? FULL_DAY : HALF_DAY;
    }

    public boolean isPartial() {
        return lost > 0 && lost < journeys;
    }

    public boolean isTotal() {
        return journeys > 0 && lost == journeys;
    }

    public int getJourneys() {
        return journeys;
    }

    public int getLost() {
        return lost;
    }

    /**
     * Los minutos programados que se perdieron.
     * <p/>
     * Se reporta aparte de los dias a proposito: si alguna vez hace falta valorizar distinto una
     * jornada corta, esa decision es del calculo de planilla y el dato ya esta.
     */
    public long getLostMinutes() {
        return lostMinutes;
    }
}
