package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionPlan;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Name("productionPlanDataModel")
@Scope(ScopeType.PAGE)
public class ProductionPlanDataModel extends QueryDataModel<Long, ProductionPlan> {
    private static final String[] RESTRICTIONS =
            {"productionPlan.date = #{gestionDataModel.criteria.year}"};

    @Create
    public void init() {
        sortProperty = "productionPlan.date";
    }

    @Override
    public String getEjbql() {
        return "select productionPlan from ProductionPlan productionPlan";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}