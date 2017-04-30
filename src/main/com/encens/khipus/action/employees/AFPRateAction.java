package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.AFPRate;
import com.encens.khipus.service.employees.AFPRateService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * AFP rate action class
 *
 * @author
 * @version 2.26
 */
@Name("afpRateAction")
@Scope(ScopeType.CONVERSATION)
public class AFPRateAction extends GenericAction<AFPRate> {

    @In
    private AFPRateService afpRateService;

    private boolean readOnlyActive;

    @Factory(value = "afpRate")
    @Restrict("#{s:hasPermission('AFPRATE','VIEW')}")
    public AFPRate initAFPRate() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(AFPRate afpRateItem) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(AFPRate.class, afpRateItem.getId()));

            initializePageVariables();
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    protected GenericService getService() {
        return afpRateService;
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
