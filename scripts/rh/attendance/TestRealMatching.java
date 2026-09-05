import com.encens.khipus.util.employees.attendance.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/** Corre el matcher sobre las sesiones reales de julio, con la jornada de produccion 07:30-19:30. */
public class TestRealMatching {
    public static void main(String[] a) throws Exception {
        SimpleDateFormat H = new SimpleDateFormat("HH:mm");
        Class.forName("com.mysql.jdbc.Driver");
        Connection cn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/terdemol?useSSL=false&characterEncoding=UTF-8", "adm", "Cisc.13");
        Map<Integer, List<AttendanceMark>> porPersona = new TreeMap<Integer, List<AttendanceMark>>();
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(
            "select marperid, marfecha, marhora, control from rh_marcado order by marperid, marfecha, marhora");
        while (rs.next()) {
            Calendar c = Calendar.getInstance();
            c.setTime(rs.getDate(2));
            Calendar h = Calendar.getInstance();
            h.setTime(rs.getTime(3));
            c.set(Calendar.HOUR_OF_DAY, h.get(Calendar.HOUR_OF_DAY));
            c.set(Calendar.MINUTE, h.get(Calendar.MINUTE));
            c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
            int k = rs.getInt(1);
            if (!porPersona.containsKey(k)) porPersona.put(k, new ArrayList<AttendanceMark>());
            porPersona.get(k).add(new AttendanceMark(c.getTime(), rs.getInt(4)));
        }
        rs.close(); st.close(); cn.close();

        int jornadas = 0, conSesion = 0, vacias = 0, fuera = 0, asumidas = 0, atrasos = 0;
        for (Map.Entry<Integer, List<AttendanceMark>> e : porPersona.entrySet()) {
            WorkSessionBuilder b = new WorkSessionBuilder(e.getValue());
            // una jornada de lunes a sabado, 07:30 a 19:30, todo julio
            List<ScheduledJourney> js = new ArrayList<ScheduledJourney>();
            Calendar d = Calendar.getInstance();
            d.set(2026, Calendar.JULY, 1, 0, 0, 0); d.set(Calendar.MILLISECOND, 0);
            while (d.get(Calendar.MONTH) == Calendar.JULY) {
                if (d.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
                    js.add(ScheduledJourney.of(d.getTime(), H.parse("07:30"), H.parse("19:30"), 5, 5, 0L));
                d.add(Calendar.DAY_OF_MONTH, 1);
            }
            SessionScheduleMatcher m = new SessionScheduleMatcher(b.getSessions(), js);
            for (JourneyAssignment ja : m.getAssignments()) {
                jornadas++;
                if (ja.isEmpty()) vacias++; else {
                    conSesion++;
                    if (ja.hasAssumedExit()) asumidas++;
                    Date fe = ja.getFirstEntry();
                    if (fe != null && fe.getTime() > ja.getJourney().getStart().getTime()
                                      + ja.getJourney().getEntryToleranceMinutes() * 60000L) atrasos++;
                }
            }
            for (AttendanceIncidence i : m.getIncidences())
                if (i.getType() == AttendanceIncidenceType.SESSION_WITHOUT_SCHEDULE) fuera++;
        }
        System.out.println("personas                        : " + porPersona.size());
        System.out.println("jornadas evaluadas              : " + jornadas);
        System.out.println("  con al menos una sesion       : " + conSesion);
        System.out.println("  vacias -> falta               : " + vacias);
        System.out.println("  con salida asumida            : " + asumidas);
        System.out.println("  con atraso de entrada         : " + atrasos);
        System.out.println("sesiones fuera de todo horario  : " + fuera);
        System.out.printf ("cobertura                       : %.1f%%%n", 100.0 * conSesion / jornadas);
    }
}
