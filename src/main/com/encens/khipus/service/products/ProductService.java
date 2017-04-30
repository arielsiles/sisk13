package com.encens.khipus.service.products;

import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product service interface
 *
 * @author
 * @version $Id: ProductService.java 2008-9-11 13:50:25 $
 */
@Local
public interface ProductService {

    Product findByCode(String code);

    List<ProductDiscountRule> findDiscountRules(Product product);

    BigDecimal getDiscountPercentage(Product product);

    BigDecimal getDiscount(Product product);
}
