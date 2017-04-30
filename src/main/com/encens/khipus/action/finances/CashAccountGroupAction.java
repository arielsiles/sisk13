package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccountGroup;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.international.StatusMessage;

/**
 * CashAccountGroupAction
 *
 * @author
 * @version 2.27
 */
@Name("cashAccountGroupAction")
@Scope(ScopeType.CONVERSATION)
public class CashAccountGroupAction extends GenericAction<CashAccountGroup> {

    @In(create = true)
    private EntityQuery countCashAccountGroupCodeQuery;
    @In(create = true)
    private EntityQuery countCashAccountGroupNameQuery;
    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @Factory(value = "cashAccountGroup", scope = ScopeType.STATELESS)
    public CashAccountGroup initCashAccountGroup() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('CASHACCOUNTGROUP','VIEW')}")
    public String select(CashAccountGroup instance) {
        return super.select(instance);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CASHACCOUNTGROUP','CREATE')}")
    public String create() {
        if (!validate()) {
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        insertCode();
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('CASHACCOUNTGROUP','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            insertCode();
            super.createAndNew();
        }
    }


    @Override
    @End
    @Restrict("#{s:hasPermission('CASHACCOUNTGROUP','UPDATE')}")
    public String update() {
        if (!validate()) {
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CASHACCOUNTGROUP','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public Boolean validate() {
        Boolean valid = true;
        if (((Long) countCashAccountGroupNameQuery.getSingleResult()) > 0) {
            valid = false;
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "CashAccountGroup.error.duplicatedName", getInstance().getName());
        }
        return valid;
    }

    public void insertCode() {
        do {
            getInstance().setCode(sequenceGeneratorService.nextValue(Constants.CASHACCOUNTGROUP_CODE_SEQUENCE));
        } while (((Long) countCashAccountGroupCodeQuery.getSingleResult()) > 0);
    }
}
