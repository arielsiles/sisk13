package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ContractWorkShift;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * El horario fijo semanal de un contrato.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface ContractScheduleService extends GenericService {

    /** Las filas vigentes -sin fecha de fin- de un contrato, ordenadas por dia de la semana. */
    List<ContractWorkShift> findCurrent(Contract contract);

    /**
     * Fija el turno de un dia de la semana a partir de una fecha. Si ya habia uno vigente, se
     * cierra el dia anterior en lugar de pisarse: los dias ya evaluados tienen que poder
     * reproducirse con el horario que regia entonces.
     */
    void setShift(Contract contract, int dayOfWeek, WorkShift workShift, Date from) throws Exception;

    /** Cierra el horario fijo completo de un contrato a partir de una fecha. */
    void close(Contract contract, Date from) throws Exception;

    /** Cuantos horarios fijos usan un turno: un turno en uso no se puede borrar. */
    Long countByWorkShift(WorkShift workShift);
}
