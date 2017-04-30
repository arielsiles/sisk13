package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.finances.CheckDeliveryWidgetSql;
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
@Name("checkDeliveryWidgetAction")
@Scope(ScopeType.EVENT)
public class CheckDeliveryWidgetAction extends PieSemaphoreWidgetViewAction<PieGraphic> {
    public static final String XML_WIDGET_ID = "19";
    public static final String WIDGET_NAME = "checkDeliveryWidget";

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
            ((CheckDeliveryWidgetSql) sqlQuery).setLowerBound(((Interval) filter).getMinValue());
            ((CheckDeliveryWidgetSql) sqlQuery).setUpperBound(((Interval) filter).getMaxValue());
        }
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((CheckDeliveryWidgetSql) sqlQuery).setBusinessUnitId(businessUnitId);
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new CheckDeliveryWidgetSql();
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
