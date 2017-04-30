package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.ProductItem;

/**
 * @author
 * @version 3.0
 */
public class ProductItemNotFoundException extends Exception {
    ProductItem productItem;

    public ProductItemNotFoundException() {
    }

    public ProductItemNotFoundException(ProductItem productItem) {
        this.productItem = productItem;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }
}