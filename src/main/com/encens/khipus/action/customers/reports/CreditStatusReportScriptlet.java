package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.customers.CreditAction;
import com.encens.khipus.action.customers.CreditTransactionAction;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.util.DateUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Encens S.R.L.
 * Scriptlet to calculate values to kardex report
 *
 * @author
 * @version $Id: CreditStatusReportScriptlet.java  23-oct-2019 17:36:48$
 */
public class CreditStatusReportScriptlet extends JRDefaultScriptlet {

    private CreditService creditService = (CreditService) Component.getInstance("creditService");
    private CreditTransactionService creditTransactionService = (CreditTransactionService) Component.getInstance("creditTransactionService");

    private CreditTransactionAction creditTransactionAction = (CreditTransactionAction) Component.getInstance("creditTransactionAction");
    private CreditAction creditAction = (CreditAction) Component.getInstance("creditAction");

    private CreditReportAction creditReportAction = (CreditReportAction) Component.getInstance("creditReportAction");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        super.beforeGroupInit(s);
        String purchaseOrderGroupName = "purchaseOrderGroup";

        if (s.equals(purchaseOrderGroupName)) {
        }
    }


    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Date endPeriodDate = (Date) getParameterValue("endPeriodDate", false);
        Long creditId = (Long) getFieldValue("creditId");
        CreditState state = (CreditState) getFieldValue("state");
        Credit credit = creditService.findCreditById(creditId);
        Date lastPaymentBefore = creditTransactionService.findLastPaymentBeforeDate(credit, endPeriodDate);

        creditReportAction.calculatePaymentPlan(credit);

        BigDecimal capitalBalance = (BigDecimal) getFieldValue("capitalBalance");
        Date lastPaymentDate = (Date) getFieldValue("lastPayment");
        Date expirationDate = (Date) getFieldValue("expirationDate");

        System.out.println("====> " + credit.getPartner().getFullName() + " <====");

        creditAction.setInstance(credit);
        creditTransactionAction.setDateTransaction(endPeriodDate);

        Long days = new Long(0);
        if (state.equals(CreditState.VIG))
            days = DateUtils.differenceBetween(lastPaymentDate, endPeriodDate, TimeUnit.DAYS) - 1;

        BigDecimal interestToDate = BigDecimal.ZERO;
        Date nextPaymentDate = endPeriodDate;
        Long expiredDays = new Long(0);
        if (capitalBalance.doubleValue() > 0) {
            if (state.equals(CreditState.VIG))
                interestToDate = creditTransactionAction.calculateInterest(capitalBalance, lastPaymentDate, endPeriodDate, credit.getAnnualRate());

            nextPaymentDate = creditAction.findDateOfNextPayment(credit, endPeriodDate);
            expiredDays = creditAction.calculateExpiredDays(credit, endPeriodDate);

            if (expiredDays <= 0)
                expiredDays = BigDecimal.ZERO.longValue();

        }
        System.out.println("===> nextPaymentDate: " + DateUtils.format(nextPaymentDate, "dd/MM/yyyy") + " <===");
        System.out.println("--------------------------> " + credit.getPartner().getFullName() + " - " + interestToDate + " - Days: " + days + " - ExpiredDays: " + expiredDays);
        //System.out.println("------------------> expiration Date: " + expirationDate);

        //initialize group values
        this.setVariableValue("interestToDateVar", interestToDate);
        this.setVariableValue("days", days);
        this.setVariableValue("expiredDays", expiredDays);
        this.setVariableValue("lastPaymentBefore", lastPaymentBefore);
        //this.setVariableValue("expiredDaysVar", expiredDays);

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
