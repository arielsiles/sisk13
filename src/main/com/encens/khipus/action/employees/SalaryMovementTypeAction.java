package com.encens.khipus.action.employees;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.SalaryMovementTypeDuplicatedByDefaultException;
import com.encens.khipus.exception.employees.SalaryMovementTypeDuplicatedNameException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.PermissionType;
import com.encens.khipus.model.employees.MovementType;
import com.encens.khipus.model.employees.SalaryMovementType;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.service.employees.SalaryMovementTypeService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@SuppressWarnings({"SeamBijectionTypeMismatchInspection"})
@Name("salaryMovementTypeAction")
@Scope(ScopeType.CONVERSATION)
public class SalaryMovementTypeAction extends GenericAction<SalaryMovementType> {
    @In(value = "org.jboss.seam.security.identity")
    private AppIdentity appIdentity;
    @In
    private SalaryMovementTypeService salaryMovementTypeService;

    @Factory(value = "salaryMovementType", scope = ScopeType.STATELESS)
    public SalaryMovementType initSalaryMovementType() {
        return getInstance();
    }

    public List<MovementType> getMovementTypeByAccessRight() {
        List<MovementType> movementTypeList = new ArrayList<MovementType>();
        if (appIdentity.hasPermission("MOVEMENTTYPEWIN", PermissionType.VIEW.name())) {
            movementTypeList.add(MovementType.WIN);
        }
        if (appIdentity.hasPermission("MOVEMENTTYPEOTHERDISCOUNT", PermissionType.VIEW.name())) {
            movementTypeList.add(MovementType.OTHER_DISCOUNT);
        }
        if (appIdentity.hasPermission("MOVEMENTTYPEOTHERINCOME", PermissionType.VIEW.name())) {
            movementTypeList.add(MovementType.OTHER_INCOME);
        }
        if (appIdentity.hasPermission("MOVEMENTTYPEDISCOUNTOUTOFRETENTION", PermissionType.VIEW.name())) {
            movementTypeList.add(MovementType.DISCOUNT_OUT_OF_RETENTION);
        }
        if (movementTypeList.isEmpty()) {
            movementTypeList = null;
        }
        return movementTypeList;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SALARYMOVEMENTTYPE','VIEW')}")
    public String select(SalaryMovementType instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(salaryMovementTypeService.load(instance));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SALARYMOVEMENTTYPE','CREATE')}")
    public String create() {
        try {
            salaryMovementTypeService.createSalaryMovementType(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (SalaryMovementTypeDuplicatedNameException e) {
            addSalaryMovementTypeDuplicatedNameMessage();
            return Outcome.REDISPLAY;
        } catch (SalaryMovementTypeDuplicatedByDefaultException e) {
            addSalaryMovementTypeDuplicatedByDefaultMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SALARYMOVEMENTTYPE','CREATE')}")
    public void createAndNew() {
        try {
            salaryMovementTypeService.createSalaryMovementType(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (SalaryMovementTypeDuplicatedNameException e) {
            addSalaryMovementTypeDuplicatedNameMessage();
        } catch (SalaryMovementTypeDuplicatedByDefaultException e) {
            addSalaryMovementTypeDuplicatedByDefaultMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SALARYMOVEMENTTYPE','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            salaryMovementTypeService.updateSalaryMovementType(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(salaryMovementTypeService.load(getInstance()));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (SalaryMovementTypeDuplicatedNameException e) {
            addSalaryMovementTypeDuplicatedNameMessage();
            return Outcome.REDISPLAY;
        } catch (SalaryMovementTypeDuplicatedByDefaultException e) {
            addSalaryMovementTypeDuplicatedByDefaultMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SALARYMOVEMENTTYPE','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public void assignCashAccount(CashAccount cashAccount) {
        getInstance().setCashAccount(cashAccount);
    }

    public void clearCashAccount() {
        getInstance().setCashAccount(null);
    }

    protected void addSalaryMovementTypeDuplicatedNameMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SalaryMovementType.error.duplicatedName", getInstance().getName());
    }

    protected void addSalaryMovementTypeDuplicatedByDefaultMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SalaryMovementType.error.byDefault", messages.get(getInstance().getMovementType().getResourceKey()));
    }

    public Boolean getReadOnly() {
        return isManaged() && !MovementType.isAvailableValue(getInstance().getMovementType());
    }
}
