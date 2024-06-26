package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.warehouse.ProductItemMinimalStockIsGreaterThanMaximumStockException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.warehouse.InventoryPeriod;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseVoucher;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.0
 */
@Local
public interface ProductItemService extends GenericService {

    @TransactionAttribute(REQUIRES_NEW)
    void createProductItem(ProductItem productItem)
            throws EntryDuplicatedException, ProductItemMinimalStockIsGreaterThanMaximumStockException;

    @TransactionAttribute(REQUIRES_NEW)
    void updateProductItem(ProductItem productItem)
            throws EntryDuplicatedException, ProductItemNotFoundException,
            ConcurrencyException, ProductItemMinimalStockIsGreaterThanMaximumStockException;

    /**
     * Finds a list of ProductItems involved in a WarehouseVoucher
     *
     * @param warehouseVoucher a given WarehouseVoucher
     * @return a list of ProductItems involved in a WarehouseVoucher
     */
    @SuppressWarnings(value = "unchecked")
    List<ProductItem> findByWarehouseVoucher(WarehouseVoucher warehouseVoucher);

    List<ProductItem> findByWarehouseCode(String warehouseCode);

    /**
     * Finds a list of ProductItems involved in a ProductItem List
     *
     * @param productItemList a given ProductItem List
     * @return a list of ProductItems involved in a ProductItem List
     */
    @SuppressWarnings(value = "unchecked")
    List<ProductItem> findInProductItemList(List<ProductItem> productItemList);

    @SuppressWarnings(value = "unchecked")
    public ProductItem findProductItemByCode(String productItemCode);

    List<ArticulosPromocion> findArticuloCombo(ArticleOrder articulo);

    @SuppressWarnings(value = "unchecked")
    public BigDecimal getInitialInventoryYear(String productItemCode, String year);

    void createProductInventory(ProductItem productItem);

    List<InventoryPeriod> getInventoryPeriodInitialList(String warehouseCode, String year);

    List<ProductItem> findProductItemList();

    List<ProductItem> findBestProductList();

    EconomicActivity findEconomicActivity(String code);
    ProductsServices findProductsAndServices(Integer code);
    ProductsServices findProductsAndServices(String activityCode, Integer productCode);
    MeasureUnitSIN findMeasureUnitSIN(Integer code);
}
