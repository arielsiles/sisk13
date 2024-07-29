package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.model.finances.CashAccountType;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class CashInflowsReportScriptlet extends JRDefaultScriptlet {

    public void beforeDetailEval() throws JRScriptletException {

        super.beforeDetailEval();

        CashAccountType cashAccountType = (CashAccountType) getFieldValue("accountType");

        this.setVariableValue("accountTypeVar", cashAccountType.toString());

    }

}
