package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.XProduction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Name("xproductionDataModel")
@Scope(ScopeType.PAGE)
public class XProductionDataModel extends QueryDataModel<Long, XProduction> {

    private static final String[] RESTRICTIONS = {"production.productionPlan = #{productionPlan}"};

    @Create
    public void init() {}

    @Override
    public String getEjbql() {
        return "select production from XProduction production";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


}