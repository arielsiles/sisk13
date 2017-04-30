package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.IVARate;
import com.encens.khipus.service.employees.IVARateService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * IVA rate action class
 *
 * @author
 * @version 2.26
 */
@Name("ivaRateAction")
@Scope(ScopeType.CONVERSATION)
public class IVARateAction extends GenericAction<IVARate> {

    @In
    private IVARateService ivaRateService;

    private boolean readOnlyActive;

    @Factory(value = "ivaRate")
    @Restrict("#{s:hasPermission('IVARATE','VIEW')}")
    public IVARate initIVARate() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(IVARate ivaRateItem) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(IVARate.class, ivaRateItem.getId()));

            initializePageVariables();
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    protected GenericService getService() {
        return ivaRateService;
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
