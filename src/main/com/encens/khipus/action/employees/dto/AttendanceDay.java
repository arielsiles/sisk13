package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.employees.RH_Mark;
import com.encens.khipus.service.employees.JourneyResolverService.JourneySource;
import com.encens.khipus.util.employees.attendance.JourneyAssignment;
import com.encens.khipus.util.employees.attendance.DayAbsence;
import com.encens.khipus.util.employees.attendance.ExcusedDay;
import com.encens.khipus.util.employees.attendance.ExcusedInterval;
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

    /* La fecha especial de dia completo que cubre este dia: permiso, vacacion, maternidad. No
       incluye el feriado, que tiene su propio efecto -suprime la jornada del horario fijo, pero
       no la del cronograma, porque produccion trabaja los feriados-. */
    private ExcusedDay excused;

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

    /** Marco la salida y no la entrada: el dia esta incompleto, no en orden. */
    public boolean isAssumedEntry() {
        return isWorking() && getEvaluation().hasAssumedEntry();
    }

    /** Le tocaba trabajar y no hay ninguna marca asociada. */
    public boolean isAbsent() {
        return isWorking() && getEvaluation().isAbsent();
    }

    /**
     * La jornada se perdio: sin marcas, o con una sola punta. Las dos cuestan lo mismo, porque
     * en las dos falta la prueba de que se trabajo el dia.
     */
    public boolean isLost() {
        return isWorking() && getEvaluation().isLost();
    }

    /**
     * Cuanto cuesta el dia y por que. Es la misma clase que consume la planilla: si la pantalla
     * sumara por su cuenta, con el tiempo dirian cosas distintas.
     */
    public DayAbsence getAbsence() {
        return DayAbsence.of(java.util.Collections.singletonList(getEvaluation()), excused);
    }

    /**
     * Cuanto dia se perdio. Hoy hay una jornada por dia, asi que da 0 o 1; el medio dia aparece
     * con los turnos partidos, cuando se pierda un bloque y no el otro.
     */
    public double getAbsenceDays() {
        return getAbsence().getDays();
    }

    /**
     * Ese dia tiene un permiso POR HORAS. Tiene que verse: si no, alguien mira una entrada tarde
     * sin minutos de atraso y no tiene forma de saber por que no se le contaron.
     */
    public boolean isPartiallyExcused() {
        return isWorking() && !getJourney().getExcusedIntervals().isEmpty();
    }

    /** Los tramos justificados de ese dia, para mostrarlos: "14:00-18:00". */
    public String getExcusedIntervalsLabel() {
        if (!isPartiallyExcused()) {
            return null;
        }
        /* El formateador se crea aca y no como constante: SimpleDateFormat no es seguro entre
           hilos, y una pantalla la miran varios a la vez. */
        java.text.SimpleDateFormat hour = new java.text.SimpleDateFormat("HH:mm");
        StringBuilder label = new StringBuilder();
        for (ExcusedInterval interval : getJourney().getExcusedIntervals()) {
            if (label.length() > 0) {
                label.append(", ");
            }
            label.append(hour.format(interval.getStart()))
                    .append("-")
                    .append(hour.format(interval.getEnd()));
        }
        return label.toString();
    }

    /**
     * La jornada figura perdida, pero un permiso por horas la cubrio entera: no cuesta nada.
     * <p/>
     * Pasa cuando el permiso tapa todo el periodo -07:30 a 19:30 en un turno de 12 h-. Sin esto
     * la pantalla diria "Falta" en un dia que la planilla no descuenta, y no habria forma de
     * explicar la diferencia.
     */
    public boolean isCoveredByLeave() {
        return isLost() && !isExcused() && !isUnpaidLeave() && 0d == getAbsence().getDays();
    }

    /** El dia esta perdonado: permiso, vacacion o maternidad con goce de haber. */
    public boolean isExcused() {
        return getAbsence().isExcused();
    }

    /** Licencia aprobada sin goce de haber: el dia no se paga, pero no se sanciona al doble. */
    public boolean isUnpaidLeave() {
        return getAbsence().isUnpaidLeave();
    }

    public ExcusedDay getExcused() {
        return excused;
    }

    public void setExcused(ExcusedDay excused) {
        this.excused = excused;
    }

    /**
     * Se perdio la jornada pero SI hubo marcas: falta la otra punta.
     * <p/>
     * Un dia perdonado no entra: no hay nada que revisar ni que corregir en un permiso.
     */
    public boolean isLostWithMarks() {
        return isLost() && !isAbsent() && !isExcused() && !isUnpaidLeave();
    }

    /**
     * La clave i18n del motivo del permiso. Las filas cargadas antes de la 6.1.0 no tienen
     * motivo, y ahi se dice lo unico que se sabe con certeza: que el dia esta justificado.
     */
    public String getExcusedReasonKey() {
        return (null == excused || null == excused.getResourceKey())
                ? "AttendanceCheck.excusedDefault" : excused.getResourceKey();
    }

    /**
     * Minutos de atraso que se cobran.
     * <p/>
     * Un dia perdonado no acumula: si el dia esta cubierto por un permiso de dia completo, una
     * vacacion o una baja por maternidad, la persona ni siquiera tenia que venir, asi que cobrarle
     * el atraso de ese dia es descontar por presentarse. Lo mismo con la licencia sin goce, que ya
     * no se paga: sumarle el atraso seria descontar dos veces el mismo dia.
     * <p/>
     * El permiso POR HORAS no entra aca: ese perdona su tramo y lo que sobra si se cobra, que es
     * lo que hace la evaluacion de la jornada.
     */
    public int getLatenessMinutes() {
        return chargeable() ? getEvaluation().getLatenessMinutes() : 0;
    }

    /** Misma regla que el atraso: un dia perdonado no acumula salida anticipada. */
    public int getEarlyExitMinutes() {
        return chargeable() ? getEvaluation().getEarlyExitMinutes() : 0;
    }

    /** El dia se evalua y ademas se cobra: le tocaba trabajar y no esta perdonado. */
    private boolean chargeable() {
        return isWorking() && !isExcused() && !isUnpaidLeave();
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

    /**
     * Sin novedades. La regla vive en {@link JourneyEvaluation} y aca solo se consulta.
     * <p/>
     * Estaba repetida, y paso lo que siempre pasa: al exigir tambien la marca de entrada se
     * corrigio la del motor y esta quedo como estaba, asi que la pantalla mostraba a la vez
     * "Sin marca de entrada" y "En orden". La evaluacion es la misma que va a usar la planilla:
     * si se duplica, con el tiempo dicen cosas distintas.
     */
    public boolean isOk() {
        return isWorking() && !isExcused() && !isUnpaidLeave() && getEvaluation().isOk();
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

    /**
     * Ese dia tiene la salida de la noche anterior.
     * <p/>
     * Tambien cuando el dia SI tenia jornada y quedo como falta: ahi la fila muestra una marca y
     * dice "Falta", y sin esta aclaracion se lee como una contradiccion. La marca es real, pero
     * cierra la jornada del dia anterior, no la de este.
     */
    public boolean isPreviousJourneyExit() {
        return coveredByPreviousJourney && !marks.isEmpty() && (!isWorking() || isAbsent());
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
        /* Un dia perdonado se pinta como descanso aunque la jornada figure perdida: no se le
           descuenta nada, asi que pintarlo de falta seria alarmar por nada. */
        if (isExcused()) {
            return "app-check__row app-check__row--rest";
        }
        /* Todo lo que cuesta dinero se ve igual de fuerte: la falta de siempre, la jornada
           perdida por marca incompleta y la licencia sin goce. */
        if (getAbsenceDays() > 0) {
            return "app-check__row app-check__row--absent";
        }
        /* Toda novedad pinta la fila. Al agregar un estado hay que sumarlo aca tambien: la
           entrada faltante quedo sin color en la primera version, y una fila sin color se lee
           como una fila sin problema. */
        if (isLate() || isEarlyExit() || isAssumedExit() || isAssumedEntry()) {
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
