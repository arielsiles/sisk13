package com.encens.khipus.service.employees;

import com.encens.khipus.action.employees.dto.AttendanceDay;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * La verificacion del control de asistencia, dia por dia.
 * <p/>
 * Corre el mismo motor que va a usar la planilla -sesiones, asociacion por solapamiento y
 * evaluacion- pero sin calcular ni guardar nada. Sirve para revisar un mes antes de generarlo,
 * y despues para explicar por que se conto un atraso.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface AttendanceCheckService {

    List<AttendanceDay> check(Contract contract, Date from, Date to);

    /**
     * El codigo de marcado de la persona se puede leer.
     * <p/>
     * Se pregunta aca y no donde haga falta porque la regla de como se lee ese codigo -texto en
     * la base, numero en el biometrico- vive en esta clase. Escrita dos veces, un dia una de las
     * dos se queda vieja.
     * <p/>
     * Importa para la planilla: un codigo ilegible no devuelve ninguna marca, y sin marcas cada
     * jornada del mes se pierde. Un error de tipeo costaria el mes entero.
     */
    boolean hasReadableMarkCode(Employee employee);

    /**
     * Si a este contrato le corresponde el motor de jornadas en esa fecha.
     * <p/>
     * Los dos motores conviven por categoria y por fecha de corte mientras dura la transicion.
     * Quien muestre resultados de asistencia tiene que saber cual corresponde, o va a mostrar
     * vacio donde en realidad hay bandas.
     */
    boolean usesJourneys(Contract contract, Date date);
}
