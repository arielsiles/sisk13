package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.service.warehouse.ProductItemReportService;
import com.encens.khipus.util.Constants;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * Scriptlet to calculate values to product item cost unit report   
 *
 * @author
 * @version $Id: ProductItemCostUnitReportScriptlet.java  11-mar-2010 19:26:24$
 */
public class ProductItemCostUnitReportScriptlet extends JRDefaultScriptlet {

    ProductItemReportService productItemReportService = (ProductItemReportService) Component.getInstance("productItemReportService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String productItemCode = (String) getFieldValue("productItem.id.productItemCode");
        this.setVariableValue("unitaryBalanceVar", sumProductItemUnitaryBalanceInventory(productItemCode));
    }

    private BigDecimal sumProductItemUnitaryBalanceInventory(String productItemCode) {
        BigDecimal sumUnitaryBalance = productItemReportService.sumProductItemUnitaryBalanceInventory(productItemCode, Constants.defaultCompanyNumber);
        return sumUnitaryBalance != null ? sumUnitaryBalance : BigDecimal.ZERO;
    }
}
