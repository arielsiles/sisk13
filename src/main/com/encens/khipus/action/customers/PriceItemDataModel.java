package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.PriceItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Customer category
 *
 * @author:
 */

@Name("priceItemDataModel")
@Scope(ScopeType.PAGE)
public class PriceItemDataModel extends QueryDataModel<Long, PriceItem> {

    private static final String[] RESTRICTIONS =
            {"lower(priceItem.productItemCode) like concat('%', concat(lower(#{priceItemDataModel.criteria.productItemCode}), '%'))"};

    @Create
    public void init() {
        sortProperty = "priceItem.productItemCode";
    }

    @Override
    public String getEjbql() {
        return "select priceItem from PriceItem priceItem";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
