package com.encens.khipus.action.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.service.admin.UserBusinessUnitService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.util.Hash;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * User actions class
 *
 * @author
 * @version 1.0
 */
@Name("userAction")
@Scope(ScopeType.CONVERSATION)
public class UserAction extends GenericAction<User> {

    @In
    private User currentUser;

    @In
    private UserService userService;

    @In
    private UserBusinessUnitService userBusinessUnitService;

    @In
    private FinancesUserService financesUserService;

    private String originalPassword;

    private List<BusinessUnit> businessUnits = new ArrayList<BusinessUnit>();

    @Logger
    protected Log log;

    @Factory(value = "user", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('USER','VIEW')}")
    public User initUser() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('USER','VIEW')}")
    public String select(User user) {
        String result = super.select(user);
        if (com.encens.khipus.framework.action.Outcome.SUCCESS.equals(result)) {
            businessUnits = userBusinessUnitService.readBusinessUnits(user);

            getInstance().setConfirmPassword(getInstance().getPassword());
            setOriginalPassword(getInstance().getPassword());
        }
        return result;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('USER','CREATE')}")
    public String create() {

        if (getInstance().getEmployee() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "User.error.employeeRequired");
            return Outcome.REDISPLAY;
        }

        String financesRoleOutcome = validateFinancesRoles();
        if (!Outcome.SUCCESS.equals(financesRoleOutcome)) {
            return financesRoleOutcome;
        }

        String financesValidationOutcome = validateFinancesFields();
        if (!Outcome.SUCCESS.equals(financesValidationOutcome)) {
            return financesValidationOutcome;
        }

        final String password = getInstance().getPassword();
        getInstance().setPassword(Hash.instance().hash(getInstance().getPassword()));

        try {
            userService.create(getInstance(), businessUnits);
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            getInstance().setPassword(password);
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('USER','UPDATE')}")
    public String update() {
        String financesRoleOutcome = validateFinancesRoles();
        if (!Outcome.SUCCESS.equals(financesRoleOutcome)) {
            return financesRoleOutcome;
        }

        String financesValidationOutcome = validateFinancesFields();
        if (!Outcome.SUCCESS.equals(financesValidationOutcome)) {
            return financesValidationOutcome;
        }

        if (!getInstance().getPassword().equals(originalPassword)) {
            getInstance().setPassword(Hash.instance().hash(getInstance().getPassword()));
        }
        Long currentVersion = (Long) getVersion(getInstance());

        try {
            userService.update(getInstance(), businessUnits);
            setOriginalPassword(getInstance().getPassword());
            getInstance().setConfirmPassword(getInstance().getPassword());

            Events.instance().raiseEvent("SessionUserUpdaterAction.updateUserSessionBusinessUnit", getInstance().getId());
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }

            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('USER','DELETE')}")
    public String delete() {
        Boolean isFinanceUser = financesUserService.isFinanceUser(getInstance().getId());
        if (null != isFinanceUser && isFinanceUser) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "User.error.delete.financeUser");

            return Outcome.SUCCESS;
        }

        return super.delete();
    }

    @Override
    public String getDisplayNameProperty() {
        return "username";
    }

    public String getOriginalPassword() {
        return originalPassword;
    }

    public void setOriginalPassword(String originalPassword) {
        this.originalPassword = originalPassword;
    }

    @Override
    protected void addDuplicatedMessage() {
        if (isManaged()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "User.error.duplicate.update", getInstance().getUsername());
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "User.error.duplicate.create", getInstance().getUsername(), getInstance().getEmployee().getFullName());
        }
    }

    public void assignEmployee(Employee employee) {
        getInstance().setEmployee(employee);
    }

    public boolean isShowFinancesCodeField() {
        return null != getInstance().getFinancesUser() && getInstance().getFinancesUser();
    }

    public boolean isDisabledFinancesFields() {
        return isManaged() &&
                null != getInstance().getFinancesUser() &&
                getInstance().getFinancesUser() &&
                null != getInstance().getFinancesCode() &&
                !"".equals(getInstance().getFinancesCode().trim());
    }

    private String validateFinancesFields() {
        if (null == getInstance().getFinancesUser() || !getInstance().getFinancesUser()) {
            return Outcome.SUCCESS;
        }

        String financesCode = getInstance().getFinancesCode();

        if (null == financesCode || "".equals(financesCode.trim())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("User.financesCode"));
            return Outcome.REDISPLAY;
        }

        Long userId = null;
        if (isManaged()) {
            userId = getInstance().getId();
        }

        Boolean isAvailableFinancesCode = financesUserService.isAvailableCode(financesCode, userId);

        if (null != isAvailableFinancesCode && !isAvailableFinancesCode) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "User.error.duplicate.financeUser", financesCode);

            getInstance().setFinancesCode(null);
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    private String validateFinancesRoles() {
        if (null != getInstance().getFinancesUser() && getInstance().getFinancesUser()) {
            return Outcome.SUCCESS;
        }

        if (userService.useFinancesAccessRights(getInstance(), currentUser.getCompany())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "User.error.required.financeUser", MessageUtils.getMessage("User.financesUser"));

            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    @Override
    protected GenericService getService() {
        return userService;
    }

    public List<BusinessUnit> getBusinessUnits() {
        return businessUnits;
    }

    public void setBusinessUnits(List<BusinessUnit> businessUnits) {
        this.businessUnits = businessUnits;
    }
}
