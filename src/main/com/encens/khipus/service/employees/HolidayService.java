package com.encens.khipus.service.employees;

import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.Date;
import java.util.Set;

/**
 * Que dias son feriados para una persona.
 * <p/>
 * Lo lee de `fechaespecial` con motivo HOLIDAY, respetando a quien alcanza: toda la unidad de
 * negocio -el feriado nacional-, una unidad organizacional, o una persona puntual.
 * <p/>
 * Existe porque el resolutor de jornadas no miraba el calendario: un feriado que caia en un dia
 * laborable del horario tipo generaba falta a todos los administrativos. El domingo, en cambio,
 * no necesita nada de esto: se resuelve solo, porque el horario no tiene linea de domingo.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface HolidayService {

    boolean isHoliday(Contract contract, Date day);

    /**
     * Los feriados de un periodo, normalizados a inicio de dia. Se pide una vez y se consulta en
     * memoria: resolver un mes preguntando dia por dia serian 31 consultas por persona.
     */
    Set<Long> holidaysBetween(Contract contract, Date from, Date to);
}
