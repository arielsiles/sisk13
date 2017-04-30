package com.encens.khipus.action.employees;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.action.common.FunctionAction;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.PermissionType;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.GestionPayrollService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Actions for SalaryMovement
 *
 * @author
 */

@Name("salaryMovementAction")
@Scope(ScopeType.CONVERSATION)
public class SalaryMovementAction extends GenericAction<SalaryMovement> {

    @In
    private GestionPayrollService gestionPayrollService;
    @In(create = true)
    private FunctionAction functionAction;
    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean idSalaryMovementReadOnly;

    @In(value = "org.jboss.seam.security.identity")
    private AppIdentity appIdentity;

    @Factory(value = "salaryMovement", scope = ScopeType.STATELESS)
    public SalaryMovement initSalaryMovement() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "amount";
    }

    public void assingEmployee(Employee employee) {
        getInstance().setEmployee(employee);
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearEmployee() {
        getInstance().setEmployee(null);
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SALARYMOVEMENT','VIEW')}")
    public String select(SalaryMovement instance) {
        return super.select(instance);
    }

    @End
    @Restrict("#{s:hasPermission('SALARYMOVEMENT','CREATE')}")
    public String create() {
        if (getInstance().getEmployee() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", messages.get("SalaryMovement.employee"));
            return Outcome.REDISPLAY;
        }

        if (!validateHasOfficialPayroll()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('SALARYMOVEMENT','CREATE')}")
    public void createAndNew() {
        if (getInstance().getEmployee() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", messages.get("SalaryMovement.employee"));
        } else if (validateHasOfficialPayroll()) {
            GestionPayroll gestionPayroll = getInstance().getGestionPayroll();
            super.createAndNew();
            if (!functionAction.getHasSeverityErrorMessages()) {
                try {
                    getInstance().setGestionPayroll(getService().findById(GestionPayroll.class, gestionPayroll.getId()));
                } catch (EntryNotFoundException ignored) {
                }
            }
        }
    }

    @End
    @Restrict("#{s:hasPermission('SALARYMOVEMENT','UPDATE')}")
    public String update() {
        if (getInstance().getEmployee() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", messages.get("SalaryMovement.employee"));
            return Outcome.REDISPLAY;
        }
        if (!validateHasOfficialPayroll()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SALARYMOVEMENT','DELETE')}")
    public String delete() {
        if (!validateHasOfficialPayroll()) {
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    public void assignGestionPayroll(GestionPayroll gestionPayroll) {
        getInstance().setGestionPayroll(gestionPayrollService.findGestionPayrollById(gestionPayroll.getId()));
        clearEmployee();
    }

    @SuppressWarnings({"NullableProblems"})
    public void cleanGestionPayroll() {
        getInstance().setGestionPayroll(null);
    }

    public Boolean getReadOnly() {
        if (idSalaryMovementReadOnly == null) {
            idSalaryMovementReadOnly = isManaged() && (!MovementType.isAvailableValue(getInstance().getSalaryMovementType().getMovementType())
                    || hasOfficialGeneration());
        }

        return idSalaryMovementReadOnly;
    }

    private Boolean hasOfficialGeneration() {
        return gestionPayrollService.hasOfficialGeneration(getInstance().getGestionPayroll());
    }

    private Boolean validateHasOfficialPayroll() {
        Boolean hasOfficialGeneration = hasOfficialGeneration();

        if (hasOfficialGeneration) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "SalaryMovement.error.officialPayroll",
                    getInstance().getGestionPayroll().getGestionName());
        }

        return !hasOfficialGeneration;
    }

    public List<MovementType> getSalaryMovementTypeByAccessRight() {
        List<MovementType> movementTypeList = new ArrayList<MovementType>();
        if (getInstance() != null
                && null != getInstance().getGestionPayroll()
                && PayrollGenerationType.GENERATION_BY_TIME.equals(getInstance().getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
            if (appIdentity.hasPermission("MOVEMENTTYPEOTHERDISCOUNT", PermissionType.VIEW.name())) {
                movementTypeList.add(MovementType.OTHER_DISCOUNT);
            }
            if (appIdentity.hasPermission("MOVEMENTTYPEDISCOUNTOUTOFRETENTION", PermissionType.VIEW.name())) {
                movementTypeList.add(MovementType.DISCOUNT_OUT_OF_RETENTION);
            }
        }
        if (appIdentity.hasPermission("MOVEMENTTYPEOTHERINCOME", PermissionType.VIEW.name())) {
            movementTypeList.add(MovementType.OTHER_INCOME);
        }
        if (movementTypeList.isEmpty()) {
            movementTypeList = null;
        }
        return movementTypeList;
    }

    public Boolean getActiveForTaxPayrollGeneration() {
        return null == getGestionPayrollNotEmpty() ? null : Boolean.FALSE;
    }

    public Boolean getGestionPayrollNotEmpty() {
        return null != getInstance().getGestionPayroll() ? Boolean.TRUE : null;
    }
}