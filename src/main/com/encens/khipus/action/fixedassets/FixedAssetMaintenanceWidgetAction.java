package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.fixedAssets.FixedAssetMaintenanceWidgetSql;
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
@Name("fixedAssetMaintenanceWidgetAction")
@Scope(ScopeType.EVENT)
public class FixedAssetMaintenanceWidgetAction extends PieWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "3";

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
            ((FixedAssetMaintenanceWidgetSql) sqlQuery).setStart(((Interval) filter).getMinValue());
            ((FixedAssetMaintenanceWidgetSql) sqlQuery).setEnd(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((FixedAssetMaintenanceWidgetSql) sqlQuery).setExecutorUnitId(executorUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new FixedAssetMaintenanceWidgetSql();
    }

    public void disableExecutorUnit() {
        executorUnitId = null;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}
