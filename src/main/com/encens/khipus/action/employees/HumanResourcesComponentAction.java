package com.encens.khipus.action.employees;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.employees.HumanResources;
import com.encens.khipus.dashboard.module.employees.HumanResourcesInstanceFactory;
import com.encens.khipus.dashboard.module.employees.HumanResourcesSqlQuery;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.6
 */
@Name("humanResourcesComponentAction")
@Scope(ScopeType.EVENT)
public class HumanResourcesComponentAction extends DashboardObjectAction<HumanResources> {
    private Integer executorUnitCode = null;

    @In(create = true)
    private EntityQuery executorUnitQuery;

    @In
    protected Map<String, String> messages;

    public void disableExecutorUnit() {
        executorUnitCode = null;
    }

    public void enableExecutorUnit(Integer code) {
        executorUnitCode = code;
    }

    public String getYear() {
        return MessageUtils.getMessage("Dashboard.year", getSqlQuery().getYear().toString()).trim();
    }

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<HumanResources>) factory.getTotalizer()).getTotals();
    }

    @Override
    public List<HumanResources> getResultList() {
        search();
        return super.getResultList();
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<HumanResources, SumTotalizer<HumanResources>>(
                new HumanResourcesSqlQuery(),
                new HumanResourcesInstanceFactory(),
                new SumTotalizer<HumanResources>()
        );
    }

    @Override
    protected void setFilters() {
        getSqlQuery().setExecutorUnitId(executorUnitCode);
    }

    private HumanResourcesSqlQuery getSqlQuery() {
        return (HumanResourcesSqlQuery) factory.getSqlQuery();
    }
}
