package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.dashboard.PieGraphic;
import com.encens.khipus.action.dashboard.PieSemaphoreWidgetViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.warehouse.MaxProductItemControlWidgetSql;
import com.encens.khipus.dashboard.util.SemaphoreState;
import com.encens.khipus.model.dashboard.Filter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Action to manage max product item control widget
 * @author
 * @version 3.3
 */
@Name("maxProductItemControlWidgetAction")
@Scope(ScopeType.EVENT)
public class MaxProductItemControlWidgetAction extends PieSemaphoreWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "20";
    public static final String WIDGET_NAME = "maxProductItemControlWidget";

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
    protected SqlQuery getSqlQueryInstance() {
        return new MaxProductItemControlWidgetSql();
    }

    @Override
    protected void applyConfigurationFilter(Filter filter, SqlQuery sqlQuery) {
        MaxProductItemControlWidgetSql widgetSql = (MaxProductItemControlWidgetSql) sqlQuery;

        if (0 == filter.getIndex()) {
            widgetSql.setSemaphoreState(SemaphoreState.GREEN);
        } else if (1 == filter.getIndex()) {
            widgetSql.setSemaphoreState(SemaphoreState.YELLOW);
        } else if (2 == filter.getIndex()) {
            widgetSql.setSemaphoreState(SemaphoreState.RED);
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((MaxProductItemControlWidgetSql) sqlQuery).setBusinessUnitId(businessUnitId);
    }

    @Override
    protected String getXmlWidgetId() {
        return XML_WIDGET_ID;
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
