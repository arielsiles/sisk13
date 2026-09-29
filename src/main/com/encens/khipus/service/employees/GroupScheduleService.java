package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GroupScheduleDay;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkShift;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * El cronograma de turnos por grupo.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface GroupScheduleService extends GenericService {

    /** Los dias cargados de un grupo en un rango, publicados o no. Para la grilla. */
    List<GroupScheduleDay> findDays(WorkGroup workGroup, Date from, Date to);

    /**
     * Los dias con turno y publicados. Es lo unico que el motor de asistencia mira: una semana
     * en borrador no puede hacerle creer que nadie tenia que trabajar.
     */
    List<GroupScheduleDay> findPublishedDays(WorkGroup workGroup, Date from, Date to);

    /** Fija el turno de un dia -o lo deja sin jornada con turno nulo-, creando la celda si falta. */
    void setShift(WorkGroup workGroup, Date day, WorkShift workShift) throws Exception;

    /** Lo mismo para un rango: es lo que hace pintar arrastrando. */
    void setShiftRange(WorkGroup workGroup, Date from, Date to, WorkShift workShift) throws Exception;

    /**
     * Copia los turnos de un rango a otro, dia por dia. Es "copiar la semana anterior".
     *
     * @param invert si true, cambia dia por noche. Es "copiar el otro grupo invirtiendo turnos"
     */
    void copyRange(WorkGroup source, Date sourceFrom, Date sourceTo,
                   WorkGroup target, Date targetFrom, boolean invert) throws Exception;

    void publish(WorkGroup workGroup, Date from, Date to) throws Exception;

    void unpublish(WorkGroup workGroup, Date from, Date to) throws Exception;

    /** Cuantos dias de cronograma usan un turno: un turno en uso no se puede borrar. */
    Long countByWorkShift(WorkShift workShift);
}
