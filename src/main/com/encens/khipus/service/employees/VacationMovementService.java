package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Vacation;
import com.encens.khipus.model.employees.VacationGestion;
import com.encens.khipus.model.employees.VacationMovement;
import com.encens.khipus.model.employees.VacationPlanning;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Libro de movimientos del saldo de vacaciones.
 * <p/>
 * El saldo no se guarda en ningun lado: es la suma de los movimientos. Nada se modifica ni se
 * borra; anular una vacacion escribe un movimiento que revierte al anterior.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface VacationMovementService extends GenericService {

    /** Saldo a una fecha de corte: lo que se necesita para un mes ya cerrado. */
    BigDecimal balanceAt(VacationPlanning vacationPlanning, Date date);

    /** Saldo con todos los movimientos, sin corte. */
    BigDecimal balance(VacationPlanning vacationPlanning);

    List<VacationMovement> findByVacationPlanning(VacationPlanning vacationPlanning);

    /**
     * Saldo con el que el empleado entra al sistema, tomado del informe que RRHH venia
     * llevando por fuera. Solo puede haber uno por plan: es el punto de partida, no una
     * correccion.
     */
    void postOpeningBalance(VacationPlanning vacationPlanning, Date date, BigDecimal days,
                            String description);

    /** true si el plan ya tiene cargado su saldo inicial. */
    boolean hasOpeningBalance(VacationPlanning vacationPlanning);

    /** Los dias del saldo inicial, o cero si no se cargo. */
    BigDecimal openingBalance(VacationPlanning vacationPlanning);

    /**
     * Registra los dias que otorga una gestion. Es idempotente: si la gestion ya tiene su
     * devengo no vuelve a escribirlo, para que resincronizar el plan no duplique dias.
     */
    void postAccrual(VacationGestion vacationGestion);

    /** Registra el consumo de una vacacion aprobada, en negativo. */
    void postTaking(Vacation vacation);

    /**
     * Devuelve al saldo los dias de una vacacion anulada. No borra la toma: escribe el
     * movimiento contrario, para que el libro muestre que paso.
     */
    void revertTaking(Vacation vacation);
}
