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
    ANNULLED("AccountState.annulled"),
    /**
     * DPF dado de alta pero todavia sin contabilizar: el certificado esta cargado y el
     * dinero no entro. Nace asi toda cuenta DPF nueva y solo sale de este estado por el
     * boton Aprobar, que es el que genera el comprobante de ingreso.
     * <p/>
     * Mientras esta pendiente no devenga (no tiene un solo asiento, con lo que la
     * provision mensual lo calcula en cero) y no se puede renovar ni cerrar.
     */
    PENDING("AccountState.pending");

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
