package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.SoldProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.4
 */
@Name("soldProductDataModel")
@Scope(ScopeType.PAGE)
public class SoldProductDataModel extends QueryDataModel<Long, SoldProduct> {
    private static final String[] RESTRICTIONS =
            {
                    "lower(soldProduct.invoiceNumber) like concat(lower(#{soldProductDataModel.criteria.invoiceNumber}), '%')",
                    "soldProduct.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.SoldProductState', 'PENDING')}"
            };

    @Create
    public void init() {
        sortProperty = "soldProduct.invoiceNumber, soldProduct.names, soldProduct.firstName, soldProduct.secondName";
    }

    @Override
    public String getEjbql() {
        return "select distinct(soldProduct) from SoldProduct soldProduct";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
