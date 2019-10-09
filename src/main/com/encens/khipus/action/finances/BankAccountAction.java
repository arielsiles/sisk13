package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.service.employees.BankAccountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * Actions for BankAccount
 *
 * @author
 */

@Name("bankAccountAction")
@Scope(ScopeType.CONVERSATION)
public class BankAccountAction extends GenericAction<BankAccount> {

    @In
    private BankAccountService bankAccountService;

    private Boolean isEmployee = Boolean.FALSE;
    private Boolean isProducer = Boolean.FALSE;

    @Factory(value = "bankAccount", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BANKACCOUNT','VIEW')}")
    public BankAccount initBankAccount() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "accountNumber";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('BANKACCOUNT','CREATE')}")
    public String create() {

        if (getInstance().getEmployee() == null && getInstance().getProducer() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankAccount.error.employeeRequired");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }

        if (getInstance().getDefaultAccount() && getEmployee()) {
            if (bankAccountService.hasDefaultAccount(getInstance().getEmployee())) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankAccount.error.defaultAccountDuplicated", getInstance().getEmployee().getFullName());
                return com.encens.khipus.framework.action.Outcome.REDISPLAY;
            }
        } else {
            if (!bankAccountService.hasDefaultAccount(getInstance().getEmployee()) && getEmployee()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankAccount.error.defaultAccountUnregistered", getInstance().getEmployee().getFullName());
                return com.encens.khipus.framework.action.Outcome.REDISPLAY;
            }
        }

        return super.create();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('BANKACCOUNT','UPDATE')}")
    public String update() {

        if (getInstance().getEmployee() == null && getInstance().getProducer() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankAccount.error.employeeRequired");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }

        if (getInstance().getDefaultAccount() && bankAccountService.hasDefaultAccount(getInstance().getEmployee(), getInstance()) && getEmployee()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BankAccount.error.defaultAccountDuplicated");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }

        return super.update();
    }

    public String getEmployeeFullName() {
        return getInstance().getEmployee() != null ? getInstance().getEmployee().getFullName() : "";
    }

    public String getProducerFullName() {
        return getInstance().getProducer() != null ? getInstance().getProducer().getFullName() : "";
    }

    public void clearProducer() {
        getInstance().setProducer(null);
    }

    public void assignEmployee(Employee employee) {
        getInstance().setEmployee(employee);
    }

    public void assignProducer(RawMaterialProducer producer) {
        getInstance().setProducer(producer);
    }

    public void clearEmployee() {
        getInstance().setEmployee(null);
    }

    public Boolean getEmployee() {
        return isEmployee;
    }

    public void setEmployee(Boolean employee) {
        isEmployee = employee;
        isProducer = false;
    }

    public Boolean getProducer() {
        return isProducer;
    }

    public void setProducer(Boolean producer) {
        isProducer = producer;
        isEmployee = false;
    }

    public Boolean isEmployeeAccount(){
        Boolean result = Boolean.FALSE;
        if (isManaged())
            if (getInstance().getEmployee() != null && getInstance().getProducer() == null)
                result = Boolean.TRUE;
        return result;
    }

    public Boolean isProducerAccount(){
        Boolean result = Boolean.FALSE;
        if (isManaged())
            if (getInstance().getProducer() != null && getInstance().getEmployee() == null)
                result = Boolean.TRUE;
        return result;
    }

}