package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("provideSearchDataModel")
@Scope(ScopeType.PAGE)
public class ProvideSearchDataModel extends QueryDataModel<Long, Provide> {

    private String productItemName;
    private String productItemCode;

    private static final String[] RESTRICTIONS =
            {
                    "lower(productItem.id.productItemCode) like concat(lower(#{provideSearchDataModel.productItemCode}), '%')",
                    "lower(productItem.name) like concat('%',concat(lower(#{provideSearchDataModel.productItemName}), '%'))",
                    "element.providerCode = #{provideSearchDataModel.criteria.providerCode}",
                    "productItem.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.ProductItemState', 'VIG')}"
            };

    @Create
    public void init() {
        sortProperty = "productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select productItem from Provide element inner join element.productItem productItem";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getProductItemName() {
        return productItemName;
    }

    public void setProductItemName(String productItemName) {
        this.productItemName = productItemName;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public void filterByProviderCode(String providerCode) {
        getCriteria().setProviderCode(providerCode);
        updateAndSearch();
    }


    public List<ProductItem> getSelectedProductItems() {
        List ids = super.getSelectedIdList();

        List<ProductItem> result = new ArrayList<ProductItem>();
        for (Object id : ids) {
            result.add(getEntityManager().find(ProductItem.class, id));
        }

        return result;
    }
}
