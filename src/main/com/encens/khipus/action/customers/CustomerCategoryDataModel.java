package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CustomerCategory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Customer category
 *
 * @author:
 */

@Name("customerCategoryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
public class CustomerCategoryDataModel extends QueryDataModel<Long, CustomerCategory> {

    private static final String[] RESTRICTIONS =
            {"lower(customerCategory.name) like concat('%', concat(lower(#{customerCategoryDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "customerCategory.name";
    }

    @Override
    public String getEjbql() {
        return "select customerCategory from CustomerCategory customerCategory";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
