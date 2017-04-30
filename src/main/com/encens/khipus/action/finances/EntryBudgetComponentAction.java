package com.encens.khipus.action.finances;

import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.finances.Budget;
import com.encens.khipus.dashboard.module.finances.BudgetInstanceFactory;
import com.encens.khipus.dashboard.module.finances.BudgetSqlQuery;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.service.dashboard.DashboardQueryService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.6.1
 */
@Name("entryBudgetComponentAction")
@Scope(ScopeType.EVENT)
public class EntryBudgetComponentAction {
    @In
    private DashboardQueryService dashboardQueryService;

    private ComponentFactory<Budget, SumTotalizer<Budget>> factory =
            new ComponentFactory<Budget, SumTotalizer<Budget>>(
                    new BudgetSqlQuery("presupuestoingreso", "ENTRY", "-1"),
                    new BudgetInstanceFactory(),
                    new SumTotalizer<Budget>()
            );

    public List<Budget> getResultList() {
        factory.getTotalizer().initialize();

        List<Budget> resultList = new ArrayList<Budget>();

        List<Month> availableMonths = getMonths();
        for (Month month : availableMonths) {
            getSqlQuery().setMonthConstant(month.name());

            getInstanceFactory().setMonthName(MessageUtils.getMessage(month.getResourceKey()).toUpperCase());
            getSqlQuery().setMonthNumber(month.getValue() + 1);

            List<Budget> partialResult = dashboardQueryService.executeQuery(factory);
            resultList.addAll(partialResult);
        }

        return resultList;
    }

    public Map<String, Number> getTotals() {
        return factory.getTotalizer().getTotals();
    }

    public void disableExecutorUnit() {
        getSqlQuery().setExecutorUnitCode(null);
    }

    public void enableExecutorUnit(Integer code) {
        getSqlQuery().setExecutorUnitCode(code);
    }

    public String getDateRange() {
        String startDate = DateUtils.format(DateUtils.getDate(getSqlQuery().getYear(), 1, 1),
                MessageUtils.getMessage("patterns.date"));
        String endDate = DateUtils.format(new Date(), MessageUtils.getMessage("patterns.date"));

        return MessageUtils.getMessage("Common.range", startDate, endDate).trim();
    }

    private List<Month> getMonths() {
        Month[] enumValues = Month.values();
        Integer currentMonth = DateUtils.getCurrentMonth(new Date());

        List<Month> result = new ArrayList<Month>();

        for (Month month : enumValues) {
            if (month.getValue() + 1 <= currentMonth) {
                result.add(month);
            }
        }

        return result;
    }

    private BudgetSqlQuery getSqlQuery() {
        return (BudgetSqlQuery) factory.getSqlQuery();
    }

    private BudgetInstanceFactory getInstanceFactory() {
        return (BudgetInstanceFactory) factory.getInstanceFactory();
    }

}
