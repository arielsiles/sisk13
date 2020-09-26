package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.customers.CreditAction;
import com.encens.khipus.action.customers.CreditTransactionAction;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.service.customers.CreditService;
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
 * @version $Id: PayrollGuarantorZoneReportScriptlet.java  16-abr-2010 17:36:48$
 */
public class PayrollGuarantorZoneReportScriptlet extends JRDefaultScriptlet {

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

        System.out.println("---------------------> PayrollGuarantorZoneReportScriptlet : YES............");

        Date dateTransaction = (Date) getParameterValue("dateTransaction", false);
        Long creditId = (Long) getFieldValue("creditId");
        Credit credit = creditService.findCreditById(creditId);

        creditAction.setInstance(credit);
        creditTransactionAction.setDateTransaction(dateTransaction);

        String guarantors = creditService.findCreditGuarantors(credit);


        System.out.println("................SET variable guarantors: " + guarantors);

        //initialize group values
        this.setVariableValue("guarantors", guarantors);

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
