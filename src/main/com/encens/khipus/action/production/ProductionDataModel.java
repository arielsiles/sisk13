package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.Production;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Name("productionDataModel")
@Scope(ScopeType.PAGE)
public class ProductionDataModel extends QueryDataModel<Long, Production> {

    private static final String[] RESTRICTIONS = {"production.productionPlan = #{productionPlan}"};

    @Create
    public void init() {
        sortProperty = "productionPlan.date";
    }

    @Override
    public String getEjbql() {
        return "select production from Production production";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


}