package com.encens.khipus.action.customers;

import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.model.customers.BranchOffice;
import com.encens.khipus.model.customers.CancellationReason;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.IOException;

@Name("invoiceCancelAction")
@Scope(ScopeType.CONVERSATION)
public class InvoiceCancelAction {

    private BranchOffice branchOffice;
    private CancellationReason cancellationReason;
    private String cuf;
    private String resultMessage;

    @In(create = true)
    private BillControllerAction billControllerAction;

    public void cancelDirectInvoice(){

        setResultMessage("Procesando anulación...");
        try {
            String resultMessage = billControllerAction.directInvoiceCancel(branchOffice, cancellationReason, cuf);
            setResultMessage(resultMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanData() {
        setBranchOffice(null);
        setCancellationReason(null);
        setCuf(null);
        setResultMessage(null);
    }

    public BranchOffice getBranchOffice() {
        return branchOffice;
    }

    public void setBranchOffice(BranchOffice branchOffice) {
        this.branchOffice = branchOffice;
    }

    public String getCuf() {
        return cuf;
    }

    public void setCuf(String cuf) {
        this.cuf = cuf;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
