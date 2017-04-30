package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;

import javax.ejb.ApplicationException;

/**
 * @author
 * @version 2.2
 */
@ApplicationException(rollback = true)
public class InventoryProductItemNotFoundException extends Exception {
    private String executorUnitCode;
    private ProductItem productItem;
    private Warehouse warehouse;

    public InventoryProductItemNotFoundException(String executorUnitCode,
                                                 ProductItem productItem,
                                                 Warehouse warehouse) {
        this.executorUnitCode = executorUnitCode;
        this.productItem = productItem;
        this.warehouse = warehouse;
    }

    public InventoryProductItemNotFoundException(String message,
                                                 String executorUnitCode,
                                                 ProductItem productItem,
                                                 Warehouse warehouse) {
        super(message);
        this.executorUnitCode = executorUnitCode;
        this.productItem = productItem;
        this.warehouse = warehouse;
    }

    public InventoryProductItemNotFoundException(String message,
                                                 Throwable cause,
                                                 String executorUnitCode,
                                                 ProductItem productItem,
                                                 Warehouse warehouse) {
        super(message, cause);
        this.executorUnitCode = executorUnitCode;
        this.productItem = productItem;
        this.warehouse = warehouse;
    }

    public InventoryProductItemNotFoundException(Throwable cause,
                                                 String executorUnitCode,
                                                 ProductItem productItem,
                                                 Warehouse warehouse) {
        super(cause);
        this.executorUnitCode = executorUnitCode;
        this.productItem = productItem;
        this.warehouse = warehouse;
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }
}
