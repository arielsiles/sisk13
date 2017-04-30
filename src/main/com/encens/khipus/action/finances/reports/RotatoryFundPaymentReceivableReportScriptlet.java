package com.encens.khipus.action.finances.reports;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.RotatoryFundPayment;
import com.encens.khipus.model.finances.RotatoryFundPaymentState;
import com.encens.khipus.reports.ReportDesignHelper;
import com.encens.khipus.service.finances.RotatoryFundPaymentService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Encens S.R.L.
 * Scriptlet to calculate summary report
 *
 * @author
 * @version $Id: RotatoryFundPaymentReceivableReportScriptlet.java  27-oct-2010 17:27:55$
 */
public class RotatoryFundPaymentReceivableReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(RotatoryFundPaymentReceivableReportScriptlet.class);
    private RotatoryFundPaymentService rotatoryFundPaymentService;

    public RotatoryFundPaymentReceivableReportScriptlet() {
        rotatoryFundPaymentService = (RotatoryFundPaymentService) Component.getInstance("rotatoryFundPaymentService");
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Long rotatoryFundId = getFieldAsLong("rotatoryFund.id");
        FinancesCurrencyType payCurrency = (FinancesCurrencyType) getFieldValue("rotatoryFund.payCurrency");
        BigDecimal amount = getFieldAsBigDecimal("rotatoryFund.amount");
        BigDecimal receivableResidue = getFieldAsBigDecimal("rotatoryFund.receivableResidue");
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        //add in Variables
        sumAndSetFieldValueAsCurrency("amountTotalMapVar", amount, payCurrency);
        sumAndSetFieldValueAsCurrency("receivableResidueTotalMapVar", receivableResidue, payCurrency);

        this.setVariableValue("paymentDatesVar", composeRotatoryFundPaymentDates(rotatoryFundId, locale));
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
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
     * Compose rotatory fund payment dates
     * @param rotatoryFundId
     * @param locale
     * @return String
     */
    private String composeRotatoryFundPaymentDates(Long rotatoryFundId, Locale locale) {
        String paymentDates = "";
        RotatoryFund rotatoryFund = null;
        try {
            rotatoryFund = rotatoryFundPaymentService.findById(RotatoryFund.class, rotatoryFundId);
        } catch (EntryNotFoundException e) {
            log.debug("Error in found rotatory fund.. " + rotatoryFundId);
        }

        if (rotatoryFund != null) {
            List<RotatoryFundPayment> rotatoryFundPaymentList = rotatoryFundPaymentService.getRotatoryFundPaymentListByState(rotatoryFund, RotatoryFundPaymentState.APR);

            for (int i = 0; i < rotatoryFundPaymentList.size(); i++) {
                RotatoryFundPayment rotatoryFundPayment = rotatoryFundPaymentList.get(i);

                if (rotatoryFundPayment.getPaymentDate() != null) {
                    paymentDates += DateUtils.format(rotatoryFundPayment.getPaymentDate(), MessageUtils.getMessage("patterns.date"));
                }
                paymentDates += " = ";

                if (rotatoryFundPayment.getPaymentAmount() != null) {
                    paymentDates += FormatUtils.formatNumber(rotatoryFundPayment.getPaymentAmount(), MessageUtils.getMessage("patterns.decimalNumber"), locale != null ? locale : Locale.getDefault());
                    paymentDates += " " + MessageUtils.getMessage(rotatoryFundPayment.getPaymentCurrency().getSymbolResourceKey());
                }

                if (i < rotatoryFundPaymentList.size()) {
                    paymentDates += "\n";
                }
            }
        }
        return paymentDates;
    }
}
