package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.customers.CreditAction;
import com.encens.khipus.action.customers.CreditTransactionAction;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.util.BigDecimalUtil;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Encens S.R.L.
 * Scriptlet to calculate values to kardex report
 *
 * @author
 * @version $Id: KardexReportScriptlet.java  16-abr-2010 17:36:48$
 */
public class CreditStatusZoneReportScriptlet extends JRDefaultScriptlet {

    private CreditService creditService = (CreditService) Component.getInstance("creditService");

    private CreditTransactionAction creditTransactionAction = (CreditTransactionAction) Component.getInstance("creditTransactionAction");
    private CreditAction creditAction = (CreditAction) Component.getInstance("creditAction");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        super.beforeGroupInit(s);
        String purchaseOrderGroupName = "purchaseOrderGroup";

        if (s.equals(purchaseOrderGroupName)) {
        }
    }


    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Date dateTransaction = (Date) getParameterValue("dateTransaction", false);
        Long creditId = (Long) getFieldValue("creditId");
        Credit credit = creditService.findCreditById(creditId);

        creditAction.setInstance(credit);
        creditTransactionAction.setDateTransaction(dateTransaction);
        BigDecimal interestToDate = creditTransactionAction.calculateInterest();
        BigDecimal criminalInterestValue = creditTransactionAction.getCriminalInterestValue();
        interestToDate = BigDecimalUtil.sum(interestToDate, criminalInterestValue);
        Long expiredDays = creditAction.calculateExpiredDays(credit, dateTransaction);

        //BigDecimal quota = credit.getQuota(); /* Revision */
        BigDecimal quota = creditTransactionAction.calculateCapitalBaseCapital(credit); /* Revision */

        /** Sumando al interes la cuota diferida si existe **/
        BigDecimal deferredQuotaValue = calculateDeferredQuota(credit, quota);
        interestToDate = BigDecimalUtil.sum(interestToDate, deferredQuotaValue);

        if (credit.getNumberQuota() == 1)
            quota = BigDecimal.ZERO;
        if (quota.compareTo(credit.getCapitalBalance()) > 0)
            quota = credit.getCapitalBalance();

        //initialize group values
        this.setVariableValue("interestToDateVar", interestToDate);
        this.setVariableValue("expiredDaysVar", expiredDays);
        this.setVariableValue("quotaVar", quota);

    }

    private BigDecimal calculateDeferredQuota(Credit credit, BigDecimal capitalValue){

        BigDecimal deferredQuotaValue = BigDecimal.ZERO;
        if (credit.getDeferredAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal quotaParam = BigDecimalUtil.divide(capitalValue, credit.getQuota());
            deferredQuotaValue = BigDecimalUtil.multiply(credit.getDeferredQuota(), quotaParam);
        }
        return deferredQuotaValue;
    }

    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }

    private BigDecimal getVariableAsBigDecimal(String varName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getVariableValue(varName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }
}
