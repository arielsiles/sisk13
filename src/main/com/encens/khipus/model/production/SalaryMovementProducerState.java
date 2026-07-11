package com.encens.khipus.model.production;

/**
 * Estado de un movimiento/descuento del productor respecto a su cobro (saldo).
 *   PENDIENTE: saldo &gt; 0, queda deuda por cobrar (se arrastra).
 *   PAGADO:    saldo = 0, cobrado totalmente.
 *
 * Reemplaza el uso previo de ProductionCollectionState (PENDING/APPROVED) que no
 * reflejaba si el descuento se cobro o no.
 */
public enum SalaryMovementProducerState {
    PENDIENTE("SalaryMovementProducer.state.pendiente"),
    PAGADO("SalaryMovementProducer.state.pagado");

    private String resourceKey;

    SalaryMovementProducerState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
