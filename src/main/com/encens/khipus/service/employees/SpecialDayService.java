package com.encens.khipus.service.employees;

import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ExcusedDay;
import com.encens.khipus.util.employees.attendance.ExcusedInterval;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Que dice `fechaespecial` sobre una persona en un rango de fechas.
 * <p/>
 * Responde dos preguntas distintas sobre la misma tabla, y estan juntas a proposito: las dos
 * tienen que resolver <b>a quien alcanza</b> una fecha especial -toda la unidad de negocio, una
 * unidad organizacional, o una persona puntual-, y esa parte escrita dos veces es la forma
 * segura de que algun dia una de las dos se quede vieja.
 * <ul>
 *   <li><b>Feriados</b> -{@link #holidaysBetween}-: suprimen la jornada del horario fijo. Existe
 *       porque el resolutor no miraba el calendario y un feriado en dia laborable generaba falta
 *       a todos los administrativos. El domingo no necesita nada de esto: el horario no tiene
 *       linea de domingo.</li>
 *   <li><b>Dias excusados</b> -{@link #excusedBetween}-: permisos, vacaciones, maternidad,
 *       compensatorios y descansos de dia completo. No suprimen la jornada: la <b>perdonan</b>.
 *       El feriado queda afuera de esta lista a proposito, porque produccion trabaja los
 *       feriados y ahi la falta si corresponde.</li>
 * </ul>
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface SpecialDayService {

    boolean isHoliday(Contract contract, Date day);

    /**
     * Los feriados de un periodo, normalizados a inicio de dia. Se pide una vez y se consulta en
     * memoria: resolver un mes preguntando dia por dia serian 31 consultas por persona.
     */
    Set<Long> holidaysBetween(Contract contract, Date from, Date to);

    /**
     * Los dias excusados de un periodo, por inicio de dia. Incluye los de goce de haber y los
     * que no lo tienen: son cosas distintas y {@link ExcusedDay#isPaid()} las separa.
     */
    Map<Long, ExcusedDay> excusedBetween(Contract contract, Date from, Date to);

    /**
     * Los permisos POR HORAS de un periodo, por inicio de dia.
     * <p/>
     * Son las fechas especiales que no son de dia completo: en vez de sacar la jornada entera,
     * justifican un tramo. El motor viejo ya los leia y el nuevo los ignoraba, asi que a quien
     * los usa le aparecian atrasos y salidas anticipadas que estaban autorizados.
     */
    Map<Long, List<ExcusedInterval>> excusedIntervalsBetween(Contract contract, Date from, Date to);
}
