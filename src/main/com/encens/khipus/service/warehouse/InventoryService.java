package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.model.xproduction.XProductionProduct;

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
    void updateInventoryForCollectMaterial(CollectMaterial collectMaterial);

    /** Deshace en inventario el ingreso hecho al aprobar el acopio: reversa exacta de
     *  updateInventoryForCollectMaterial (misma cantidad y mismo valor neto). */
    void revertInventoryForCollectMaterial(CollectMaterial collectMaterial);

    /** Valor neto que el acopio aporta al Saldo_Mon del articulo. Unica formula: la usan
     *  el alta, la reversa y la previsualizacion del impacto. */
    BigDecimal collectMaterialNetAmount(CollectMaterial collectMaterial);

    void updateInventoryForProduction(XProductionProduct product);

    void updateInventoryForSalesAnnuled(CustomerOrder customerOrder);
    void updateInventoryRemoveFromProduction(ProductionProduct product);
    void updateInventoryRemoveFromProduction(XProductionProduct product);

    void increaseProductItemAmount(ProductItem productItem, BigDecimal newQuantityInventory, BigDecimal amountToAdd, BigDecimal amountCTAdd);
}
