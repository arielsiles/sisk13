package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.fixedAssets.PendingFixedAssetMaintenanceRequestWidgetSql;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.27
 */

@Name("pendingFixedAssetMaintenanceRequestWidgetAction")
@Scope(ScopeType.EVENT)
public class PendingFixedAssetMaintenanceRequestWidgetAction extends PieWidgetViewAction<PieGraphic> {

    public static final String XML_WIDGET_ID = "5";

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
            ((PendingFixedAssetMaintenanceRequestWidgetSql) sqlQuery).setStart(((Interval) filter).getMinValue());
            ((PendingFixedAssetMaintenanceRequestWidgetSql) sqlQuery).setEnd(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((PendingFixedAssetMaintenanceRequestWidgetSql) sqlQuery).setBusinessUnitId(businessUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new PendingFixedAssetMaintenanceRequestWidgetSql();
    }

    public void disableBusinessUnit() {
        businessUnitId = null;
    }

    public void enableBusinessUnit(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

}
