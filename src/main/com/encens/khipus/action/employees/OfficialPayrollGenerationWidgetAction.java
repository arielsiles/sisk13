package com.encens.khipus.action.employees;

import com.encens.khipus.action.dashboard.MeterGraphic;
import com.encens.khipus.action.dashboard.MeterWidgetViewAction;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.dashboard.Interval;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.service.employees.GestionService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
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
@Name("officialPayrollGenerationWidgetAction")
@Scope(ScopeType.EVENT)
public class OfficialPayrollGenerationWidgetAction extends MeterWidgetViewAction<MeterGraphic> {
    public static final String XML_WIDGET_ID = "9";
    public static final String WIDGET_NAME = "officialPayrollGenerationWidget";
    @In
    private GestionService gestionService;

    @In
    private GeneratedPayrollService generatedPayrollService;

    private Gestion gestion;
    private Month month;
    private Integer executorUnitId;
    private int total;
    private int generated;

    @Override
    public String getXmlWidgetId() {
        return XML_WIDGET_ID;
    }

    @Create
    public void initialize() {
        super.initialize();
    }

    protected void resetFilters() {
        Date today = new Date();
        int year = DateUtils.getCurrentYear(today);
        month = Month.getMonth(DateUtils.getCurrentMonth(today));
        gestion = gestionService.getGestion(year);
        super.resetFilters();
    }

    public void refresh() {
        int[] result = generatedPayrollService.calculateGeneratedPayrolls(gestion.getYear(), month.name(), executorUnitId);
        total = result[0];
        generated = result[1];
        meterValue = Integer.valueOf(result[2]).longValue();
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    public String getTitle() {
        return MessageUtils.getMessage("Widget.title.officialPayrollGeneration", generated, total, meterValue);
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
        resetFilters();
    }

    public void disableExecutorUnit() {
        executorUnitId = null;
        resetFilters();
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

    @Override
    public boolean isInThirdInterval() {
        Interval interval = (Interval) widget.getFilters().get(firstPosition);
        return null != interval && null != interval.getMaxValue()
                && meterValue <= (interval.getMaxValue());
    }

    @Override
    public boolean isInFirstInterval() {
        Interval interval = (Interval) widget.getFilters().get(thirdPosition);
        return null != interval && null != interval.getMaxValue()
                && meterValue >= interval.getMinValue();
    }

}
