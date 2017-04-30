package com.encens.khipus.service.production;

import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProductComposition;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: Ariel Siles Encinas
 */
@Local
public interface MetaProductService {
    MetaProduct find(long id);
    public MetaProduct find(ProductItem productItem);
    public ProductComposition findProductoComposition(ProductComposition composition);
}
