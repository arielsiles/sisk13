package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.customers.CreditAction;
import com.encens.khipus.action.customers.CreditTransactionAction;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;

import java.math.BigDecimal;
import java.util.Calendar;
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

        Calendar cal = Calendar.getInstance();
        cal.setTime(credit.getFirstPayment());
        cal.add(Calendar.MONTH, credit.getTerm()-1);
        Date expirationDate = cal.getTime();

        Long expiredDays = creditAction.calculateExpiredDays(credit, dateTransaction);

        System.out.println("------------------> expiration Date: " + expirationDate);

        //initialize group values
        this.setVariableValue("interestToDateVar", interestToDate);
        this.setVariableValue("expirationDateVar", expirationDate);
        this.setVariableValue("expiredDaysVar", expiredDays);

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
