package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.ApplicationException;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.2
 */
@ApplicationException(rollback = true)
public class ProductItemAmountException extends Exception {
    private BigDecimal availableAmount;
    private ProductItem productItem;

    public ProductItemAmountException(BigDecimal availableAmount,
                                      ProductItem productItem) {
        this.availableAmount = availableAmount;
        this.productItem = productItem;
    }

    public ProductItemAmountException(String message,
                                      BigDecimal availableAmount,
                                      ProductItem productItem) {
        super(message);
        this.availableAmount = availableAmount;
        this.productItem = productItem;
    }

    public ProductItemAmountException(String message,
                                      Throwable cause,
                                      BigDecimal availableAmount,
                                      ProductItem productItem) {
        super(message, cause);
        this.availableAmount = availableAmount;
        this.productItem = productItem;
    }

    public ProductItemAmountException(Throwable cause,
                                      BigDecimal availableAmount,
                                      ProductItem productItem) {
        super(cause);
        this.availableAmount = availableAmount;
        this.productItem = productItem;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public ProductItem getProductItem() {
        return productItem;
    }
}
