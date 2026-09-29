package com.encens.khipus.util.employees.attendance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Hora en que se corta el dia cuando la persona NO tiene horario.
     * <p/>
     * Es el <i>day divide</i> de la industria. Sin horario contra que comparar no hay ventana
     * que reparta las marcas, asi que se empareja cronologicamente igual que antes, pero
     * <b>encerrado dentro de cada dia</b>: asi un dia con marcas impares deja de arrastrar el
     * desfase a los siguientes. Se corta a las 03:00 y no a medianoche para que una salida de
     * madrugada siga perteneciendo al dia anterior.
     */
    private static final int DAY_DIVIDE_HOUR = 3;

    /**
     * Menos que esto entre la primera y la ultima marca de una jornada es <b>una sola pasada por
     * el lector</b>, no una entrada y una salida.
     * <p/>
     * {@link #SAME_EVENT_MINUTES} junta las filas repetidas del mismo momento, pero se queda
     * corto con la persona que pasa dos veces con unos minutos de diferencia. En los datos
     * reales: dos marcas a las 07:26 y 07:29 para un turno de 07:30 a 19:30 se leian como una
     * jornada de tres minutos y descontaban casi doce horas.
     * <p/>
     * No alcanza con mirar si las dos marcas estan del lado de la entrada: alguien que entra a
     * su hora y se va a media manana tambien las tiene asi, y esa salida temprana es real y hay
     * que verla. Lo que distingue el caso es que las dos marcas esten <b>pegadas entre si</b>.
     */
    private static final long SAME_PASS_MINUTES = 15;

    private final List<WorkSession> sessions = new ArrayList<WorkSession>();
    private final List<AttendanceIncidence> incidences = new ArrayList<AttendanceIncidence>();

    /** El indicador dijo entrada y salida para el mismo momento: no se puede confiar en el. */
    private boolean indicatorContradicts = false;

    /**
     * @param marks marcas del empleado, con su fecha y hora ya combinadas. Se ordenan aca:
     *              no se asume que vengan en orden.
     */
    public WorkSessionBuilder(List<AttendanceMark> marks) {
        this(marks, new ArrayList<ScheduledJourney>());
    }

    /**
     * @param journeys las jornadas que le tocaban a esa persona en el periodo. Con jornadas, el
     *                 horario guia el emparejamiento; sin ellas se cae al emparejamiento
     *                 cronologico acotado por dia.
     */
    public WorkSessionBuilder(List<AttendanceMark> marks, List<ScheduledJourney> journeys) {
        List<AttendanceMark> ordered = new ArrayList<AttendanceMark>(marks);
        Collections.sort(ordered, new Comparator<AttendanceMark>() {
            public int compare(AttendanceMark a, AttendanceMark b) {
                return a.getMoment().compareTo(b.getMoment());
            }
        });

        ordered = collapseSameEvent(ordered);

        List<ScheduledJourney> sorted = sortedByStart(journeys);
        ScheduledJourney.clipWindows(sorted);

        List<AttendanceMark> loose = pairWithinJourneys(ordered, sorted);

        /* Lo que no cayo en ninguna ventana no se descarta: se empareja como se pueda para que
           la etapa siguiente lo reporte como trabajo fuera de horario. */
        if (indicatorIsUsable(loose)) {
            pairByIndicator(loose);
        } else {
            pairChronologically(loose);
        }
        flagAbnormalSessions();
    }

    private static List<ScheduledJourney> sortedByStart(List<ScheduledJourney> journeys) {
        List<ScheduledJourney> sorted = new ArrayList<ScheduledJourney>(journeys);
        Collections.sort(sorted, new Comparator<ScheduledJourney>() {
            public int compare(ScheduledJourney a, ScheduledJourney b) {
                return a.getStart().compareTo(b.getStart());
            }
        });
        return sorted;
    }

    /**
     * El cambio de fondo del plan 10: <b>la jornada guia el emparejamiento</b>.
     * <p/>
     * Cada marca entra en la ventana de la jornada que la contiene; si cae en dos -turnos
     * partidos, ventanas que se rozan- gana el borde mas cercano. Dentro de cada jornada, la mas
     * temprana es la entrada y la mas tardia la salida, y las del medio se reportan.
     * <p/>
     * Con esto <b>cada jornada se resuelve sola</b>: no hay un hilo cronologico que se corra, asi
     * que un dia con una marca faltante deja de contaminar a los siguientes.
     *
     * @return las marcas que no cayeron en ninguna ventana
     */
    private List<AttendanceMark> pairWithinJourneys(List<AttendanceMark> marks,
                                                    List<ScheduledJourney> journeys) {
        List<AttendanceMark> loose = new ArrayList<AttendanceMark>();
        if (journeys.isEmpty()) {
            loose.addAll(marks);
            return loose;
        }

        Map<ScheduledJourney, List<AttendanceMark>> byJourney
                = new LinkedHashMap<ScheduledJourney, List<AttendanceMark>>();
        for (ScheduledJourney journey : journeys) {
            byJourney.put(journey, new ArrayList<AttendanceMark>());
        }

        for (AttendanceMark mark : marks) {
            ScheduledJourney owner = journeyFor(mark, journeys);
            if (null == owner) {
                loose.add(mark);
            } else {
                byJourney.get(owner).add(mark);
            }
        }

        for (Map.Entry<ScheduledJourney, List<AttendanceMark>> entry : byJourney.entrySet()) {
            buildSessionOf(entry.getKey(), entry.getValue());
        }
        return loose;
    }

    /** La jornada cuya ventana contiene la marca; si hay varias, la del borde mas cercano. */
    private ScheduledJourney journeyFor(AttendanceMark mark, List<ScheduledJourney> journeys) {
        ScheduledJourney best = null;
        long bestDistance = Long.MAX_VALUE;
        for (ScheduledJourney journey : journeys) {
            if (!journey.windowContains(mark.getMoment())) {
                continue;
            }
            long distance = journey.minutesToNearestEdge(mark.getMoment());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = journey;
            }
        }
        return best;
    }

    private void buildSessionOf(ScheduledJourney journey, List<AttendanceMark> marks) {
        if (marks.isEmpty()) {
            /* Sin marcas no se arma sesion: la jornada vacia es lo unico que despues sera falta,
               y de eso se encarga la etapa siguiente. */
            return;
        }

        AttendanceMark first = marks.get(0);
        AttendanceMark last = marks.get(marks.size() - 1);

        /* Todas las marcas de la jornada dentro del mismo puñado de minutos: fue una sola
           pasada. Se queda la primera y las demas se reportan, igual que las del medio. */
        boolean singlePass = minutesBetween(first.getMoment(), last.getMoment()) < SAME_PASS_MINUTES;
        if (singlePass) {
            for (int i = 1; i < marks.size(); i++) {
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.MARK_WITHOUT_PAIR, marks.get(i).getMoment(), null));
            }
            last = first;
        }

        WorkSession session;
        if (1 == marks.size() || singlePass) {
            /* Una sola marca: es entrada o salida segun a que borde de SU jornada este mas
               cerca. Es lo que lee bien el 17:09 de una jornada que termina 17:30 como una
               salida sin entrada, en vez de como una entrada con ocho horas de atraso. */
            if (journey.closerToStart(first.getMoment())) {
                session = new WorkSession(first.getMoment(), null, WorkSession.Pairing.JOURNEY);
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.ENTRY_WITHOUT_EXIT, first.getMoment(), null));
            } else {
                session = new WorkSession(null, first.getMoment(), WorkSession.Pairing.JOURNEY);
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.EXIT_WITHOUT_ENTRY, first.getMoment(), null));
            }
        } else {
            session = new WorkSession(first.getMoment(), last.getMoment(),
                    WorkSession.Pairing.JOURNEY);
            for (int i = 1; i < marks.size() - 1; i++) {
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.MARK_WITHOUT_PAIR, marks.get(i).getMoment(), null));
            }
        }
        session.setJourney(journey);
        sessions.add(session);
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
                    && sameWorkDay(entry.getMoment(), marks.get(index + 1).getMoment())
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

    /**
     * Si dos momentos caen en el mismo dia de trabajo, cortando a {@link #DAY_DIVIDE_HOUR}.
     * Sin esto, la salida de un lunes se empareja con la entrada del martes y el desfase se
     * arrastra hasta el fin del periodo.
     */
    private boolean sameWorkDay(Date one, Date other) {
        return workDay(one) == workDay(other);
    }

    private long workDay(Date moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(moment);
        calendar.add(Calendar.HOUR_OF_DAY, -DAY_DIVIDE_HOUR);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
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
