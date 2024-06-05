package com.encens.khipus.action.finances.reports;

import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.model.finances.ExpenseType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class CashAccountReportScriptlet extends JRDefaultScriptlet {

    public void beforeDetailEval() throws JRScriptletException {

        super.beforeDetailEval();

        CashAccountType cashAccountType = (CashAccountType) getFieldValue("type");
        FinancesCurrencyType currency   = (FinancesCurrencyType) getFieldValue("currency");
        Boolean active                  = (Boolean) getFieldValue("active");
        Boolean hasPermission           = (Boolean) getFieldValue("permission");
        ExpenseType expenseType         = (ExpenseType) getFieldValue("expenseType");

        String currencyVar = "";
        if (currency.equals(FinancesCurrencyType.P))
            currencyVar = "B";
        if (currency.equals(FinancesCurrencyType.D))
            currencyVar = "D";

        String permissionVar = "N";
        if (hasPermission != null && hasPermission)
            permissionVar = "S";

        this.setVariableValue("typeVar", cashAccountType.toString());
        this.setVariableValue("currencyVar", currencyVar);
        this.setVariableValue("activeVar", active ? "S" : "N");
        this.setVariableValue("permissionVar", permissionVar);
        this.setVariableValue("expenseTypeVar", expenseType != null ? expenseType.toString() : "");



    }

}
