package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.Guarantor;
import com.encens.khipus.model.customers.GuarantorDocument;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.service.customers.GuarantorService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("guarantorAction")
@Scope(ScopeType.CONVERSATION)
public class GuarantorAction extends GenericAction<Guarantor> {

    @In
    private GuarantorService guarantorService;

    private List<GuarantorDocument> guarantorDocumentList = new ArrayList<GuarantorDocument>();

    @Factory(value = "guarantor", scope = ScopeType.STATELESS)
    public Guarantor initCashSale() {
        return getInstance();
    }

    public void assignPartner(Partner partner) {
        getInstance().setPartner(partner);
    }


    public void assignGuarantorDocument(){

        GuarantorDocument guarantorDocument = new GuarantorDocument();

        guarantorDocumentList.add(guarantorDocument);

    }

    public void removeInstance(GuarantorDocument instance) {
        guarantorDocumentList.remove(instance);
        //selectedProductItems.remove(instance.getProductItem().getId());
    }

    @End(beforeRedirect = true)
    public String createGuarantor(Credit credit){
        Guarantor guarantor = getInstance();

        for (GuarantorDocument guarantorDocument : guarantorDocumentList){
            guarantor.getGuarantorDocumentList().add(guarantorDocument);
            guarantorDocument.setGuarantor(guarantor);
        }

        guarantor.setCredit(credit);
        guarantorService.createCreditGuarantor(guarantor);

        return Outcome.SUCCESS;
    }

    public Integer getRows() {
        if (null != guarantorDocumentList && !guarantorDocumentList.isEmpty()) {
            return guarantorDocumentList.size() + 1;
        }

        return 1;
    }

    public List<GuarantorDocument> getGuarantorDocumentList() {
        return guarantorDocumentList;
    }

    public void setGuarantorDocumentList(List<GuarantorDocument> guarantorDocumentList) {
        this.guarantorDocumentList = guarantorDocumentList;
    }
}
