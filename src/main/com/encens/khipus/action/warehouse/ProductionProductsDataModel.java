package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryPK;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProductItemByWarehouseDataModel
 *
 * @author
 * @version 2.17
 */
@Name("productionProductsDataModel")
@Scope(ScopeType.PAGE)
public class ProductionProductsDataModel extends QueryDataModel<InventoryPK, Inventory> {
    private String productItemCode;
    private String productItemName;
    private WarehouseType warehouseType = WarehouseType.FINISHED_GOODS;

    private static final String[] RESTRICTIONS =
            {
                    "lower(productItem.productItemCode) like concat(lower(#{productionProductsDataModel.productItemCode}), '%')",
                    "lower(productItem.name) like concat('%',concat(lower(#{productionProductsDataModel.productItemName}), '%'))",
                    "productItem.warehouse.warehouseType = #{productionProductsDataModel.warehouseType}",
                    "productItem.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.ProductItemState', 'VIG')}"
            };

    @Create
    public void init() {
        sortProperty = "productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select productItem " +
                " from ProductItem productItem ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getProductItemName() {
        return productItemName;
    }

    public void setProductItemName(String productItemName) {
        this.productItemName = productItemName;
    }

    public List<ProductItem> getSelectedProductItems() {
        List ids = super.getSelectedIdList();

        List<ProductItem> result = new ArrayList<ProductItem>();
        for (Object id : ids) {
            ProductItem item = getEntityManager().find(ProductItem.class, id);
            result.add(item);
        }

        return result;
    }

    public WarehouseType getWarehouseType() {
        return warehouseType;
    }

    public void setWarehouseType(WarehouseType warehouseType) {
        this.warehouseType = warehouseType;
    }
}
