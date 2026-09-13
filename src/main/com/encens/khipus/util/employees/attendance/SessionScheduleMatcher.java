package com.encens.khipus.util.employees.attendance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Asocia cada sesion con la jornada que le corresponde, por solapamiento temporal.
 * <p/>
 * Segunda etapa del motor. <b>Aca desaparece `Limit`</b>, y no se reemplaza por otro
 * parametro: se elimina la pregunta. El motor viejo preguntaba, para cada marca y cada banda,
 * si la marca "pertenecia" a esa banda dentro de una ventana rigida de 180 minutos, y si no
 * entraba la descartaba. Con la banda sin dos marcas venia la falta. De ahi salia el peor
 * error posible: alguien que trabajo catorce horas figuraba ausente porque su salida quedo
 * fuera de la ventana.
 * <p/>
 * Aca no hay ventana. Cada sesion se compara contra todas las jornadas y se queda con la que
 * mas se superpone. Si no se superpone con ninguna, es trabajo fuera de horario y se reporta;
 * no se pierde. <b>Y una jornada sin ninguna sesion es lo unico que despues sera falta.</b>
 *
 * @author
 * @version 6.1.0
 */
public class SessionScheduleMatcher {

    /**
     * Cuanto se le presta a una sesion incompleta para poder medir el solapamiento.
     * <p/>
     * Una sesion sin salida es un instante, y un instante casi nunca cae dentro de la jornada:
     * quien entra 07:25 a una jornada que arranca 07:30 quedaria "fuera de horario" y su
     * jornada sin sesion, o sea falta. Justo el error que este motor viene a eliminar. Se le
     * da un tramo hacia adelante -o hacia atras si lo que falta es la entrada- solo para
     * decidir a que jornada pertenece; el tramo no se usa para nada mas.
     */
    private static final long OPEN_SESSION_SPAN_MINUTES = 16 * 60;

    private final List<JourneyAssignment> assignments = new ArrayList<JourneyAssignment>();
    private final List<AttendanceIncidence> incidences = new ArrayList<AttendanceIncidence>();

    public SessionScheduleMatcher(List<WorkSession> sessions, List<ScheduledJourney> journeys) {
        for (ScheduledJourney journey : journeys) {
            assignments.add(new JourneyAssignment(journey));
        }

        for (WorkSession session : sessions) {
            /* Si la sesion ya sabe de que jornada es -porque se armo dentro de su ventana- se
               respeta esa decision. Volver a deducirla por solapamiento podria contradecirla, y
               la primera lectura es la que tuvo el horario a la vista. */
            JourneyAssignment declared = assignmentOf(session.getJourney());
            if (null != declared) {
                declared.add(session);
                continue;
            }

            /* Con jornadas resueltas, una sesion sin jornada declarada es una que se armo con
               marcas que NO cayeron en ninguna ventana. Eso ya es la respuesta: trabajo fuera de
               horario. Buscarle jornada por solapamiento revierte esa decision por atras, y con
               el tramo artificial que se le presta a una sesion incompleta puede alcanzar la
               jornada del dia anterior. Es lo que hacia que un feriado -sin jornada, marcas
               sueltas- le pusiera al dia previo una salida de la manana siguiente. */
            if (!assignments.isEmpty()) {
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.SESSION_WITHOUT_SCHEDULE, session.getStart(), null));
                continue;
            }

            JourneyAssignment best = null;
            long bestOverlap = 0;
            for (JourneyAssignment assignment : assignments) {
                /* Una sesion que cruza la medianoche solo puede pertenecer a una jornada que
                   tambien cruza. La asimetria importa: quien entra 19:30 y sale 23:00 en un turno
                   de noche NO cruza y sigue siendo de ese turno. Lo imposible es al reves, y era
                   como una jornada de oficina de 09:00 a 17:30 se quedaba con una sesion de
                   17:09 a 07:45 del dia siguiente, por 21 minutos de solapamiento. */
                if (session.crossesMidnight() && !assignment.getJourney().crossesMidnight()) {
                    continue;
                }
                long overlap = overlapMinutes(session, assignment.getJourney());
                if (overlap > bestOverlap) {
                    bestOverlap = overlap;
                    best = assignment;
                }
            }
            if (null != best) {
                best.add(session);
            } else {
                /* Trabajo fuera de horario: se reporta, no se descarta. */
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.SESSION_WITHOUT_SCHEDULE, session.getStart(), null));
            }
        }

        for (JourneyAssignment assignment : assignments) {
            if (assignment.isEmpty()) {
                incidences.add(new AttendanceIncidence(
                        AttendanceIncidenceType.SCHEDULE_WITHOUT_SESSION,
                        assignment.getJourney().getStart(), null));
            }
        }
    }

    private JourneyAssignment assignmentOf(ScheduledJourney journey) {
        if (null == journey) {
            return null;
        }
        for (JourneyAssignment assignment : assignments) {
            if (assignment.getJourney() == journey) {
                return assignment;
            }
        }
        return null;
    }

    /**
     * Minutos en que la sesion y la jornada se pisan. Cero si no se tocan.
     */
    private long overlapMinutes(WorkSession session, ScheduledJourney journey) {
        Date from = matchingStart(session);
        Date to = matchingEnd(session);
        long start = Math.max(from.getTime(), journey.getStart().getTime());
        long end = Math.min(to.getTime(), journey.getEnd().getTime());
        return end <= start ? 0 : (end - start) / 60000L;
    }

    private Date matchingStart(WorkSession session) {
        if (session.isExitWithoutEntry()) {
            return new Date(session.getExit().getTime() - OPEN_SESSION_SPAN_MINUTES * 60000L);
        }
        return session.getEntry();
    }

    private Date matchingEnd(WorkSession session) {
        if (session.isEntryWithoutExit()) {
            return new Date(session.getEntry().getTime() + OPEN_SESSION_SPAN_MINUTES * 60000L);
        }
        return session.getExit();
    }

    public List<JourneyAssignment> getAssignments() {
        return assignments;
    }

    public List<AttendanceIncidence> getIncidences() {
        return incidences;
    }

    /** Las jornadas que no tuvieron ninguna sesion: las unicas que seran falta. */
    public List<JourneyAssignment> getAbsences() {
        List<JourneyAssignment> result = new ArrayList<JourneyAssignment>();
        for (JourneyAssignment assignment : assignments) {
            if (assignment.isEmpty()) {
                result.add(assignment);
            }
        }
        return result;
    }
}
