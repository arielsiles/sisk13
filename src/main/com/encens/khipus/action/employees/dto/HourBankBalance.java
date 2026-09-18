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
 *
 * @author
 * @version 6.1.0
 */
public class HourBankBalance implements Serializable {

    private Contract contract;
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

    /** El saldo: acumulado menos lo que salio. Puede ser negativo, y se muestra. */
    public BigDecimal getBalance() {
        return accrued.subtract(used).subtract(paid);
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
