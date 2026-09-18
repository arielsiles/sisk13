package com.encens.khipus.util.employees.attendance;

import java.util.List;

/**
 * Cuanta falta deja un dia, mirando todas sus jornadas, y de que clase es.
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
 *
 * <h3>Las tres clases</h3>
 * El dia perdido no siempre cuesta lo mismo, y la planilla necesita saberlo <b>separado</b>:
 * <table>
 *   <tr><td><b>Ausencia</b></td><td>no vino y no tiene excusa</td><td>1 dia, y la planilla lo
 *       duplica</td></tr>
 *   <tr><td><b>Registro</b></td><td>vino y falta una punta</td><td>1 dia o medio, simple</td></tr>
 *   <tr><td><b>Sin goce</b></td><td>licencia aprobada sin goce de haber</td><td>1 dia,
 *       simple</td></tr>
 * </table>
 * El x2 es una <b>sancion</b> y es para quien no vino. Quien marco mal cometio un error de
 * registro; quien tiene licencia sin goce no cometio ninguno: simplemente ese dia no se le paga.
 * <p/>
 * Vive en el motor y no en la pantalla porque la planilla usa exactamente esto: si se duplicara,
 * con el tiempo dirian cosas distintas.
 *
 * @author
 * @version 6.1.0
 */
public final class DayAbsence {

    public static final double FULL_DAY = 1d;
    public static final double HALF_DAY = 0.5d;

    private final int journeys;
    private final int lost;
    private final int lostWithMarks;
    /* Cuanta jornada se perdio, contando cada una entre 0 y 1: una jornada tapada a medias por un
       permiso por horas suma 0,5 y no 1. Sin permisos vale lo mismo que contar jornadas. */
    private final double lostShare;
    private final double lostShareWithMarks;
    private final long lostMinutes;
    private final long scheduledMinutes;
    private final ExcusedDay excused;

    private DayAbsence(int journeys, int lost, int lostWithMarks,
                       double lostShare, double lostShareWithMarks,
                       long lostMinutes, long scheduledMinutes, ExcusedDay excused) {
        this.journeys = journeys;
        this.lost = lost;
        this.lostWithMarks = lostWithMarks;
        this.lostShare = lostShare;
        this.lostShareWithMarks = lostShareWithMarks;
        this.lostMinutes = lostMinutes;
        this.scheduledMinutes = scheduledMinutes;
        this.excused = excused;
    }

    public static DayAbsence of(List<JourneyEvaluation> evaluations) {
        return of(evaluations, null);
    }

    /**
     * @param evaluations las jornadas de UN dia. Sin jornadas no hay nada que perder: ese dia no
     *                    le tocaba trabajar.
     * @param excused     la fecha especial de dia completo que cubre ese dia, o null.
     */
    public static DayAbsence of(List<JourneyEvaluation> evaluations, ExcusedDay excused) {
        int total = 0;
        int lost = 0;
        int withMarks = 0;
        double share = 0d;
        double shareWithMarks = 0d;
        long minutes = 0;
        long scheduled = 0;
        if (null != evaluations) {
            for (JourneyEvaluation evaluation : evaluations) {
                if (null == evaluation || null == evaluation.getJourney()) {
                    continue;
                }
                total++;
                scheduled += evaluation.getScheduledMinutes();
                if (evaluation.isLost()) {
                    lost++;
                    minutes += evaluation.getLostMinutes();
                    share += evaluation.getLostShare();
                    if (!evaluation.isAbsent()) {
                        withMarks++;
                        shareWithMarks += evaluation.getLostShare();
                    }
                }
            }
        }
        return new DayAbsence(total, lost, withMarks, share, shareWithMarks,
                minutes, scheduled, excused);
    }

    /** El dia esta perdonado: permiso, vacacion, feriado propio, con goce de haber. */
    public boolean isExcused() {
        return null != excused && excused.isPaid();
    }

    /**
     * Licencia aprobada <b>sin</b> goce de haber: el dia no se paga, haya marcado o no.
     * <p/>
     * Que no se mire si marco es deliberado. La licencia es un hecho administrativo aprobado; si
     * alguien tiene licencia sin goce y ademas marco, eso es una contradiccion de datos que RRHH
     * tiene que resolver, no algo que el motor deba interpretar solo.
     */
    public boolean isUnpaidLeave() {
        return null != excused && !excused.isPaid();
    }

    /**
     * Cuanto dia se perdio en total: 0, medio, uno, o la parte que el permiso no cubrio.
     * <p/>
     * La regla de siempre no cambia: cada jornada del dia vale su parte -con dos jornadas, media
     * cada una- y perderlas todas cuesta el dia entero. Lo que se agrega es que una jornada tapada
     * por un permiso por horas se pierde solo en la parte NO cubierta: en un periodo de 12 h con
     * permiso de 07:30 a 13:30 y sin marcas, se pierde medio dia, no uno.
     */
    public double getDays() {
        if (0 == journeys || isExcused()) {
            return 0d;
        }
        if (isUnpaidLeave()) {
            return FULL_DAY;
        }
        return round(lostShare / journeys);
    }

    /** No vino y no tiene excusa. Es la unica clase que la planilla duplica. */
    public double getAbsenceDays() {
        return share(lostShare - lostShareWithMarks);
    }

    /** Vino y falta una punta. Se le descuenta el dia, pero no se lo sanciona al doble. */
    public double getRegistryDays() {
        return share(lostShareWithMarks);
    }

    /** Licencia sin goce de haber. */
    public double getUnpaidDays() {
        return isUnpaidLeave() ? getDays() : 0d;
    }

    /**
     * La parte del dia que corresponde a las jornadas perdidas de una clase.
     * <p/>
     * Reparte porque un dia puede perder dos jornadas por causas distintas -la manana sin ninguna
     * marca y la tarde con una sola punta-, y ahi el medio dia de cada una tiene que ir a su
     * clase. Con una sola jornada por dia el reparto es todo o nada.
     */
    private double share(double lostShareOfClass) {
        if (0 == journeys || 0d == lostShareOfClass || isExcused() || isUnpaidLeave()) {
            return 0d;
        }
        return round(lostShareOfClass / journeys);
    }

    /** Dos decimales: un dia partido en tercios no se muestra ni se paga con decimales infinitos. */
    private static double round(double value) {
        return Math.round(value * 100d) / 100d;
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

    public ExcusedDay getExcused() {
        return excused;
    }

    /**
     * Los minutos programados que se perdieron.
     * <p/>
     * Se reporta aparte de los dias a proposito: si alguna vez hace falta valorizar distinto una
     * jornada corta, esa decision es del calculo de planilla y el dato ya esta.
     */
    public long getLostMinutes() {
        if (isExcused()) {
            return 0;
        }
        return isUnpaidLeave() ? scheduledMinutes : lostMinutes;
    }
}
