package com.encens.khipus.action.finances;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.finances.FoundsToPayWidgetSql;
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
@Name("foundsToPayWidgetAction")
@Scope(ScopeType.EVENT)
public class FoundsToPayWidgetAction extends PieWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "7";

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
            ((FoundsToPayWidgetSql) sqlQuery).setStart(((Interval) filter).getMinValue());
            ((FoundsToPayWidgetSql) sqlQuery).setEnd(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((FoundsToPayWidgetSql) sqlQuery).setExecutorUnitId(executorUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new FoundsToPayWidgetSql();
    }

    public void disableExecutorUnit() {
        executorUnitId = null;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}
