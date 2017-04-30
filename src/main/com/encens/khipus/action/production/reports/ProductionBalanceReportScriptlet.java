package com.encens.khipus.action.production.reports;

import com.encens.khipus.service.production.ProcessedProductService;
import com.encens.khipus.service.production.ProductionPlanningService;
import com.encens.khipus.util.RoundUtil;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.util.Date;

/**
 * Encens S.R.L.
 *
 *
 * @author
 * @version 2.3
 */
public class ProductionBalanceReportScriptlet extends JRDefaultScriptlet {

    private ProductionPlanningService productionPlanningService = (ProductionPlanningService) Component.getInstance("productionPlanningService");
    private ProductionBalanceReportAction productionBalanceReportAction = (ProductionBalanceReportAction) Component.getInstance("productionBalanceReportAction");
    private ProcessedProductService processedProductService = (ProcessedProductService) Component.getInstance("processedProductService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String codArt = (String) this.getFieldValue("productItemCode");
        Date date = productionBalanceReportAction.getDate();

        Double totalMilk = productionPlanningService.getTotalMilkByDateAndCodArt(date,codArt);
        Double totalProducedOrder = productionPlanningService.getTotalProducedOrderByArticleAndDate(codArt,date,date);
        Double productVolume = processedProductService.findByCode(codArt).getAmount();
        //Double totalProducedRepro = productionPlanningService.getTotalProducedReproByArticleAndDate(codArt,date,date);
        Double sngOrder = productionPlanningService.getProductionOrderSNGbyDateAndCodArt(codArt,date);
        Double sngRepro = productionPlanningService.getReproSNGbyDateAndCodArt(codArt,date);


        Double totalProduced = totalProducedOrder; //+ totalProducedRepro;
        Double volume = RoundUtil.getRoundValue((totalProduced * productVolume)/1000,2, RoundUtil.RoundMode.SYMMETRIC);
        Double output = volume * (sngOrder/100);

        this.setVariableValue("volume",volume);
        this.setVariableValue("totalProduced",totalProduced);
        this.setVariableValue("totalMilk",totalMilk);
        this.setVariableValue("sng",sngOrder);
        this.setVariableValue("output",output);

    }


}
