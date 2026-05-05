package com.encens.khipus.exception.warehouse;

import javax.ejb.ApplicationException;
import java.math.BigDecimal;

/**
 * Lanzada cuando, tras la reconstruccion cronologica completa, el saldo
 * final del articulo queda negativo. Indica datos inconsistentes en el
 * historial de movimientos; el ajuste no debe aplicarse hasta corregir.
 */
@ApplicationException(rollback = true)
public class NegativeFinalBalanceException extends Exception {

    private final BigDecimal finalQuantity;

    public NegativeFinalBalanceException(String productItemCode, BigDecimal finalQuantity) {
        super("Saldo final negativo para articulo " + productItemCode + ": " + finalQuantity +
                ". Corrija las transacciones inconsistentes antes de aplicar el ajuste.");
        this.finalQuantity = finalQuantity;
    }

    public BigDecimal getFinalQuantity() {
        return finalQuantity;
    }
}
