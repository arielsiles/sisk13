package com.encens.khipus.model.employees;

/**
 * Tipo de movimiento del saldo de vacaciones.
 * <p/>
 * El saldo no se guarda: se deriva sumando los movimientos hasta una fecha. Por eso los
 * movimientos no se modifican ni se borran; una vacacion anulada genera un movimiento que
 * revierte al anterior, y asi el libro explica como se llego al saldo actual.
 * <p/>
 * No hay caducidad: los dias no usados de una gestion se acumulan a la siguiente.
 *
 * @author
 * @version 6.1.0
 */
public enum VacationMovementType {

    /**
     * El saldo con el que el empleado entra al sistema, tomado del informe que RRHH venia
     * llevando por fuera. Es un tipo propio y no un ajuste generico para que el libro se
     * explique solo: se lee "saldo inicial al 08-08-2026" y no "ajuste".
     */
    OPENING("VacationMovementType.opening"),

    /** Los dias que otorga la regla de antiguedad al cumplirse un anio de servicio. */
    ACCRUAL("VacationMovementType.accrual"),

    /** Los dias que consume una vacacion aprobada. Va en negativo. */
    TAKING("VacationMovementType.taking"),

    /** Correccion manual, con motivo y usuario. Tambien revierte una vacacion anulada. */
    ADJUSTMENT("VacationMovementType.adjustment");

    private String resourceKey;

    VacationMovementType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
