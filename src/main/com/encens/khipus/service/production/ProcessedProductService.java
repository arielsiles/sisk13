package com.encens.khipus.service.production;

import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductComposition;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/24/13
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface ProcessedProductService {
    public ProcessedProduct find(long id);
    public ProcessedProduct findByCode(String code);
    public void createProductionProduct(ProductItem productItem);
    public void updateProductionProduct(ProductItem productItem);
    public ProductComposition getProductComposite(Long id);
}
