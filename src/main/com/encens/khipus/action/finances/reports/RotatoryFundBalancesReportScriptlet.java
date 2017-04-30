package com.encens.khipus.action.finances.reports;

import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.reports.ReportDesignHelper;
import com.encens.khipus.service.finances.DiscountCommentService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Scriptlet to calculate summary report
 *
 * @author
 * @version $Id: RotatoryFundBalancesReportScriptlet.java  26-oct-2010 19:18:34$
 */
public class RotatoryFundBalancesReportScriptlet extends JRDefaultScriptlet {
    private DiscountCommentService discountCommentService = (DiscountCommentService) Component.getInstance("discountCommentService");
    private Log log = Logging.getLog(RotatoryFundBalancesReportScriptlet.class);

    @Override
    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        FinancesCurrencyType payCurrency = (FinancesCurrencyType) getFieldValue("rotatoryFund.payCurrency");
        BigDecimal amount = getFieldAsBigDecimal("rotatoryFund.amount");
        BigDecimal payableResidue = getFieldAsBigDecimal("rotatoryFund.payableResidue");
        BigDecimal receivableResidue = getFieldAsBigDecimal("rotatoryFund.receivableResidue");

        sumAndSetFieldValueAsCurrency("amountTotalMapVar", amount, payCurrency);
        sumAndSetFieldValueAsCurrency("payableResidueTotalMapVar", payableResidue, payCurrency);
        sumAndSetFieldValueAsCurrency("receivableResidueTotalMapVar", receivableResidue, payCurrency);

        Long id = (Long) getFieldValue("rotatoryFund.id");
        List<Object[]> discountCommentCauseList = discountCommentService.findCauseByRotatoryFundId(id);
        this.setVariableValue("cause", concatLineSeparated(discountCommentCauseList));
    }

    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }

    /**
     * Sum and set in totalizerMapVar the field value for this currency
     *
     * @param totalizerMapVar
     * @param fieldValue
     * @param currencyType
     * @throws JRScriptletException
     */
    private void sumAndSetFieldValueAsCurrency(String totalizerMapVar, BigDecimal fieldValue, FinancesCurrencyType currencyType) throws JRScriptletException {
        Map<String, BigDecimal> totalMapVar = (Map<String, BigDecimal>) this.getVariableValue(totalizerMapVar);
        if (totalMapVar == null) {
            totalMapVar = new HashMap<String, BigDecimal>();
        }

        totalMapVar = ReportDesignHelper.sumBigDecimalCurrencyMap(currencyType.getSymbolResourceKey(), fieldValue, totalMapVar);

        this.setVariableValue(totalizerMapVar, totalMapVar);
    }

    /**
     * Concat a List of Object[] of Date and Strings with internal space separation
     * and \n\n separation from each list element
     *
     * @param discountCommentCauseList a list of Strings
     * @return a String concatenated and separated by \n\n
     */
    @SuppressWarnings({"unchecked"})
    public static String concatLineSeparated(List<Object[]> discountCommentCauseList) {
        String result = "";
        for (Object[] discountComment : discountCommentCauseList) {
            result += FormatUtils.concat(DateUtils.format((Date) discountComment[1], MessageUtils.getMessage("patterns.date")), (String) discountComment[0]) + "\n\n";
        }
        if (discountCommentCauseList.size() > 0) {
            result = result.substring(0, result.lastIndexOf("\n\n"));
        }
        return result;
    }
}
