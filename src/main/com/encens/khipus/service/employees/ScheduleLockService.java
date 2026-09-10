package com.encens.khipus.service.employees;

import com.encens.khipus.exception.employees.ClosedPeriodException;
import com.encens.khipus.model.employees.WorkShift;

import javax.ejb.Local;
import java.util.Date;

/**
 * Hasta que fecha los horarios ya no se pueden tocar.
 * <p/>
 * El limite lo marca la ultima planilla OFICIAL generada: todo lo que quedo dentro de un
 * periodo pagado ya se calculo con los horarios de entonces, y cambiarlos despues haria que el
 * sistema no pueda reproducir lo que pago. Las planillas de prueba no cierran nada.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface ScheduleLockService {

    /** @return el ultimo dia cerrado por una planilla oficial, o null si no hay ninguna */
    Date lastClosedDate();

    boolean isClosed(Date day);

    /** Lanza si el dia cae dentro de un periodo cerrado. */
    void checkOpen(Date day) throws ClosedPeriodException;

    /**
     * @return true si el turno se uso en algun dia ya cerrado. Un turno asi no se puede editar:
     *         cambiarle las horas cambiaria en silencio como se calculo una planilla pagada
     */
    boolean isWorkShiftLocked(WorkShift workShift);
}
