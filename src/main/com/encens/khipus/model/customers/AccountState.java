package com.encens.khipus.model.customers;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum AccountState {
    ACTIVE("AccountState.active"),
    INACTIVE("AccountState.inactive"),
    /**
     * Cuenta que nunca existio como operacion: se dio de alta y el desembolso jamas
     * se contabilizo, o se cargo dos veces y esta es la copia huerfana. No es lo mismo
     * que INACTIVE, que es un certificado real ya cerrado y que si devengo intereses.
     * <p/>
     * Las cuentas anuladas quedan fuera de toda operacion (provision, renovacion,
     * transferencias); se siguen listando para que el registro no desaparezca.
     */
    ANNULLED("AccountState.annulled");

    private String resourceKey;

    AccountState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
