package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieSemaphoreWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.fixedAssets.FixedAssetWidgetSql;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 3.2
 */
@Name("fixedAssetWidgetAction")
@Scope(ScopeType.EVENT)
public class FixedAssetWidgetAction extends PieSemaphoreWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "2";
    public static final String WIDGET_NAME = "fixedAssetWidget";

    private Integer businessUnitId;

    @Create
    public void initialize() {
        super.initialize();
    }

    public void refresh() {
        getGraphic();
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
            ((FixedAssetWidgetSql) sqlQuery).setLowerBound(((Interval) filter).getMinValue());
            ((FixedAssetWidgetSql) sqlQuery).setUpperBound(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((FixedAssetWidgetSql) sqlQuery).setBusinessUnitId(businessUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new FixedAssetWidgetSql();
    }

    @Override
    public String getWidgetName() {
        return WIDGET_NAME;
    }

    public void disableBusinessUnit() {
        businessUnitId = null;
    }

    public void enableBusinessUnit(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }
}
