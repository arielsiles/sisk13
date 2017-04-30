package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Bank;
import com.encens.khipus.model.contacts.BankState;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.service.customers.ExtensionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.List;

/**
 * Actions for Bank
 *
 * @version 2.8
 * @author:
 */

@Name("bankAction")
@Scope(ScopeType.CONVERSATION)
public class BankAction extends GenericAction<Bank> {
    @In
    private ExtensionService extensionService;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    @Factory(value = "bank", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BANKENTITY','VIEW')}")
    public Bank initBank() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Factory("bankState")
    public BankState[] getTargetType() {
        return BankState.values();
    }

    public void changeState(BankState state) {
        if (state.equals(BankState.INACTIVE)) {
            getInstance().setCancelDate(new Date());
        }
        getInstance().setStateDate(new Date());
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('BANKENTITY','VIEW')}")
    public String select(Bank instance) {
        String outCome = super.select(instance);
        updateShowExtension();
        return outCome;
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().setExtensionSite(null);
        }
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
    }
}
