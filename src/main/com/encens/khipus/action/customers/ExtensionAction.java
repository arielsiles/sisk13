package com.encens.khipus.action.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.service.customers.ExtensionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Extension
 *
 * @author
 * @version 2.7
 */
@Name("extensionAction")
@Scope(ScopeType.CONVERSATION)
public class ExtensionAction extends GenericAction<Extension> {
    @In(value = "documentTypeAction")
    private DocumentTypeAction documentTypeAction;

    @In
    private ExtensionService extensionService;


    @Factory(value = "extension", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('EXTENSION','VIEW')}")
    public Extension initExtension() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "extension";
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('EXTENSION','CREATE')}")
    public String create() {
        return super.create();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('EXTENSION','VIEW')}")
    public String select(Extension instance) {
        try {
            setOp(OP_UPDATE);
            /*refresh the instance from database*/
            setInstance(extensionService.findExtension(instance.getId()));
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('EXTENSION','UPDATE')}")
    public String update() {
        try {
            extensionService.updateDocumentType(getInstance());
            addUpdatedMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                extensionService.findExtension(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }

        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('EXTENSION','DELETE')}")
    public String delete() {
        try {
            extensionService.deleteDocumentType(getInstance());
            addDeletedMessage();
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    public String addExtension() {
        /* to create the new Extension instance*/
        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setDocumentType(documentTypeAction.getInstance());
        return Outcome.SUCCESS;
    }
}