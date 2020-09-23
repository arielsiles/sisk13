package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Guarantor;
import com.encens.khipus.model.customers.GuarantorDocument;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("guarantorAction")
@Scope(ScopeType.CONVERSATION)
public class GuarantorAction extends GenericAction<Guarantor> {

    private List<GuarantorDocument> guarantorDocumentList = new ArrayList<GuarantorDocument>();

    @Factory(value = "guarantor", scope = ScopeType.STATELESS)
    public Guarantor initCashSale() {
        return getInstance();
    }

    public void assignPerson(Person person) {
        getInstance().setPerson(person);
    }


    public void assignGuarantorDocument(){

        GuarantorDocument guarantorDocument = new GuarantorDocument();

        guarantorDocumentList.add(guarantorDocument);

    }

    public List<GuarantorDocument> getGuarantorDocumentList() {
        return guarantorDocumentList;
    }

    public void setGuarantorDocumentList(List<GuarantorDocument> guarantorDocumentList) {
        this.guarantorDocumentList = guarantorDocumentList;
    }
}
