package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.MetaProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("metaProductDataModel")
@Scope(ScopeType.PAGE)
public class MetaProductDataModel extends QueryDataModel<Long, MetaProduct> {
    private static final String[] RESTRICTIONS = {
            "upper(metaProduct.code) like concat(concat('%',upper(#{metaProductDataModel.criteria.code})), '%')",
            "upper(metaProduct.name) like concat(concat('%',upper(#{metaProductDataModel.criteria.name})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "metaProduct.name";
    }

    @Override
    public String getEjbql() {
        return "select metaProduct from MetaProduct metaProduct";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
