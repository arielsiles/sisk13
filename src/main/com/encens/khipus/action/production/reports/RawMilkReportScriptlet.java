package com.encens.khipus.action.production.reports;

import com.encens.khipus.service.production.ProductionPlanningService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Encens S.R.L.
 *
 *
 * @author
 * @version 2.3
 */
public class RawMilkReportScriptlet extends JRDefaultScriptlet {

    private ProductionPlanningService productionPlanningService = (ProductionPlanningService) Component.getInstance("productionPlanningService");
    private RawMilkReportAction rawMilkReportAction = (RawMilkReportAction) Component.getInstance("rawMilkReportAction");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String codGroup = (String) this.getFieldValue("subGroup.groupCode");
        String codSubGroup = (String) this.getFieldValue("subGroup.subGroupCode");
        Date startDate = rawMilkReportAction.getStartDate();
        Date endDate = rawMilkReportAction.getEndDate();

        BigDecimal totalAmount = BigDecimal.ZERO;

        totalAmount = productionPlanningService.getTotalMilkBySubGroup(codGroup, codSubGroup, startDate, endDate);

        this.setVariableValue("amount", totalAmount);

    }


}
