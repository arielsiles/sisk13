package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.xproduction.XProductionProduct;
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
@Name("xproductionProductAssignedDataModel")
@Scope(ScopeType.PAGE)
public class XProductionProductAssignedDataModel extends QueryDataModel<Long, XProductionProduct> {
    private String productItemCode;
    private String productItemName;

    private static final String[] RESTRICTIONS =
            {
                    "product.production = #{xproduction}"
            };

    @Create
    public void init() {
        sortProperty = "product.productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select product " +
                " from XProductionProduct product ";
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
