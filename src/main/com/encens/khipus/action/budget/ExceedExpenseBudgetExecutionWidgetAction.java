package com.encens.khipus.action.budget;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.budget.BudgetExecutionSql;
import com.encens.khipus.model.budget.ClassifierType;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * ExceedExpenseBudgetExecutionWidgetAction
 *
 * @author
 * @version 2.27
 */
@Name("exceedExpenseBudgetExecutionWidgetAction")
@Scope(ScopeType.EVENT)
public class ExceedExpenseBudgetExecutionWidgetAction extends PieWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "17";

    private Integer executorUnitId;

    @Create
    public void initialize() {
        setGraphic(new PieGraphic());
        initializeWidget();
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    @Override
    protected String getXmlWidgetId() {
        return XML_WIDGET_ID;
    }

    @Override
    protected void applyConfigurationFilter(Filter filter, SqlQuery sqlQuery) {
        if (filter instanceof Interval) {
            ((BudgetExecutionSql) sqlQuery).setLowerBound(((Interval) filter).getMinValue());
            ((BudgetExecutionSql) sqlQuery).setUpperBound(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((BudgetExecutionSql) sqlQuery).setExecutorUnitId(executorUnitId);
        ((BudgetExecutionSql) sqlQuery).setClassifierType(ClassifierType.ACCOUNTING_ITEM);
        ((BudgetExecutionSql) sqlQuery).setExceeded(true);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new BudgetExecutionSql();
    }

    public void disableExecutorUnit() {
        executorUnitId = null;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}