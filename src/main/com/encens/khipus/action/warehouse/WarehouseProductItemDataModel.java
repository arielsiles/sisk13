package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.26.4
 */
@Name("warehouseProductItemDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('INVENTORY','VIEW')}")
public class WarehouseProductItemDataModel extends QueryDataModel<InventoryPK, Inventory> {
    private String productItemCode;
    private String productItemName;

    private static final String[] RESTRICTIONS = {
            "warehouse = #{inventoryAction.warehouse}",
            "lower(productItem.id.productItemCode) like concat(lower(#{warehouseProductItemDataModel.productItemCode}), '%')",
            "lower(productItem.name) like concat('%', concat(lower(#{warehouseProductItemDataModel.productItemName}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select inventory from Inventory inventory " +
                "left join fetch inventory.productItem productItem " +
                "left join fetch inventory.warehouse warehouse ";
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

    @Override
    public void clear() {
        productItemCode = null;
        productItemName = null;
        super.clear();
    }
}
