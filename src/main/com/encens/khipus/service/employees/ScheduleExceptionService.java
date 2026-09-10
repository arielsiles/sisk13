package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ScheduleException;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.Date;

/**
 * Excepciones de horario por persona y dia.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface ScheduleExceptionService extends GenericService {

    /** La excepcion de un contrato en una fecha, o null. Sirve para no duplicar el mismo dia. */
    ScheduleException find(Contract contract, Date date);

    /** Cuantas excepciones usan un turno: un turno en uso no se puede borrar. */
    Long countByWorkShift(WorkShift workShift);
}
