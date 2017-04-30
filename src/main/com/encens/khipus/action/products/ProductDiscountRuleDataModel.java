package com.encens.khipus.action.products;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.products.ProductDiscountRule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Product Discount Rule
 *
 * @author:
 */

@Name("productDiscountRuleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTDISCOUNTRULE','VIEW')}")
public class ProductDiscountRuleDataModel extends QueryDataModel<Long, ProductDiscountRule> {

    private static final String[] RESTRICTIONS =
            {"lower(productDiscountRule.name) like concat('%', concat(lower(#{productDiscountRuleDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "productDiscountRule.name";
    }

    @Override
    public String getEjbql() {
        return "select productDiscountRule from ProductDiscountRule productDiscountRule";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}