package com.encens.khipus.action.employees;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.employees.HumanResourcesExpenseExtended;
import com.encens.khipus.dashboard.module.employees.HumanResourcesExpenseExtendedInstanceFactory;
import com.encens.khipus.dashboard.module.employees.HumanResourcesExpenseExtendedSqlQuery;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.Map;

/**
 * @author
 * @version 2.7
 */
@Name("humanResourcesExpenseExtendedAction")
@Scope(ScopeType.PAGE)
public class HumanResourcesExpenseExtendedAction extends DashboardObjectAction<HumanResourcesExpenseExtended> {
    private Integer year = DateUtils.getCurrentYear(new Date());

    private ExecutorUnit executorUnit;

    @Create
    public void initialize() {
        search();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(ExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<HumanResourcesExpenseExtended>) factory.getTotalizer()).getTotals();
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<HumanResourcesExpenseExtended, SumTotalizer<HumanResourcesExpenseExtended>>(
                new HumanResourcesExpenseExtendedSqlQuery(),
                new HumanResourcesExpenseExtendedInstanceFactory(),
                new SumTotalizer<HumanResourcesExpenseExtended>()
        );
    }

    @Override
    protected void setFilters() {
        Integer executorUnitCode = null;
        if (null != executorUnit) {
            executorUnitCode = executorUnit.getId();
        }

        ((HumanResourcesExpenseExtendedSqlQuery) factory.getSqlQuery()).setExecutorUnitCode(executorUnitCode);
        ((HumanResourcesExpenseExtendedSqlQuery) factory.getSqlQuery()).setYear(year);
    }
}
