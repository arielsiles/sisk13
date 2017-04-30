package com.encens.khipus.action.finances;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.finances.sql.PendingReceiptAccountsToPaySql;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Action for the pie widget
 * @author
 * @version 2.26
 */
@Name("pendingReceiptAccountsToPayWidgetAction")
@Scope(ScopeType.EVENT)
public class PendingReceiptAccountsToPayWidgetAction extends PieWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "16";

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
            ((PendingReceiptAccountsToPaySql) sqlQuery).setLowerBound(((Interval) filter).getMinValue());
            ((PendingReceiptAccountsToPaySql) sqlQuery).setUpperBound(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((PendingReceiptAccountsToPaySql) sqlQuery).setExecutorUnitId(executorUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new PendingReceiptAccountsToPaySql();
    }

    public void disableExecutorUnit() {
        executorUnitId = null;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}
