package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.util.Date;

public class SummaryClientReportScriptlet extends JRDefaultScriptlet {

    //private VoucherAccoutingService voucherAccoutingService = (VoucherAccoutingService) Component.getInstance("voucherAccoutingService");
    private ClientService clientService = (ClientService) Component.getInstance("clientService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Long clientId = (Long) getFieldValue("id");
        Date startDate = (Date) getParameterValue("startDate");
        String cashAccountCode = (String) getParameterValue("cashAccountCode");

        Double balance = clientService.getBalanceClient(startDate, cashAccountCode, clientId);

        this.setVariableValue("balance", balance);
    }

}