package com.encens.khipus.service.production;

import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: Ariel Siles Encinas
 */
@Local
public interface ProductionInputService {
    public void createProductionInput(ProductItem productItem);
    public void updateProductionInput(ProductItem productItem);
}
