package com.encens.khipus.action.finances.reports;

import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.reports.ReportDesignHelper;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 2.28
 */
public class RotatoryFundCollectionDetailReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(RotatoryFundCollectionDetailReportScriptlet.class);

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Map<String, BigDecimal> totalMapVar = (Map<String, BigDecimal>) this.getVariableValue("totalMapVar");

        if (totalMapVar == null) {
            totalMapVar = new HashMap<String, BigDecimal>();
        }

        BigDecimal amount = ReportDesignHelper.getFieldAsBigDecimal(this, "spendDistribution.amount");
        FinancesCurrencyType sourceCurrency = (FinancesCurrencyType) getFieldValue("rotatoryFundCollection.sourceCurrency");
        totalMapVar = ReportDesignHelper.sumBigDecimalCurrencyMap(sourceCurrency.getSymbolResourceKey(), amount, totalMapVar);
        this.setVariableValue("totalMapVar", totalMapVar);
    }

}

