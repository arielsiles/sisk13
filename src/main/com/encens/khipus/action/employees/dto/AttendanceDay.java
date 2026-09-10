package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.employees.RH_Mark;
import com.encens.khipus.service.employees.JourneyResolverService.JourneySource;
import com.encens.khipus.util.employees.attendance.JourneyAssignment;
import com.encens.khipus.util.employees.attendance.JourneyEvaluation;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Un dia de la verificacion: que le tocaba, de donde salio eso, que marco y como cierra.
 * <p/>
 * Existe para poder explicarle a una persona por que se le conto un atraso. Sin esta pantalla
 * la unica respuesta posible seria "lo dice el sistema", que no alcanza cuando alguien reclama.
 *
 * @author
 * @version 6.1.0
 */
public class AttendanceDay implements Serializable {

    private Date day;
    private ScheduledJourney journey;
    private JourneySource source;
    private JourneyAssignment assignment;
    private List<RH_Mark> marks = new ArrayList<RH_Mark>();

    /* La jornada del dia anterior termina en este dia: es una noche que cruza la medianoche. */
    private boolean coveredByPreviousJourney;

    /* De que grupo salio la jornada. Sin esto, un dia prestado se ve identico a uno normal
       y no habria forma de explicar por que se evaluo contra otro horario. */
    private String groupName;
    private boolean loaned;

    /* Lo que le habria tocado si el dia no fuera feriado. Solo para mostrar. */
    private ScheduledJourney suppressedJourney;

    /* Es feriado para esta persona. Es un hecho del DIA y no el origen de la jornada:
       un feriado planificado y trabajado -produccion- tiene jornada y sigue siendo
       feriado. Derivarlo del origen lo escondia justo en el caso que mas importa ver,
       que es el que se paga distinto. */
    private boolean holiday;

    public AttendanceDay(Date day) {
        this.day = day;
    }

    /** Ese dia le tocaba trabajar. Sin jornada no se evalua nada: no hay falta ni atraso. */
    public boolean isWorking() {
        return null != journey;
    }

    public Date getExpectedEntry() {
        return null == journey ? null : journey.getStart();
    }

    public Date getExpectedExit() {
        return null == journey ? null : journey.getEnd();
    }

    public Date getRealEntry() {
        return null == assignment ? null : assignment.getFirstEntry();
    }

    /**
     * La salida tal como se marco. Vacia si no se marco.
     * <p/>
     * A proposito NO devuelve el cierre que asume el motor: la columna "Marco" muestra lo que
     * la persona marco, y mezclar ahi una hora calculada seria mentir.
     */
    public Date getRealExit() {
        return null == assignment ? null : assignment.getLastMarkedExit();
    }

    /**
     * La evaluacion de la jornada. Vive en el motor y no aca: la planilla usa la misma, asi
     * que lo que muestra esta pantalla es exactamente lo que se va a pagar.
     */
    public JourneyEvaluation getEvaluation() {
        return JourneyEvaluation.of(assignment);
    }

    /** La salida no se marco y se dio por cerrada al fin de la jornada. */
    public boolean isAssumedExit() {
        return getEvaluation().hasAssumedExit();
    }

    /** Le tocaba trabajar y no hay ninguna marca asociada. */
    public boolean isAbsent() {
        return isWorking() && getEvaluation().isAbsent();
    }

    public int getLatenessMinutes() {
        return isWorking() ? getEvaluation().getLatenessMinutes() : 0;
    }

    public int getEarlyExitMinutes() {
        return isWorking() ? getEvaluation().getEarlyExitMinutes() : 0;
    }

    public int getExtraMinutes() {
        return isWorking() ? getEvaluation().getExtraMinutes() : 0;
    }

    public boolean isLate() {
        return getLatenessMinutes() > 0;
    }

    public boolean isEarlyExit() {
        return getEarlyExitMinutes() > 0;
    }

    /** Sin novedades: trabajo, entro en hora y salio en hora. */
    public boolean isOk() {
        return isWorking() && !isAbsent() && !isLate() && !isEarlyExit() && !isAssumedExit();
    }

    /** Ese dia es feriado, haya o no jornada. */
    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean holiday) {
        this.holiday = holiday;
    }

    /** Es feriado y por eso NO hay jornada: su horario fijo decia que se trabajaba. */
    public boolean isSuppressedByHoliday() {
        return JourneySource.HOLIDAY.equals(source);
    }

    /**
     * Trabajo un feriado, con jornada planificada o sin ella. No es una novedad menor: esas
     * horas se pagan distinto o se acumulan, asi que tienen que verse siempre.
     */
    public boolean isWorkedOnHoliday() {
        return holiday && !marks.isEmpty();
    }

    /**
     * Marco algo un dia que no le tocaba. No es un error, pero conviene verlo.
     * <p/>
     * No cuenta el viernes de una semana de noche: esa marca de las 07:30 es la SALIDA de la
     * jornada del jueves, no una marca suelta. Sin esta excepcion la pantalla acusaria de
     * "marco sin jornada" todos los viernes del mes, que es ruido y confunde.
     */
    public boolean isMarkedWithoutJourney() {
        return !isWorking() && !coveredByPreviousJourney && !holiday && !marks.isEmpty();
    }

    /** Ese dia solo tiene la salida de la noche anterior. */
    public boolean isPreviousJourneyExit() {
        return !isWorking() && coveredByPreviousJourney && !marks.isEmpty();
    }

    public void setCoveredByPreviousJourney(boolean coveredByPreviousJourney) {
        this.coveredByPreviousJourney = coveredByPreviousJourney;
    }

    public ScheduledJourney getSuppressedJourney() {
        return suppressedJourney;
    }

    public void setSuppressedJourney(ScheduledJourney suppressedJourney) {
        this.suppressedJourney = suppressedJourney;
    }

    /**
     * La jornada a mostrar: la real, o la que el feriado suspendio.
     * <p/>
     * En un feriado las horas se muestran igual que cualquier otro dia. Que sea feriado ya lo
     * dice la etiqueta junto a la fecha, y ahi se acaba: para quien trabajo el feriado la
     * jornada no tiene nada de suspendida.
     */
    public ScheduledJourney getShownJourney() {
        return null != journey ? journey : suppressedJourney;
    }

    public boolean isShowingJourney() {
        return null != getShownJourney();
    }

    public Double getShownHours() {
        return null == getShownJourney() ? null : getShownJourney().getMinutes() / 60d;
    }

    public Date getShownEntry() {
        return null == getShownJourney() ? null : getShownJourney().getStart();
    }

    public Date getShownExit() {
        return null == getShownJourney() ? null : getShownJourney().getEnd();
    }

    /**
     * Primera y ultima marca del dia. En un feriado no hay jornada contra la cual medir, y
     * sin embargo hay que poder ver a que hora entro y salio quien vino a trabajar.
     */
    public Date getFirstMarkTime() {
        return marks.isEmpty() ? null : marks.get(0).getMarTime();
    }

    public Date getLastMarkTime() {
        return marks.isEmpty() ? null : marks.get(marks.size() - 1).getMarTime();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /** Ese dia estaba prestado a otro grupo. */
    public boolean isLoaned() {
        return loaned;
    }

    public void setLoaned(boolean loaned) {
        this.loaned = loaned;
    }

    public String getRowStyleClass() {
        if (isAbsent()) {
            return "app-check__row app-check__row--absent";
        }
        if (isLate() || isEarlyExit() || isAssumedExit()) {
            return "app-check__row app-check__row--warn";
        }
        if (holiday) {
            return "app-check__row app-check__row--holiday";
        }
        if (!isWorking()) {
            return "app-check__row app-check__row--rest";
        }
        return "app-check__row";
    }

    public Date getDay() {
        return day;
    }

    public ScheduledJourney getJourney() {
        return journey;
    }

    public void setJourney(ScheduledJourney journey) {
        this.journey = journey;
    }

    public JourneySource getSource() {
        return source;
    }

    public void setSource(JourneySource source) {
        this.source = source;
    }

    public JourneyAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(JourneyAssignment assignment) {
        this.assignment = assignment;
    }

    public List<RH_Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<RH_Mark> marks) {
        this.marks = marks;
    }
}
