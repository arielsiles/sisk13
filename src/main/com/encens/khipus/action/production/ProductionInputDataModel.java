package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionInput;
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
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("productionInputDataModel")
@Scope(ScopeType.PAGE)
public class ProductionInputDataModel extends QueryDataModel<Long, ProductionInput> {

    private static final String[] RESTRICTIONS = {
            "upper(productionInput.name) like concat(concat('%',upper(#{productionInputDataModel.criteria.name})), '%')",
            "upper(productionInput.code) like concat(concat('%',upper(#{productionInputDataModel.criteria.code})), '%')",
            "upper(productionInput.description) like concat(concat('%',upper(#{productionInputDataModel.criteria.description})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "productionInput.name";
    }

    @Override
    public String getEjbql() {
        return "select productionInput " +
                "from ProductionInput productionInput " +
                "left join fetch productionInput.measureUnitProduction";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
