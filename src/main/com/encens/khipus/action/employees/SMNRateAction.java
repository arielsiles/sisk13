package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.SMNRate;
import com.encens.khipus.service.employees.SMNRateService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * SMN rate action class
 *
 * @author
 * @version 2.26
 */
@Name("smnRateAction")
@Scope(ScopeType.CONVERSATION)
public class SMNRateAction extends GenericAction<SMNRate> {

    @In
    private SMNRateService smnRateService;

    private boolean readOnlyActive;

    @Factory(value = "smnRate")
    @Restrict("#{s:hasPermission('SMNRATE','VIEW')}")
    public SMNRate initSMNRate() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(SMNRate smnRateItem) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(SMNRate.class, smnRateItem.getId()));

            initializePageVariables();
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    protected GenericService getService() {
        return smnRateService;
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
