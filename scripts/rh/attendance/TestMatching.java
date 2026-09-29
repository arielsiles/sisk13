import com.encens.khipus.util.employees.attendance.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Criterios de aceptacion de RF-12, etapa 2: asociacion sesion-jornada. */
public class TestMatching {
    static SimpleDateFormat DT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    static SimpleDateFormat H = new SimpleDateFormat("HH:mm");
    static SimpleDateFormat D = new SimpleDateFormat("dd/MM/yyyy");
    static int ok = 0, fail = 0;

    static Date t(String s) throws Exception { return DT.parse(s); }
    static WorkSession s(String e, String x) throws Exception {
        return new WorkSession(e == null ? null : t(e), x == null ? null : t(x),
                               WorkSession.Pairing.CHRONOLOGICAL);
    }
    static ScheduledJourney j(String dia, String desde, String hasta) throws Exception {
        return ScheduledJourney.of(D.parse(dia), H.parse(desde), H.parse(hasta), 5, 5, 1L);
    }
    static void check(String caso, boolean cond, String detalle) {
        System.out.printf("  %-5s %-54s %s%n", cond ? "OK" : "FALLA", caso, detalle);
        if (cond) ok++; else fail++;
    }
    static int inc(SessionScheduleMatcher m, AttendanceIncidenceType t) {
        int n = 0;
        for (AttendanceIncidence i : m.getIncidences()) if (i.getType() == t) n++;
        return n;
    }

    public static void main(String[] a) throws Exception {
        System.out.println("== El que sale 5 h despues NO genera falta ni incidencia ==");
        SessionScheduleMatcher m1b = new SessionScheduleMatcher(
            Arrays.asList(s("01/07/2026 07:30", "02/07/2026 00:30")),
            Arrays.asList(j("01/07/2026", "07:30", "19:30")));
        check("se queda 5 h de mas: la jornada tiene su sesion",
              !m1b.getAssignments().get(0).isEmpty(), "");
        check("y no hay ninguna incidencia", m1b.getIncidences().isEmpty(),
              m1b.getIncidences().toString());

        System.out.println("\n== Dos jornadas en el dia: manana y tarde ==");
        SessionScheduleMatcher m2 = new SessionScheduleMatcher(
            Arrays.asList(s("02/07/2026 08:00", "02/07/2026 12:00"),
                          s("02/07/2026 14:00", "02/07/2026 18:00")),
            Arrays.asList(j("02/07/2026", "08:00", "12:00"), j("02/07/2026", "14:00", "18:00")));
        check("cada sesion va a SU jornada",
              m2.getAssignments().get(0).getSessions().size() == 1
              && m2.getAssignments().get(1).getSessions().size() == 1, "");
        check("la tarde no se asigna a la manana",
              H.format(m2.getAssignments().get(1).getFirstEntry()).equals("14:00"), "");
        check("sin incidencias", m2.getIncidences().isEmpty(), m2.getIncidences().toString());

        System.out.println("\n== Jornada nocturna 19:30 -> 07:30 ==");
        ScheduledJourney noche = j("03/07/2026", "19:30", "07:30");
        check("la jornada cruza la medianoche", noche.crossesMidnight(),
              DT.format(noche.getStart()) + " -> " + DT.format(noche.getEnd()));
        check("dura 12 h", noche.getMinutes() == 720, noche.getMinutes() + " min");
        SessionScheduleMatcher m3 = new SessionScheduleMatcher(
            Arrays.asList(s("03/07/2026 19:28", "04/07/2026 07:35")), Arrays.asList(noche));
        check("una sola sesion cubre la noche entera",
              m3.getAssignments().get(0).getSessions().size() == 1, "");
        check("sin incidencias", m3.getIncidences().isEmpty(), m3.getIncidences().toString());

        System.out.println("\n== Entrada sin salida: incidencia, NO falta ==");
        SessionScheduleMatcher m4 = new SessionScheduleMatcher(
            Arrays.asList(s("05/07/2026 07:28", null)),
            Arrays.asList(j("05/07/2026", "07:30", "19:30")));
        check("la jornada NO queda vacia -> no es falta",
              !m4.getAssignments().get(0).isEmpty(), "");
        check("no se reporta jornada sin sesion",
              inc(m4, AttendanceIncidenceType.SCHEDULE_WITHOUT_SESSION) == 0, "");
        check("la salida se asume en el fin de jornada, nunca mas alla",
              H.format(m4.getAssignments().get(0).getLastExit()).equals("19:30"),
              DT.format(m4.getAssignments().get(0).getLastExit()));
        check("y queda marcado que fue asumida", m4.getAssignments().get(0).hasAssumedExit(), "");
        check("la entrada real se conserva",
              H.format(m4.getAssignments().get(0).getFirstEntry()).equals("07:28"), "");

        System.out.println("\n== Entrada 5 min antes: no puede quedar fuera de horario ==");
        SessionScheduleMatcher m5 = new SessionScheduleMatcher(
            Arrays.asList(s("06/07/2026 07:25", "06/07/2026 19:35")),
            Arrays.asList(j("06/07/2026", "07:30", "19:30")));
        check("entra a su jornada", !m5.getAssignments().get(0).isEmpty(), "");

        System.out.println("\n== Jornada sin ninguna sesion: ESO si es falta ==");
        SessionScheduleMatcher m6 = new SessionScheduleMatcher(
            new ArrayList<WorkSession>(), Arrays.asList(j("07/07/2026", "07:30", "19:30")));
        check("una jornada en la lista de faltas", m6.getAbsences().size() == 1, "");
        check("y se reporta como tal",
              inc(m6, AttendanceIncidenceType.SCHEDULE_WITHOUT_SESSION) == 1, "");

        System.out.println("\n== Trabajo fuera de horario: se reporta, no se pierde ==");
        SessionScheduleMatcher m7 = new SessionScheduleMatcher(
            Arrays.asList(s("08/07/2026 22:00", "08/07/2026 23:30")),
            Arrays.asList(j("08/07/2026", "07:30", "19:30")));
        check("se reporta sesion fuera de horario",
              inc(m7, AttendanceIncidenceType.SESSION_WITHOUT_SCHEDULE) == 1, "");
        check("y la jornada figura sin sesion",
              inc(m7, AttendanceIncidenceType.SCHEDULE_WITHOUT_SESSION) == 1, "");

        System.out.println("\n== Se elige la jornada con MAS solapamiento ==");
        SessionScheduleMatcher m8 = new SessionScheduleMatcher(
            Arrays.asList(s("09/07/2026 11:00", "09/07/2026 17:00")),
            Arrays.asList(j("09/07/2026", "08:00", "12:00"), j("09/07/2026", "13:00", "18:00")));
        check("va a la tarde (4 h) y no a la manana (1 h)",
              m8.getAssignments().get(1).getSessions().size() == 1
              && m8.getAssignments().get(0).isEmpty(), "");

        System.out.printf("%n%d OK, %d FALLA%n", ok, fail);
        if (fail > 0) System.exit(1);
    }
}
