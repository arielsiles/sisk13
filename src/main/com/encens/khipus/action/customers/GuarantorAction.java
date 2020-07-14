package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.Guarantor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.2
 */

@Name("guarantorAction")
@Scope(ScopeType.CONVERSATION)
public class GuarantorAction extends GenericAction<Guarantor> {


    @Factory(value = "guarantor", scope = ScopeType.STATELESS)
    public Guarantor initCashSale() {
        return getInstance();
    }


}
