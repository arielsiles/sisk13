package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehousePK;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.0
 */
@Local
public interface InventoryService extends GenericService {

    /**
     * Finds the unitaryBalance quantity by ProductItemPK and WarehousePK
     *
     * @param warehouseId   the warehouse filter
     * @param productItemId the productItem filter
     * @return the unitaryBalance quantity by ProductItemPK and WarehousePK
     */
    BigDecimal findUnitaryBalanceByProductItemAndArticle(WarehousePK warehouseId, ProductItemPK productItemId);
    public Warehouse findWarehouseByItemArticle(ProductItem productItem);
}
