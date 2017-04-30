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
 * Encens S.R.L.
 * Scriptlet to calculate summary retrieve debtor report
 *
 * @author
 * @version $Id: RetrieveByDebtorReportScriptlet.java  13-sep-2010 12:56:45$
 */
public class RetrieveByDebtorReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(RetrieveByDebtorReportScriptlet.class);

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Map<String, BigDecimal> totalMapVar = (Map<String, BigDecimal>) this.getVariableValue("totalMapVar");

        if (totalMapVar == null) {
            totalMapVar = new HashMap<String, BigDecimal>();
        }

        BigDecimal collectionAmount = getFieldAsBigDecimal("rotatoryFundCollection.collectionAmount");
        FinancesCurrencyType collectionCurrency = (FinancesCurrencyType) getFieldValue("rotatoryFundCollection.collectionCurrency");

        totalMapVar = ReportDesignHelper.sumBigDecimalCurrencyMap(collectionCurrency.getSymbolResourceKey(), collectionAmount, totalMapVar);

        this.setVariableValue("totalMapVar", totalMapVar);
    }

    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }
}
