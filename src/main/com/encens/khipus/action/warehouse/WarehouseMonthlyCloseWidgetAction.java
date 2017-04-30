package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.dashboard.MeterGraphic;
import com.encens.khipus.action.dashboard.MeterWidgetViewAction;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.service.warehouse.MonthProcessService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;

/**
 * @author
 * @version 3.2
 */
@Name("warehouseMonthlyCloseWidgetAction")
@Scope(ScopeType.EVENT)
public class WarehouseMonthlyCloseWidgetAction extends MeterWidgetViewAction<MeterGraphic> {
    public static final String XML_WIDGET_ID = "1";
    public static final String WIDGET_NAME = "warehouseMonthlyCloseWidget";

    @In
    private MonthProcessService monthProcessService;

    private Month month;

    private Date today;
    private Date monthProcess;

    @Override
    public String getXmlWidgetId() {
        return XML_WIDGET_ID;
    }

    @Create
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void resetFilters() {
        today = new Date();
        monthProcess = monthProcessService.getMothProcessDate(today);
        month = Month.getMonth(monthProcess);
        super.resetFilters();
    }

    @Override
    protected void refresh() {
        if (!DateUtils.getCurrentYear(today).equals(DateUtils.getCurrentYear(monthProcess))
                || !DateUtils.getCurrentMonth(today).equals(DateUtils.getCurrentMonth(monthProcess))) {
            meterValue = DateUtils.daysBetween(monthProcess, today);
        }
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    public Month getMonth() {
        return month;
    }

    @Override
    protected void setGraphicParameters(MeterGraphic graphic) {
        graphic.setWidget(widget);
        graphic.setMeterValue(meterValue);
    }

    @Override
    public void search() {
    }

    @Override
    protected DtoConfiguration getDtoConfiguration() {
        return null;
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return null;
    }

    @Override
    public String getWidgetName() {
        return WIDGET_NAME;
    }

}
