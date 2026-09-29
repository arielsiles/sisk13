package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Contract;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Una linea del reporte de saldos del banco de horas.
 * <p/>
 * Es la vista que reemplaza a la planilla que RRHH llevaba a mano: una fila por persona con lo
 * acumulado, lo usado, lo pagado y el saldo. El detalle de como se llego a ese saldo esta un nivel
 * mas abajo, en los movimientos.
 * <p/>
 * La fila cuadra sola: {@code anterior + acumulado - usado - pagado = saldo}. Esa es la razon de
 * que exista {@link #previous}: sin el, el saldo dependia de la fecha que se escribiera arriba y
 * la misma persona tenia un saldo distinto en cada consulta.
 *
 * @author
 * @version 6.1.1
 */
public class HourBankBalance implements Serializable {

    private Contract contract;
    /** Lo que ya tenia antes de la fecha consultada. No es un movimiento: es el punto de partida. */
    private BigDecimal previous = BigDecimal.ZERO;
    private BigDecimal accrued = BigDecimal.ZERO;
    private BigDecimal used = BigDecimal.ZERO;
    private BigDecimal paid = BigDecimal.ZERO;
    private BigDecimal hoursPerDay;

    public HourBankBalance(Contract contract) {
        this.contract = contract;
    }

    public Employee getEmployee() {
        return null == contract ? null : contract.getEmployee();
    }

    public Contract getContract() {
        return contract;
    }

    /**
     * El saldo de verdad: todo lo que la persona tiene, no el neto del periodo consultado.
     * <p/>
     * Por eso arranca en lo anterior. Un saldo es acumulado desde siempre y no puede depender del
     * filtro; lo que el filtro decide es que movimiento se muestra al costado. Puede ser negativo
     * —se paga de mas o se adelanta un permiso—, y se muestra.
     */
    public BigDecimal getBalance() {
        return previous.add(accrued).subtract(used).subtract(paid);
    }

    /** Null si la jornada no esta configurada: un cero ahi se leeria como saldo real. */
    public BigDecimal getBalanceInDays() {
        if (null == hoursPerDay || 0 == hoursPerDay.signum()) {
            return null;
        }
        return getBalance().divide(hoursPerDay, 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean isNegative() {
        return getBalance().signum() < 0;
    }

    public BigDecimal getPrevious() {
        return previous;
    }

    public void setPrevious(BigDecimal previous) {
        this.previous = null == previous ? BigDecimal.ZERO : previous;
    }

    public void addAccrued(BigDecimal hours) {
        accrued = accrued.add(null == hours ? BigDecimal.ZERO : hours);
    }

    /** Lo usado y lo pagado se guardan en positivo: los movimientos vienen en negativo. */
    public void addUsed(BigDecimal hours) {
        used = used.add(null == hours ? BigDecimal.ZERO : hours.abs());
    }

    public void addPaid(BigDecimal hours) {
        paid = paid.add(null == hours ? BigDecimal.ZERO : hours.abs());
    }

    public BigDecimal getAccrued() {
        return accrued;
    }

    public BigDecimal getUsed() {
        return used;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setHoursPerDay(BigDecimal hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }
}
