package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.XProductionPlan;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Name("xproductionPlanDataModel")
@Scope(ScopeType.PAGE)
public class XProductionPlanDataModel extends QueryDataModel<Long, XProductionPlan> {

    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS = {"" +
            "productionPlan.date >= #{xproductionPlanDataModel.startDate}",
            "productionPlan.date <= #{xproductionPlanDataModel.endDate}"};

    @Create
    public void init() {
        sortProperty = "productionPlan.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select productionPlan from XProductionPlan productionPlan";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}