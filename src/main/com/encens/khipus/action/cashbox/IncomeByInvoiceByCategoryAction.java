package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.cashbox.IncomeByInvoiceByCategory;
import com.encens.khipus.dashboard.module.cashbox.IncomeByInvoiceByCategoryInstanceFactory;
import com.encens.khipus.dashboard.module.cashbox.IncomeByInvoiceByCategorySqlQuery;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Map;

/**
 * @author
 * @version 2.7
 */
@Name("incomeByInvoiceByCategoryAction")
@Scope(ScopeType.PAGE)
public class IncomeByInvoiceByCategoryAction extends DashboardObjectAction<IncomeByInvoiceByCategory> {
    @In
    private IncomeByInvoiceExtendedAction incomeByInvoiceExtendedAction;

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<IncomeByInvoiceByCategory>) factory.getTotalizer()).getTotals();
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<IncomeByInvoiceByCategory, SumTotalizer<IncomeByInvoiceByCategory>>(
                new IncomeByInvoiceByCategorySqlQuery(),
                new IncomeByInvoiceByCategoryInstanceFactory(),
                new SumTotalizer<IncomeByInvoiceByCategory>()
        );
    }

    @Override
    protected void setFilters() {
        Integer executorUnitCode = null;
        if (null != incomeByInvoiceExtendedAction.getExecutorUnit()) {
            executorUnitCode = incomeByInvoiceExtendedAction.getExecutorUnit().getId();
        }

        Integer startDateAsInteger = null;
        if (null != incomeByInvoiceExtendedAction.getStartDate()) {
            startDateAsInteger = DateUtils.dateToInteger(incomeByInvoiceExtendedAction.getStartDate());
        }

        Integer endDateAsInteger = null;
        if (null != incomeByInvoiceExtendedAction.getEndDate()) {
            endDateAsInteger = DateUtils.dateToInteger(incomeByInvoiceExtendedAction.getEndDate());
        }

        ((IncomeByInvoiceByCategorySqlQuery) factory.getSqlQuery()).setExecutorUnitCode(executorUnitCode);
        ((IncomeByInvoiceByCategorySqlQuery) factory.getSqlQuery()).setStartDate(startDateAsInteger);
        ((IncomeByInvoiceByCategorySqlQuery) factory.getSqlQuery()).setEndDate(endDateAsInteger);
    }
}
