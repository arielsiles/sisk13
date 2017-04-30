package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.1
 */
@Name("productItemInputSearchDataModel")
@Scope(ScopeType.PAGE)
public class ProductItemInputSearchDataModel extends QueryDataModel<ProductItemPK, ProductItem> {

    private List<String> groups = new ArrayList<String>();

    private static final String[] RESTRICTIONS =
            {
                    "lower(productItem.id.productItemCode) like concat(lower(#{productItemInputSearchDataModel.criteria.id.productItemCode}), '%')",
                    "lower(productItem.name) like concat('%',concat(lower(#{productItemInputSearchDataModel.criteria.name}), '%'))",
                    "productItem.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.ProductItemState', 'VIG')}",
                    "productItem.groupCode in (#{productItemInputSearchDataModel.groups})"
            };

    @Create
    public void init() {
        sortProperty = "productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select productItem from ProductItem productItem";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public List<ProductItem> getSelectedProductItems() {
        List ids = super.getSelectedIdList();

        List<ProductItem> result = new ArrayList<ProductItem>();
        for (Object id : ids) {
            result.add(getEntityManager().find(ProductItem.class, id));
        }

        return result;
    }
    //todo:listar los grupos realacionados solo con los articulos
    public List<String> getGroups() {
        if(groups.size()==0)
        {
            groups.add("2");
        }
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
