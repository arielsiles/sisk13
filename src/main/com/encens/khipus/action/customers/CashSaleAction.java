package com.encens.khipus.action.customers;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CashSale;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;

/**
 * @author
 * @version 2.2
 */

@Name("cashSaleAction")
@Scope(ScopeType.CONVERSATION)
public class CashSaleAction extends GenericAction<CashSale> {

    private String gloss = "TRASPASO DE FONDOS CAJA, VENTA DE PRODUCTOS VETERINARIOS A CAJA DE AHORROS";
    private Date startDate;
    private Date endDate;
    private Double transferAmount;

    @In
    private VoucherAccoutingService voucherAccoutingService;


    @Factory(value = "cashSale", scope = ScopeType.STATELESS)
    public CashSale initCashSale() {
        return getInstance();
    }


    @End
    public String generateTransfer(){

        String start = DateUtils.format(startDate, "dd/MM/yyyy");
        String end   = DateUtils.format(endDate, "dd/MM/yyyy");

        if (startDate.equals(endDate))
            gloss = gloss + " DEL " + start;
        else
            gloss = gloss + " DEL " + start + " - " + end;

        try {
            if (transferAmount > 0){
                voucherAccoutingService.generateCashTransferAccountEntry(startDate, endDate, getTransferAmount(), gloss);
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"CashSale.message.transfer", transferAmount);
            }else {
                setTransferAmount(0.0);
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"CashSale.message.amountZero");
            }

        }catch (CompanyConfigurationNotFoundException e){
            e.printStackTrace();
        }

        setTransferAmount(0.0);
        return Outcome.SUCCESS;
    }

    public void calculateTransfer(){

        setTransferAmount(voucherAccoutingService.calculateCashTransferAmount(startDate, endDate));
        if (transferAmount == 0){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"CashSale.message.amountZero");
        }
    }


    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Double transferAmount) {
        this.transferAmount = transferAmount;
    }
}
