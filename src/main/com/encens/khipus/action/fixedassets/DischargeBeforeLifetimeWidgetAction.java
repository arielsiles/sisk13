package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.dashboard.MeterGraphic;
import com.encens.khipus.action.dashboard.MeterWidgetViewAction;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.dashboard.Widget;
import com.encens.khipus.service.dashboard.WidgetService;
import com.encens.khipus.service.fixedassets.FixedAssetService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.2
 */
@Name("dischargeBeforeLifetimeWidgetAction")
@Scope(ScopeType.EVENT)
public class DischargeBeforeLifetimeWidgetAction extends MeterWidgetViewAction<MeterGraphic> {
    public static final String XML_WIDGET_ID = "4";
    public static final String WIDGET_NAME = "dischargeBeforeLifetimeWidget";

    @In
    public WidgetService widgetService;
    @In
    public FixedAssetService fixedAssetService;

    private BigDecimal totalDischarged;
    private BigDecimal totalDischargedBeforeLifetime;

    private Integer businessUnitId;

    @Override
    public String getXmlWidgetId() {
        return XML_WIDGET_ID;
    }

    @Create
    public void initialize() {
        super.initialize();
    }

    protected void resetFilters() {
        super.resetFilters();
    }

    protected void refresh() {
        totalDischarged = fixedAssetService.findRegisteredFixedAssets(businessUnitId);
        totalDischargedBeforeLifetime = fixedAssetService.findFADischargedBeforeLifetime(businessUnitId);
        if (BigDecimalUtil.isPositive(totalDischargedBeforeLifetime) && BigDecimalUtil.isPositive(totalDischarged)) {
            meterValue = BigDecimalUtil.multiply(
                    BigDecimalUtil.divide(totalDischargedBeforeLifetime, totalDischarged)
                    , BigDecimalUtil.ONE_HUNDRED)
                    .longValue();
        }
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    public String getTitle() {
        return MessageUtils.getMessage("Widget.title.dischargeBeforeLifetime", totalDischargedBeforeLifetime, totalDischarged, meterValue);
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


    public void disableBusinessUnit() {
        businessUnitId = null;
        resetFilters();
    }

    public void enableBusinessUnit(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
        resetFilters();
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public BigDecimal getTotalDischarged() {
        return totalDischarged;
    }

    public void setTotalDischarged(BigDecimal totalDischarged) {
        this.totalDischarged = totalDischarged;
    }

    public BigDecimal getTotalDischargedBeforeLifetime() {
        return totalDischargedBeforeLifetime;
    }

    public void setTotalDischargedBeforeLifetime(BigDecimal totalDischargedBeforeLifetime) {
        this.totalDischargedBeforeLifetime = totalDischargedBeforeLifetime;
    }

    public Integer getBusinessUnitId() {
        return businessUnitId;
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    @Override
    public String getWidgetName() {
        return WIDGET_NAME;
    }

}
