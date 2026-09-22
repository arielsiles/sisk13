package com.encens.khipus.action.employees.dto;

import com.encens.khipus.util.employees.attendance.DayAbsence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Los numeros de un periodo: lo que la pantalla de verificacion muestra y lo que la planilla
 * descuenta, calculados <b>una sola vez</b> a partir de los mismos dias.
 * <p/>
 * Es la pieza que sostiene el resto: mientras la pantalla sumaba por su cuenta y la planilla
 * calculaba por la suya, las dos podian dar distinto y no habia forma de decir cual mentia. Con
 * una sola sumadora, si el recibo no coincide con la pantalla es un defecto y no una diferencia
 * de criterio.
 * <p/>
 * <b>No decide dinero.</b> Cuenta dias y minutos; a cuanto se paga cada uno es del calculo de
 * planilla. La unica regla de costo que vive aca es la separacion de las faltas en clases, porque
 * es una propiedad del hecho -por que se perdio el dia- y no del precio.
 *
 * @author
 * @version 6.1.0
 */
public class AttendancePeriodSummary implements Serializable {

    private int journeyDays;
    private int workedDays;
    private int excusedDays;

    private double absenceDays;
    private double registryDays;
    private double unpaidDays;

    private int lateDays;
    private int latenessMinutes;
    private final List<Integer> latenessByDay = new ArrayList<Integer>();
    private int earlyExitMinutes;
    private int extraMinutes;

    private long scheduledMinutes;
    private long workedMinutes;
    private long lostMinutes;

    /* Los dias que conviene mirar antes de oficializar: la persona estuvo y falta una marca, asi
       que el descuento se puede evitar corrigiendo el registro. */
    private final List<AttendanceDay> reviewDays = new ArrayList<AttendanceDay>();

    /* Los dias tal cual, para que quien necesite el detalle -el rastro de la planilla- no tenga
       que volver a pedirlos ni recalcularlos. */
    private List<AttendanceDay> days = new ArrayList<AttendanceDay>();

    public static AttendancePeriodSummary of(List<AttendanceDay> days) {
        AttendancePeriodSummary summary = new AttendancePeriodSummary();
        if (null == days) {
            return summary;
        }
        summary.days = days;
        for (AttendanceDay day : days) {
            if (!day.isWorking()) {
                continue;
            }
            summary.journeyDays++;
            summary.scheduledMinutes += day.getEvaluation().getScheduledMinutes();
            summary.workedMinutes += day.getEvaluation().getWorkedMinutes();

            if (day.isExcused()) {
                summary.excusedDays++;
            }
            if (!day.isLost()) {
                summary.workedDays++;
            }

            DayAbsence absence = day.getAbsence();
            summary.absenceDays += absence.getAbsenceDays();
            summary.registryDays += absence.getRegistryDays();
            summary.unpaidDays += absence.getUnpaidDays();
            summary.lostMinutes += absence.getLostMinutes();

            summary.latenessMinutes += day.getLatenessMinutes();
            if (day.getLatenessMinutes() > 0) {
                /* Cada atraso por separado, no solo la suma: hay politicas que cobran por evento
                   -cuatro atrasos de diez minutos no son lo mismo que uno de cuarenta-. */
                summary.latenessByDay.add(Integer.valueOf(day.getLatenessMinutes()));
            }
            summary.earlyExitMinutes += day.getEarlyExitMinutes();
            summary.extraMinutes += day.getExtraMinutes();
            if (day.isLate()) {
                summary.lateDays++;
            }
            if (day.isLostWithMarks()) {
                summary.reviewDays.add(day);
            }
        }
        return summary;
    }

    /**
     * Los dias que la planilla descuenta del sueldo.
     * <pre>
     * ausencia x 2  +  registro  +  sin goce
     * </pre>
     * El x2 es una <b>sancion</b> y es solo para quien no vino. Quien vino y marco mal cometio un
     * error de registro; quien tiene licencia sin goce no cometio ninguno. Cobrarles el doble a
     * los tres era castigar lo mismo tres cosas distintas.
     */
    public double getChargedDays() {
        return absenceDays * 2 + registryDays + unpaidDays;
    }

    /** Los dias perdidos sin duplicar nada. Es lo que se muestra, no lo que se cobra. */
    public double getLostDays() {
        return absenceDays + registryDays + unpaidDays;
    }

    public boolean hasDaysToReview() {
        return !reviewDays.isEmpty();
    }

    public int getReviewDayCount() {
        return reviewDays.size();
    }

    public List<AttendanceDay> getReviewDays() {
        return reviewDays;
    }

    public List<AttendanceDay> getDays() {
        return days;
    }

    public int getJourneyDays() {
        return journeyDays;
    }

    public int getWorkedDays() {
        return workedDays;
    }

    public int getExcusedDays() {
        return excusedDays;
    }

    public double getAbsenceDays() {
        return absenceDays;
    }

    public double getRegistryDays() {
        return registryDays;
    }

    public double getUnpaidDays() {
        return unpaidDays;
    }

    public int getLateDays() {
        return lateDays;
    }

    /** Los minutos de cada dia con atraso, en orden. Los dias sin atraso no entran. */
    public List<Integer> getLatenessByDay() {
        return latenessByDay;
    }

    public int getLatenessMinutes() {
        return latenessMinutes;
    }

    public int getEarlyExitMinutes() {
        return earlyExitMinutes;
    }

    public int getExtraMinutes() {
        return extraMinutes;
    }

    public long getScheduledMinutes() {
        return scheduledMinutes;
    }

    public long getWorkedMinutes() {
        return workedMinutes;
    }

    public long getLostMinutes() {
        return lostMinutes;
    }

    public Double getScheduledHours() {
        return scheduledMinutes / 60d;
    }
}
