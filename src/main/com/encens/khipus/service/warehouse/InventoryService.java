package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.warehouse.*;

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
    Inventory findInventoryByProductItemCode(String productItemCode);
    InventoryDetail findInventoryDetailByProductItemCode(String productItemCode);
    public Warehouse findWarehouseByItemArticle(ProductItem productItem);
    void updateInventoryForSales(CustomerOrder customerOrder);
    void updateInventoryForProduction(ProductionProduct product);
    void updateInventoryRemoveFromProduction(ProductionProduct product);
}
