package com.encens.khipus.action.dashboard;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 3.2
 */
@Name("financesAgileWidgetAction")
@Scope(ScopeType.PAGE)
public class FinancesAgileWidgetAction extends AgileWidgetAction {
    // an array to hold which components
    protected static String[][] graphicViewActions = {
            {"officialPayrollGenerationWidgetAction", "OFFICIALPAYROLLGENERATIONWIDGET"},
            {"dischargeBeforeLifetimeWidgetAction", "DISCHARGEBEFORELIFETIMEWIDGET"},
            {"warehouseMonthlyCloseWidgetAction", "WAREHOUSEMONTHLYCLOSEWIDGET"},
            {"fixedAssetWidgetAction", "FIXEDASSETWIDGET"},
            {"expiredReceivablesWidgetAction", "EXPIREDRECEIVABLESWIDGET"},
            {"checkDeliveryWidgetAction", "CHECKDELIVERYWIDGET"},
            {"maxProductItemControlWidgetAction", "MAXPRODUCTITEMCONTROLWIDGET"},
            {"minProductItemControlWidgetAction", "MINPRODUCTITEMCONTROLWIDGET"}
    };

    @Override
    public String[][] getGraphicViewActions() {
        return graphicViewActions;
    }
}
