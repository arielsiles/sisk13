package com.encens.khipus.action.finances.reports;

import com.encens.khipus.model.finances.FinancesCurrencyType;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the rotatory fund by account report scriptlet in order to calculate te totals by currency
 *
 * @author
 * @version 2.26.4
 */
public class RotatoryFundByAccountReportScriptlet extends JRDefaultScriptlet {

    public void beforeDetailEval() throws JRScriptletException {
        HashMap<FinancesCurrencyType,Double> totals= (HashMap<FinancesCurrencyType,Double>) this.getVariableValue("totalsMap");
        super.beforeDetailEval();
        Double amount=0.0;
        if(this.getFieldValue("rFCSDistribution.amount")!=null){
            amount=((BigDecimal)this.getFieldValue("rFCSDistribution.amount")).doubleValue();
            FinancesCurrencyType currency= (FinancesCurrencyType) this.getFieldValue("cashAccount.currency");
            if(totals.containsKey(currency)){
                Double value=totals.get(currency);
                totals.put(currency,value+amount);
            }
            else{
                totals.put(currency,amount);
            }
            this.setVariableValue("totalsMap",totals);
        }
    }
    
}
