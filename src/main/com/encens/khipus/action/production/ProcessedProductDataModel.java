package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProcessedProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 10:41 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("processedProductDataModel")
@Scope(ScopeType.PAGE)
public class ProcessedProductDataModel extends QueryDataModel<Long, ProcessedProduct> {
    private static final String[] RESTRICTIONS = {
            "upper(processedProduct.name) like concat(concat('%',upper(#{processedProductDataModel.criteria.name})), '%')",
            "upper(processedProduct.code) like concat(concat('%',upper(#{processedProductDataModel.criteria.code})), '%')",
            "upper(processedProduct.description) like concat(concat('%',upper(#{processedProductDataModel.criteria.description})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "processedProduct.name";
    }

    @Override
    public String getEjbql() {
        return "select processedProduct from ProcessedProduct processedProduct";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
