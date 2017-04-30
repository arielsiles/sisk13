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
public class ProductsProducedReportScriptlet extends JRDefaultScriptlet {

    private ProductionPlanningService productionPlanningService = (ProductionPlanningService) Component.getInstance("productionPlanningService");
    private ProductsProducedReportAction productsProducedReportAction = (ProductsProducedReportAction) Component.getInstance("productsProducedReportAction");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String codArt = (String) this.getFieldValue("productItemCode");
        Date startDate = productsProducedReportAction.getStartDate();
        Date endDate = productsProducedReportAction.getEndDate();

        Double totalProducedOrder = productionPlanningService.getTotalProducedOrderByArticleAndDate(codArt,startDate,endDate);
        Double totalProducedRepro = productionPlanningService.getTotalProducedReproByArticleAndDate(codArt,startDate,endDate);
        this.setVariableValue("totalOrder", totalProducedOrder);
        this.setVariableValue("totalRepro", totalProducedRepro);
        this.setVariableValue("total", totalProducedOrder + totalProducedRepro);

    }


}
