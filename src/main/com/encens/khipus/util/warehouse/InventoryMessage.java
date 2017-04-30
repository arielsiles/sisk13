package com.encens.khipus.util.warehouse;


import com.encens.khipus.model.warehouse.ProductItem;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.2
 */
public class InventoryMessage {
    private ProductItem productItem;
    private boolean notFound;
    private boolean notEnough;
    private BigDecimal availableQuantity;

    public InventoryMessage(ProductItem productItem, boolean notFound) {
        this.productItem = productItem;
        this.notFound = notFound;
    }

    public InventoryMessage(ProductItem productItem, boolean notEnough, BigDecimal availableQuantity) {
        this.productItem = productItem;
        this.notEnough = notEnough;
        this.availableQuantity = availableQuantity;
    }

    public boolean isNotFound() {
        return notFound;
    }

    public boolean isNotEnough() {
        return notEnough;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }
}
