package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.cashbox.IncomeByInvoiceByConcept;
import com.encens.khipus.dashboard.module.cashbox.IncomeByInvoiceByConceptInstanceFactory;
import com.encens.khipus.dashboard.module.cashbox.IncomeByInvoiceByConceptSqlQuery;
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

@Name("incomeByInvoiceByConceptAction")
@Scope(ScopeType.PAGE)
public class IncomeByInvoiceByConceptAction extends DashboardObjectAction<IncomeByInvoiceByConcept> {
    @In
    private IncomeByInvoiceExtendedAction incomeByInvoiceExtendedAction;

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<IncomeByInvoiceByConcept>) factory.getTotalizer()).getTotals();
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<IncomeByInvoiceByConcept, SumTotalizer<IncomeByInvoiceByConcept>>(
                new IncomeByInvoiceByConceptSqlQuery(),
                new IncomeByInvoiceByConceptInstanceFactory(),
                new SumTotalizer<IncomeByInvoiceByConcept>()
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
        ((IncomeByInvoiceByConceptSqlQuery) factory.getSqlQuery()).setExecutorUnitCode(executorUnitCode);
        ((IncomeByInvoiceByConceptSqlQuery) factory.getSqlQuery()).setStartDate(startDateAsInteger);
        ((IncomeByInvoiceByConceptSqlQuery) factory.getSqlQuery()).setEndDate(endDateAsInteger);
    }
}
