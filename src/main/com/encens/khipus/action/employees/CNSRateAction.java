package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.CNSRate;
import com.encens.khipus.service.employees.CNSRateService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * CNS rate action class
 *
 * @author
 * @version 2.26
 */
@Name("cnsRateAction")
@Scope(ScopeType.CONVERSATION)
public class CNSRateAction extends GenericAction<CNSRate> {

    @In
    private CNSRateService cnsRateService;

    private boolean readOnlyActive;

    @Factory(value = "cnsRate")
    @Restrict("#{s:hasPermission('CNSRATE','VIEW')}")
    public CNSRate initCNSRate() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(CNSRate cnsRateItem) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(CNSRate.class, cnsRateItem.getId()));

            initializePageVariables();
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    protected GenericService getService() {
        return cnsRateService;
    }

    @End
    public String delete() {
        if (getInstance().getActive()) {
            log.debug("entity cannot be deleted because is the active rate");
            addDeleteActiveMessage();

            return Outcome.FAIL;
        } else {
            return super.delete();
        }
    }

    protected void addDeleteActiveMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.message.activeObject.delete", messages.get("Common.info.item"));
    }

    public boolean getReadOnlyActive() {
        return readOnlyActive;
    }

    public void initializePageVariables() {
        if (isManaged()) {
            readOnlyActive = getInstance().getActive();
        }
    }
}
