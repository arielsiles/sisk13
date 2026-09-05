package com.encens.khipus.util.employees.attendance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Arma las sesiones de trabajo a partir de las marcas crudas.
 * <p/>
 * Primera etapa del motor de asistencia, y la unica que no conoce el horario. Su regla es que
 * <b>nada se descarta</b>: lo que no se puede emparejar sale como sesion incompleta y como
 * incidencia, nunca como falta. La falta es asunto de la etapa que compara con la jornada.
 * <p/>
 * Dos formas de emparejar, y la eleccion es por dato y no por configuracion:
 * <p/>
 * <b>Por el indicador</b> del dispositivo, cuando es confiable. <b>Cronologicamente</b>
 * -primera con segunda, tercera con cuarta- cuando no lo es. El historico no lo es: en los
 * ultimos doce meses la base tiene 24.547 marcas grabadas como entrada y 256 como salida,
 * porque el dispositivo grababa casi todo como entrada. Lo que se importa del archivo si viene
 * balanceado. El motor tiene que funcionar en los dos escenarios y por eso mira los datos que
 * le tocan en lugar de confiar en una bandera global.
 *
 * @author
 * @version 6.1.0
 */
public class WorkSessionBuilder {

    /* Codificacion de `control` en rh_marcado. */
    public static final int CONTROL_ENTRY = 1;
    public static final int CONTROL_EXIT = 3;

    /** Mas que esto es casi seguro un error de marcado, no una jornada. */
    private static final long ABNORMAL_MINUTES = 16 * 60;

    /** Filas del mismo momento: el lector registrando una sola pasada varias veces. */
    private static final long SAME_EVENT_MINUTES = 2;

    /** Proporcion de transiciones que deben alternar para confiar en el indicador. */
    private static final double MIN_ALTERNATION = 0.90;

    private final List<WorkSession> sessions = new ArrayList<WorkSession>();
    private final List<AttendanceIncidence> incidences = new ArrayList<AttendanceIncidence>();

    /** El indicador dijo entrada y salida para el mismo momento: no se puede confiar en el. */
    private boolean indicatorContradicts = false;

    /**
     * @param marks marcas del empleado, con su fecha y hora ya combinadas. Se ordenan aca:
     *              no se asume que vengan en orden.
     */
    public WorkSessionBuilder(List<AttendanceMark> marks) {
        List<AttendanceMark> ordered = new ArrayList<AttendanceMark>(marks);
        Collections.sort(ordered, new Comparator<AttendanceMark>() {
            public int compare(AttendanceMark a, AttendanceMark b) {
                return a.getMoment().compareTo(b.getMoment());
            }
        });

        ordered = collapseSameEvent(ordered);

        if (indicatorIsUsable(ordered)) {
            pairByIndicator(ordered);
        } else {
            pairChronologically(ordered);
        }
        flagAbnormalSessions();
    }

    /**
     * El dispositivo repite la misma marca, y no siempre identica: en los datos reales el
     * mismo minuto llega como entrada y como salida a la vez -"07:35E 07:35E 07:35S"-. Todas
     * esas filas son <b>un solo hecho</b>: la persona paso una vez por el lector.
     * <p/>
     * La importacion las guarda todas a proposito, porque no puede descartar un registro del
     * dispositivo. El motor las junta, que es distinto: no borra nada, lee un evento donde hay
     * varias filas del mismo momento.
     * <p/>
     * Se juntan las que caen dentro de {@link #SAME_EVENT_MINUTES}. Si dentro de un grupo el
     * indicador se contradice, queda anotado: es la senal de que no se puede confiar en el.
     */
    private List<AttendanceMark> collapseSameEvent(List<AttendanceMark> marks) {
        List<AttendanceMark> result = new ArrayList<AttendanceMark>();
        int index = 0;
        while (index < marks.size()) {
            AttendanceMark first = marks.get(index);
            int control = first.getControl();
            int next = index + 1;
            while (next < marks.size()
                    && minutesBetween(first.getMoment(), marks.get(next).getMoment()) <= SAME_EVENT_MINUTES) {
                if (marks.get(next).getControl() != control) {
                    indicatorContradicts = true;
                }
                next++;
            }
            result.add(first);
            index = next;
        }
        return result;
    }

    private long minutesBetween(Date from, Date to) {
        return Math.abs(to.getTime() - from.getTime()) / 60000L;
    }

    /**
     * Mide si el indicador entrada/salida sirve, en vez de suponerlo.
     * <p/>
     * Un indicador sano alterna: entrada, salida, entrada, salida. Se cuenta que proporcion de
     * transiciones alternan y se lo usa solo si alcanza {@link #MIN_ALTERNATION}. Si no, se
     * empareja cronologicamente, que es el respaldo que pide el SPEC.
     * <p/>
     * Hace falta medirlo porque el dato es desparejo: el historico de la base tiene 24.547
     * marcas grabadas como entrada contra 256 como salida -el dispositivo grababa casi todo
     * como entrada- y en los datos importados el mismo minuto llega como entrada y como salida
     * a la vez. Una bandera de configuracion no puede cubrir los dos casos; el conteo si.
     */
    private boolean indicatorIsUsable(List<AttendanceMark> marks) {
        if (indicatorContradicts || marks.size() < 2) {
            return false;
        }
        int alterna = 0;
        for (int i = 1; i < marks.size(); i++) {
            if (marks.get(i).getControl() != marks.get(i - 1).getControl()) {
                alterna++;
            }
        }
        return (double) alterna / (marks.size() - 1) >= MIN_ALTERNATION;
    }

    private void pairByIndicator(List<AttendanceMark> marks) {
        AttendanceMark open = null;
        for (AttendanceMark mark : marks) {
            if (CONTROL_EXIT == mark.getControl()) {
                if (null != open) {
                    sessions.add(new WorkSession(open.getMoment(), mark.getMoment(),
                            WorkSession.Pairing.INDICATOR));
                    open = null;
                } else {
                    /* Salida sin su entrada: pasa cuando la jornada empezo el dia anterior. */
                    sessions.add(new WorkSession(null, mark.getMoment(),
                            WorkSession.Pairing.INDICATOR));
                    incidences.add(new AttendanceIncidence(
                            AttendanceIncidenceType.EXIT_WITHOUT_ENTRY, mark.getMoment(), null));
                }
            } else {
                if (null != open) {
                    /* Dos entradas seguidas: la primera se quedo sin salida. */
                    closeAsEntryWithoutExit(open);
                }
                open = mark;
            }
        }
        if (null != open) {
            closeAsEntryWithoutExit(open);
        }
    }

    /**
     * Primera con segunda, tercera con cuarta. Si sobra una marca queda como sesion incompleta:
     * hay que evaluarla igual, porque su entrada existe y es valida.
     * <p/>
     * Dos marcas no se emparejan si estan separadas por mas de {@link #ABNORMAL_MINUTES}. Sin
     * ese limite, un dia con marcas impares arrastra el desfase a todos los dias siguientes y
     * la salida de un lunes termina emparejada con la entrada del martes.
     */
    private void pairChronologically(List<AttendanceMark> marks) {
        int index = 0;
        while (index < marks.size()) {
            AttendanceMark entry = marks.get(index);
            boolean hasNext = index + 1 < marks.size();
            boolean closeEnough = hasNext
                    && minutesBetween(entry.getMoment(), marks.get(index + 1).getMoment()) <= ABNORMAL_MINUTES;
            if (closeEnough) {
                sessions.add(new WorkSession(entry.getMoment(), marks.get(index + 1).getMoment(),
                        WorkSession.Pairing.CHRONOLOGICAL));
                index += 2;
            } else {
                sessions.add(new WorkSession(entry.getMoment(), null,
                        WorkSession.Pairing.CHRONOLOGICAL));
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.ENTRY_WITHOUT_EXIT, entry.getMoment(), null));
                index += 1;
            }
        }
    }

    private void closeAsEntryWithoutExit(AttendanceMark entry) {
        sessions.add(new WorkSession(entry.getMoment(), null, WorkSession.Pairing.INDICATOR));
        incidences.add(new AttendanceIncidence(
                AttendanceIncidenceType.ENTRY_WITHOUT_EXIT, entry.getMoment(), null));
    }

    private void flagAbnormalSessions() {
        for (WorkSession session : sessions) {
            if (session.isComplete() && session.getMinutes() > ABNORMAL_MINUTES) {
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.ABNORMALLY_LONG_SESSION, session.getEntry(),
                        session.getMinutes() / 60 + " horas"));
            }
        }
    }

    public List<WorkSession> getSessions() {
        return sessions;
    }

    public List<AttendanceIncidence> getIncidences() {
        return incidences;
    }
}
