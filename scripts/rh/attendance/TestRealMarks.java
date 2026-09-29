import com.encens.khipus.util.employees.attendance.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestRealMarks {
    public static void main(String[] a) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection cn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/terdemol?useSSL=false&characterEncoding=UTF-8", "adm", "Cisc.13");
        // agrupa por persona y por dia
        Map<String, List<AttendanceMark>> porDia = new TreeMap<String, List<AttendanceMark>>();
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(
            "select marperid, marfecha, marhora, control from rh_marcado order by marperid, marfecha, marhora");
        int total = 0;
        while (rs.next()) {
            String k = String.valueOf(rs.getInt(1));
            Calendar c = Calendar.getInstance();
            c.setTime(rs.getDate(2));
            Calendar h = Calendar.getInstance();
            h.setTime(rs.getTime(3));
            c.set(Calendar.HOUR_OF_DAY, h.get(Calendar.HOUR_OF_DAY));
            c.set(Calendar.MINUTE, h.get(Calendar.MINUTE));
            c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
            if (!porDia.containsKey(k)) porDia.put(k, new ArrayList<AttendanceMark>());
            porDia.get(k).add(new AttendanceMark(c.getTime(), rs.getInt(4)));
            total++;
        }
        rs.close(); st.close(); cn.close();

        int sesiones = 0, completas = 0, incompletas = 0, indicador = 0, cronologico = 0;
        Map<String,Integer> inc = new TreeMap<String,Integer>();
        long minutos = 0;
        for (List<AttendanceMark> marcas : porDia.values()) {
            WorkSessionBuilder b = new WorkSessionBuilder(marcas);
            for (WorkSession s : b.getSessions()) {
                sesiones++;
                if (s.isComplete()) { completas++; minutos += s.getMinutes(); } else incompletas++;
                if (s.getPairing() == WorkSession.Pairing.INDICATOR) indicador++; else cronologico++;
            }
            for (AttendanceIncidence i : b.getIncidences()) {
                String t = i.getType().name();
                inc.put(t, (inc.containsKey(t) ? inc.get(t) : 0) + 1);
            }
        }
        System.out.println("marcaciones procesadas : " + total);
        System.out.println("personas               : " + porDia.size());
        System.out.println("sesiones armadas       : " + sesiones);
        System.out.println("  completas            : " + completas);
        System.out.println("  incompletas          : " + incompletas);
        System.out.println("  por indicador        : " + indicador);
        System.out.println("  cronologicas         : " + cronologico);
        System.out.printf ("promedio por sesion    : %.1f h%n", completas == 0 ? 0.0 : minutos / 60.0 / completas);
        System.out.println("incidencias:");
        for (Map.Entry<String,Integer> e : inc.entrySet())
            System.out.println("  " + e.getKey() + " : " + e.getValue());
        if (inc.isEmpty()) System.out.println("  ninguna");
    }
}
