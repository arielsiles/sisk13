package com.encens.khipus.service.employees;

import com.encens.khipus.action.employees.dto.AttendanceDay;
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
}
