package com.encens.khipus.service.products;

import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;

import javax.ejb.Local;


/**
 * Product discount service interface
 *
 * @author:
 */

@Local
public interface ProductDiscountService {

    Product findDiscountByRule(Product product, ProductDiscountRule rule);

    void newDiscount(ProductDiscountRule rule, Product product);

}
