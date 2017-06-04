package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.service.accouting.VoucherAccoutingService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.util.Date;

public class SummaryProviderReportScriptlet extends JRDefaultScriptlet {

    //private MovementDetailService movementDetailService = (MovementDetailService) Component.getInstance("movementDetailService");
    private VoucherAccoutingService voucherAccoutingService = (VoucherAccoutingService) Component.getInstance("voucherAccoutingService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String providerCode = (String) getFieldValue("providerCode");
        Date startDate = (Date) getParameterValue("startDate");
        String cashAccount = (String) getParameterValue("cashAccount");
        String cashAccountCode = (String) getParameterValue("cashAccountCode");
        Double balance = voucherAccoutingService.getBalanceProvider(startDate, cashAccountCode, providerCode);

        this.setVariableValue("balance", balance);
    }

}