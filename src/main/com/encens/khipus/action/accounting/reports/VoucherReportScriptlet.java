package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.service.production.ProductionPlanningService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 *
 *
 * @author
 * @version 2.3
 */
public class VoucherReportScriptlet extends JRDefaultScriptlet {

    private ProductionPlanningService productionPlanningService = (ProductionPlanningService) Component.getInstance("productionPlanningService");
    private VoucherReportAction voucherReportAction = (VoucherReportAction) Component.getInstance("voucherReportAction");
    private VoucherUpdateAction voucherUpdateAction = (VoucherUpdateAction) Component.getInstance("voucherUpdateAction");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        //String codArt = (String) this.getFieldValue("productItemCode");

        /*Date startDate = productsProducedReportAction.getStartDate();
        Date endDate = productsProducedReportAction.getEndDate();

        Double totalProducedOrder = productionPlanningService.getTotalProducedOrderByArticleAndDate(codArt,startDate,endDate);
        Double totalProducedRepro = productionPlanningService.getTotalProducedReproByArticleAndDate(codArt,startDate,endDate);

        this.setVariableValue("totalOrder", totalProducedOrder);
        this.setVariableValue("totalRepro", totalProducedRepro);
        this.setVariableValue("total", totalProducedOrder + totalProducedRepro);*/

        BigDecimal totalDebits  = voucherUpdateAction.getTotalsDebit();
        BigDecimal totalCredits = voucherUpdateAction.getTotalsCredit();

        System.out.println("===========> T. Debits: " + totalDebits);
        System.out.println("===========> T. Credits: " + totalCredits);

        this.setVariableValue("totalDebits", totalDebits);
        this.setVariableValue("totalCredits", totalCredits);
    }


}
