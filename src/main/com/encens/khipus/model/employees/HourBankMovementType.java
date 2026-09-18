package com.encens.khipus.model.employees;

/**
 * Tipo de movimiento del banco de horas.
 * <p/>
 * El saldo no se guarda: se deriva sumando los movimientos. Por eso los movimientos no se
 * modifican; anular un permiso genera el movimiento que lo revierte, y asi el libro explica como
 * se llego al saldo actual.
 * <p/>
 * Las tres formas de bajar el saldo conviven a proposito. Una empresa que solo compensa no usa
 * {@link #PAYMENT}; una que solo paga no usa {@link #LEAVE}; una que hace las dos usa las dos. No
 * hay nada que configurar: es el mismo saldo con tres salidas.
 *
 * @author
 * @version 6.1.0
 */
public enum HourBankMovementType {

    /**
     * El saldo con el que la persona entra al sistema, tomado de la planilla que RRHH venia
     * llevando por fuera. Es un tipo propio y no un ajuste generico para que el libro se explique
     * solo: se lee "saldo inicial" y no "ajuste".
     */
    OPENING("HourBankMovementType.opening", true),

    /** Las horas extra AUTORIZADAS por el jefe de area, validadas y registradas por RRHH. */
    ACCRUAL("HourBankMovementType.accrual", true),

    /** Las horas que consume un permiso compensatorio. Va en negativo. */
    LEAVE("HourBankMovementType.leave", false),

    /** Las horas que se cobran en vez de tomarse. Va en negativo y alimenta la planilla. */
    PAYMENT("HourBankMovementType.payment", false),

    /** Correccion manual, con motivo y usuario. Tambien revierte un permiso anulado. */
    ADJUSTMENT("HourBankMovementType.adjustment", true);

    private final String resourceKey;

    /** Si por naturaleza suma. El ajuste puede ir en los dos sentidos: manda el signo del monto. */
    private final boolean credit;

    HourBankMovementType(String resourceKey, boolean credit) {
        this.resourceKey = resourceKey;
        this.credit = credit;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public boolean isCredit() {
        return credit;
    }
}
