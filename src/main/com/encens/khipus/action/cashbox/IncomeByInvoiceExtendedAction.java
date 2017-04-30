package com.encens.khipus.action.cashbox;

import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author
 * @version 2.7
 */
@Name("incomeByInvoiceExtendedAction")
@Scope(ScopeType.PAGE)
public class IncomeByInvoiceExtendedAction implements Serializable {
    private ExecutorUnit executorUnit;

    private Date startDate = DateUtils.getDate(DateUtils.getCurrentYear(new Date()), 1, 1);

    private Date endDate = new Date();

    @Out
    @In(create = true)
    private IncomeByInvoiceByCategoryAction incomeByInvoiceByCategoryAction;

    @Out
    @In(create = true)
    private IncomeByInvoiceByBranchAction incomeByInvoiceByBranchAction;

    @Out
    @In(create = true)
    private IncomeByInvoiceByConceptAction incomeByInvoiceByConceptAction;

    @Create
    public void initialize() {
        incomeByInvoiceByCategoryAction.search();
        incomeByInvoiceByBranchAction.search();
        incomeByInvoiceByConceptAction.search();
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

    public void setExecutorUnit(ExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public ExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void search() {
        incomeByInvoiceByCategoryAction.search();
        incomeByInvoiceByBranchAction.search();
        incomeByInvoiceByConceptAction.search();
    }

    public IncomeByInvoiceByCategoryAction getIncomeByInvoiceByCategoryAction() {
        return incomeByInvoiceByCategoryAction;
    }

    public IncomeByInvoiceByBranchAction getIncomeByInvoiceByBranchAction() {
        return incomeByInvoiceByBranchAction;
    }

    public IncomeByInvoiceByConceptAction getIncomeByInvoiceByConceptAction() {
        return incomeByInvoiceByConceptAction;
    }
}
