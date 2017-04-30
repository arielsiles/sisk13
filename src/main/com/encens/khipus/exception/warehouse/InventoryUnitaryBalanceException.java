package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.ApplicationException;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.2
 */
@ApplicationException(rollback = true)
public class InventoryUnitaryBalanceException extends Exception {
    private BigDecimal availableUnitaryBalance;
    private ProductItem productItem;

    public InventoryUnitaryBalanceException(BigDecimal availableUnitaryBalance,
                                            ProductItem productItem) {
        this.availableUnitaryBalance = availableUnitaryBalance;
        this.productItem = productItem;
    }

    public InventoryUnitaryBalanceException(String message,
                                            BigDecimal availableUnitaryBalance,
                                            ProductItem productItem) {
        super(message);
        this.availableUnitaryBalance = availableUnitaryBalance;
        this.productItem = productItem;
    }

    public InventoryUnitaryBalanceException(String message,
                                            Throwable cause,
                                            BigDecimal availableUnitaryBalance,
                                            ProductItem productItem) {
        super(message, cause);
        this.availableUnitaryBalance = availableUnitaryBalance;
        this.productItem = productItem;
    }

    public InventoryUnitaryBalanceException(Throwable cause,
                                            BigDecimal availableUnitaryBalance,
                                            ProductItem productItem) {
        super(cause);
        this.availableUnitaryBalance = availableUnitaryBalance;
        this.productItem = productItem;
    }

    public BigDecimal getAvailableUnitaryBalance() {
        return availableUnitaryBalance;
    }

    public ProductItem getProductItem() {
        return productItem;
    }
}
