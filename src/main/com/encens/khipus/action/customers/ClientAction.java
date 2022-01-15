package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.PaymentMethodSin;
import com.encens.khipus.service.customers.ClientService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

/**
 * @author
 * @version 2.2
 */

@Name("clientAction")
@Scope(ScopeType.CONVERSATION)
public class ClientAction extends GenericAction<Client> {

    @In
    private ClientService clientService;

    private String clientName;
    private Boolean personFlag = Boolean.TRUE;
    private PaymentMethodSin paymentMethodSin;

    private boolean showNitExtension = false;

    @Factory(value = "client", scope = ScopeType.STATELESS)
    public Client initClient() {
        return getInstance();
    }

    public Client getClient() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "fullName";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Client instance) {
        String outCome = super.select(instance);
        setPaymentMethodSin(clientService.findPaymentMethodSin(instance.getPaymentMethodTypeCode()));
        return outCome;
    }

    @End
    @Override
    public String create() {

        getInstance().setCommission(0.0);
        getInstance().setGuarantee(0.0);
        if (getInstance().getPersonFlag())
            getInstance().setPersonType("cliente");
        else
            getInstance().setPersonType("institucion");

        if (getInstance().getName() == null)
            getInstance().setName("");
        if (getInstance().getLastName() == null)
            getInstance().setLastName("");
        if (getInstance().getMaidenName() == null)
            getInstance().setMaidenName("");

        getInstance().setPaymentMethodTypeCode(this.paymentMethodSin.getCode());

        return super.create();

    }

    @End
    @Override
    public String update() {
        getInstance().setPaymentMethodTypeCode(this.paymentMethodSin.getCode());
        return super.update();
    }

    public void updateShowNitExtension() {
        showNitExtension = true;
    }

    public void changePersonFlag(){
        System.out.println("------------> changePersonFlag: " + this.personFlag);
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    public Boolean getPersonFlag() {
        return personFlag;
    }

    public void setPersonFlag(Boolean personFlag) {
        this.personFlag = personFlag;
    }

    public boolean isShowNitExtension() {
        return showNitExtension;
    }

    public void setShowNitExtension(boolean showNitExtension) {
        this.showNitExtension = showNitExtension;
    }

    public boolean hasDocumentTypeCI(){
        boolean result = false;
        if (getInstance().getInvoiceDocumentType() != null)
            if (getInstance().getInvoiceDocumentType().getSinCode() == 1)
                result = true;

        return result;
    }

    public PaymentMethodSin getPaymentMethodSin() {
        return paymentMethodSin;
    }

    public void setPaymentMethodSin(PaymentMethodSin paymentMethodSin) {
        this.paymentMethodSin = paymentMethodSin;
    }
}
