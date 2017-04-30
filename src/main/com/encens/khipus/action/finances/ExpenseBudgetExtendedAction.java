package com.encens.khipus.action.finances;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.finances.BudgetExtended;
import com.encens.khipus.dashboard.module.finances.BudgetExtendedInstanceFactory;
import com.encens.khipus.dashboard.module.finances.BudgetExtendedSqlQuery;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.model.employees.Month;
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
@Name("expenseBudgetExtendedAction")
@Scope(ScopeType.PAGE)
public class ExpenseBudgetExtendedAction extends DashboardObjectAction<BudgetExtended> {
    private ExecutorUnit executorUnit;
    private Month month = Month.getCurrentMonth();
    private Integer year = DateUtils.getCurrentYear(new Date());

    public ExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(ExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Create
    public void initialize() {
        search();
    }

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<BudgetExtended>) factory.getTotalizer()).getTotals();
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<BudgetExtended, SumTotalizer<BudgetExtended>>(
                new BudgetExtendedSqlQuery("1", "presupuestogasto", "EXPENSE", "ACCOUNTING_ITEM"),
                new BudgetExtendedInstanceFactory(),
                new SumTotalizer<BudgetExtended>()
        );
    }

    @Override
    protected void setFilters() {
        Integer executorUnitCode = null;
        if (null != executorUnit) {
            executorUnitCode = executorUnit.getId();
        }

        ((BudgetExtendedSqlQuery) factory.getSqlQuery()).setExecutorUnitCode(executorUnitCode);
        ((BudgetExtendedSqlQuery) factory.getSqlQuery()).setMonth(month);
        ((BudgetExtendedSqlQuery) factory.getSqlQuery()).setYear(year);
    }
}
