package com.encens.khipus.action.purchases;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.purchases.PendingInvoicesWidgetWidgetSql;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.26
 */
@Name("pendingInvoicesWidgetAction")
@Scope(ScopeType.EVENT)
public class PendingInvoicesWidgetAction extends PieWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "8";

    private Integer businessUnitId;

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
            ((PendingInvoicesWidgetWidgetSql) sqlQuery).setStart(((Interval) filter).getMinValue());
            ((PendingInvoicesWidgetWidgetSql) sqlQuery).setEnd(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((PendingInvoicesWidgetWidgetSql) sqlQuery).setBusinessUnitId(businessUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new PendingInvoicesWidgetWidgetSql();
    }

    public void disableBusinessUnit() {
        businessUnitId = null;
    }

    public void enableBusinessUnit(Integer executorUnitId) {
        this.businessUnitId = executorUnitId;
    }
}
