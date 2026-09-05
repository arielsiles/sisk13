import com.encens.khipus.util.employees.attendance.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestSessions {
    static SimpleDateFormat F = new SimpleDateFormat("dd/MM HH:mm");
    static int ok = 0, fail = 0;

    static AttendanceMark m(String s, int control) throws Exception {
        return new AttendanceMark(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(s), control);
    }
    static void check(String caso, boolean cond, String detalle) {
        System.out.printf("  %-4s %-52s %s%n", cond ? "OK" : "FALLA", caso, detalle);
        if (cond) ok++; else fail++;
    }
    static String show(List<WorkSession> ss) {
        StringBuilder b = new StringBuilder();
        for (WorkSession s : ss) {
            b.append(s.getEntry() == null ? "----- " : F.format(s.getEntry()) + " ");
            b.append("-> ").append(s.getExit() == null ? "-----" : F.format(s.getExit())).append("  ");
        }
        return b.toString().trim();
    }

    public static void main(String[] a) throws Exception {
        System.out.println("== Emparejamiento cronologico (indicador inservible) ==");
        WorkSessionBuilder b1 = new WorkSessionBuilder(Arrays.asList(
            m("01/07/2026 07:40", 1), m("01/07/2026 12:00", 1),
            m("01/07/2026 14:00", 1), m("01/07/2026 19:30", 1)));
        check("4 marcas todas 'entrada' -> 2 sesiones", b1.getSessions().size() == 2, show(b1.getSessions()));
        check("sin incidencias", b1.getIncidences().isEmpty(), "" + b1.getIncidences().size());
        check("la tarde NO se mezcla con la manana",
              F.format(b1.getSessions().get(0).getExit()).equals("01/07 12:00"), show(b1.getSessions()));

        System.out.println("\n== Numero impar: entrada sin salida ==");
        WorkSessionBuilder b2 = new WorkSessionBuilder(Arrays.asList(
            m("02/07/2026 07:40", 1), m("02/07/2026 12:00", 1), m("02/07/2026 14:00", 1)));
        check("3 marcas -> 2 sesiones, la ultima incompleta", b2.getSessions().size() == 2, show(b2.getSessions()));
        check("la incompleta conserva su ENTRADA",
              b2.getSessions().get(1).isEntryWithoutExit(), show(b2.getSessions()));
        check("genera incidencia, no falta",
              b2.getIncidences().size() == 1
              && b2.getIncidences().get(0).getType() == AttendanceIncidenceType.ENTRY_WITHOUT_EXIT,
              b2.getIncidences().toString());

        System.out.println("\n== Emparejamiento por indicador (archivo importado) ==");
        WorkSessionBuilder b3 = new WorkSessionBuilder(Arrays.asList(
            m("03/07/2026 07:40", 1), m("03/07/2026 12:00", 3),
            m("03/07/2026 14:00", 1), m("03/07/2026 19:30", 3)));
        check("2 sesiones por indicador", b3.getSessions().size() == 2, show(b3.getSessions()));
        check("usa el indicador y no el orden",
              b3.getSessions().get(0).getPairing() == WorkSession.Pairing.INDICATOR, "");

        System.out.println("\n== Jornada nocturna que cruza medianoche ==");
        WorkSessionBuilder b4 = new WorkSessionBuilder(Arrays.asList(
            m("04/07/2026 19:30", 1), m("05/07/2026 07:30", 3)));
        check("UNA sesion, no dos dias sueltos", b4.getSessions().size() == 1, show(b4.getSessions()));
        check("dura 12 horas", b4.getSessions().get(0).getMinutes() == 720,
              b4.getSessions().get(0).getMinutes() + " min");
        check("sin incidencias", b4.getIncidences().isEmpty(), "");

        System.out.println("\n== Salida sin entrada ==");
        WorkSessionBuilder b5 = new WorkSessionBuilder(Arrays.asList(
            m("06/07/2026 07:30", 3), m("06/07/2026 19:30", 1)));
        check("la salida huerfana se conserva", b5.getSessions().get(0).isExitWithoutEntry(), show(b5.getSessions()));
        check("y se reporta",
              b5.getIncidences().get(0).getType() == AttendanceIncidenceType.EXIT_WITHOUT_ENTRY, "");

        System.out.println("\n== Sesion anormalmente larga ==");
        WorkSessionBuilder b6 = new WorkSessionBuilder(Arrays.asList(
            m("07/07/2026 07:00", 1), m("08/07/2026 09:00", 3)));
        check("26 h -> incidencia", b6.getIncidences().size() == 1, b6.getIncidences().toString());
        check("pero la sesion NO se descarta", b6.getSessions().size() == 1, show(b6.getSessions()));

        System.out.println("\n== Sin marcas ==");
        WorkSessionBuilder b7 = new WorkSessionBuilder(new ArrayList<AttendanceMark>());
        check("ninguna sesion, ninguna incidencia",
              b7.getSessions().isEmpty() && b7.getIncidences().isEmpty(), "");

        System.out.printf("%n%d OK, %d FALLA%n", ok, fail);
        if (fail > 0) System.exit(1);
    }
}
