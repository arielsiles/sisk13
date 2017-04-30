package com.encens.khipus.action.products;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.products.ProductState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author:
 */
@Name("productStateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTSTATE','VIEW')}")
public class ProductStateDataModel extends QueryDataModel<Long, ProductState> {

    private static final String[] RESTRICTIONS =
            {"lower(productState.name) like concat('%', concat(lower(#{productStateDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "productState.name";
    }

    @Override
    public String getEjbql() {
        return "select productState from ProductState productState";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
