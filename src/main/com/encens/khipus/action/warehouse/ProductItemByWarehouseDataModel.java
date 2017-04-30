package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryPK;
import com.encens.khipus.model.warehouse.ProductItem;
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
@Name("productItemByWarehouseDataModel")
@Scope(ScopeType.PAGE)
public class ProductItemByWarehouseDataModel extends QueryDataModel<InventoryPK, Inventory> {
    private String productItemCode;
    private String productItemName;
    private static final String[] RESTRICTIONS =
            {
                    "inventory.warehouse = #{warehouseVoucherCreateAction.warehouseVoucher.warehouse}",
                    "inventory.warehouse = #{warehouseVoucherUpdateAction.warehouseVoucher.warehouse}",
                    "lower(inventory.productItem.id.productItemCode) like concat(lower(#{productItemByWarehouseDataModel.productItemCode}), '%')",
                    "lower(inventory.productItem.name) like concat('%',concat(lower(#{productItemByWarehouseDataModel.productItemName}), '%'))",
                    "inventory.productItem.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.ProductItemState', 'VIG')}"
            };

    @Create
    public void init() {
        sortProperty = "inventory.productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select inventory " +
                " from Inventory inventory " +
                " left join fetch inventory.productItem productItem" +
                " left join fetch inventory.warehouse";
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
            Inventory item = getEntityManager().find(Inventory.class, id);
            result.add(item.getProductItem());
        }

        return result;
    }
}
