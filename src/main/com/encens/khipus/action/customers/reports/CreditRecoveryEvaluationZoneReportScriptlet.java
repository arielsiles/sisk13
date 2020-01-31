package com.encens.khipus.action.customers.reports;

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
 * @version $Id: KardexReportScriptlet.java  16-abr-2010 17:36:48$
 */
public class CreditRecoveryEvaluationZoneReportScriptlet extends JRDefaultScriptlet {

    private CreditService creditService = (CreditService) Component.getInstance("creditService");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        super.beforeGroupInit(s);
        String purchaseOrderGroupName = "purchaseOrderGroup";

        if (s.equals(purchaseOrderGroupName)) {}
    }


    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Long productiveZoneId = (Long) getFieldValue("id");
        Date startDate = (Date) getParameterValue("startDate", false);
        Date endDate   = (Date) getParameterValue("endDate", false);

        //List<Object[]> newCredits = creditService.getAmountNewCredits(productiveZoneId, startDate, endDate); // (Cant, Monto)
        //Object[] obj = newCredits.get(0);

        //System.out.println("-----> Cant: " + obj[1] + " - Monto: " + obj[2]);
        System.out.println("-----> productiveZoneId: " + productiveZoneId);

        /*this.setVariableValue("newCredits", Integer.getInteger(obj[1].toString()));
        this.setVariableValue("amountNewCredits", BigDecimalUtil.toBigDecimal(obj[2]));*/

        this.setVariableValue("newCredits", productiveZoneId.intValue());
        this.setVariableValue("amountNewCredits", BigDecimal.ONE);

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
