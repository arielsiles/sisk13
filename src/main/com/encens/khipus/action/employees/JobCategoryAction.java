package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.CashAccount;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for JobCategory
 *
 * @author
 * @version 1.1.6
 */

@Name("jobCategoryAction")
@Scope(ScopeType.CONVERSATION)
public class JobCategoryAction extends GenericAction<JobCategory> {

    @Factory(value = "jobCategory", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('JOBCATEGORY','VIEW')}")
    public JobCategory initJobCategory() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @End
    @Restrict("#{s:hasPermission('JOBCATEGORY','CREATE')}")
    public String create() {
        return super.create();
    }

    @End
    @Restrict("#{s:hasPermission('JOBCATEGORY','UPDATE')}")
    public String update() {

        return super.update();
    }

    @End
    @Restrict("#{s:hasPermission('JOBCATEGORY','DELETE')}")
    public String delete() {
        return super.delete();
    }

    //    nationalCurrencyDebitAccount

    public void assignNationalCurrencyDebitAccount(CashAccount nationalCurrencyDebitAccount) {
        getInstance().setNationalCurrencyDebitAccount(nationalCurrencyDebitAccount);
    }

    public void clearNationalCurrencyDebitAccount() {
        getInstance().setNationalCurrencyDebitAccount(null);
    }

//    nationalCurrencyCreditAccount

    public void assignNationalCurrencyCreditAccount(CashAccount nationalCurrencyCreditAccount) {
        getInstance().setNationalCurrencyCreditAccount(nationalCurrencyCreditAccount);
    }

    public void clearNationalCurrencyCreditAccount() {
        getInstance().setNationalCurrencyCreditAccount(null);
    }

//    foreignCurrencyDebitAccount

    public void assignForeignCurrencyDebitAccount(CashAccount foreignCurrencyDebitAccount) {
        getInstance().setForeignCurrencyDebitAccount(foreignCurrencyDebitAccount);
    }

    public void clearForeignCurrencyDebitAccount() {
        getInstance().setForeignCurrencyDebitAccount(null);
    }

//    foreignCurrencyCreditAccount

    public void assignForeignCurrencyCreditAccount(CashAccount foreignCurrencyCreditAccount) {
        getInstance().setForeignCurrencyCreditAccount(foreignCurrencyCreditAccount);
    }


    public void clearForeignCurrencyCreditAccount() {
        getInstance().setForeignCurrencyCreditAccount(null);
    }

    public void assignNationalCurrencyChristmasExpendAccount(CashAccount cashAccount) {
        getInstance().setNationalCurrencyChristmasExpendAccount(cashAccount);
    }


    public void clearNationalCurrencyChristmasExpendAccount() {
        getInstance().setNationalCurrencyChristmasExpendAccount(null);
    }

    public void assignNationalCurrencyChristmasProvisionAccount(CashAccount cashAccount) {
        getInstance().setNationalCurrencyChristmasProvisionAccount(cashAccount);
    }


    public void clearNationalCurrencyChristmasProvisionAccount() {
        getInstance().setNationalCurrencyChristmasProvisionAccount(null);
    }

    public void assignForeignCurrencyChristmasProvisionAccount(CashAccount cashAccount) {
        getInstance().setForeignCurrencyChristmasProvisionAccount(cashAccount);
    }


    public void clearForeignCurrencyChristmasProvisionAccount() {
        getInstance().setForeignCurrencyChristmasProvisionAccount(null);
    }

    public void assignNationalCurrencyCompensationExpendAccount(CashAccount cashAccount) {
        getInstance().setNationalCurrencyCompensationExpendAccount(cashAccount);
    }

    public void clearNationalCurrencyCompensationExpendAccount() {
        getInstance().setNationalCurrencyCompensationExpendAccount(null);
    }

    public void assignNationalCurrencyCompensationPrevisionAccount(CashAccount cashAccount) {
        getInstance().setNationalCurrencyCompensationPrevisionAccount(cashAccount);
    }

    public void clearNationalCurrencyCompensationPrevisionAccount() {
        getInstance().setNationalCurrencyCompensationPrevisionAccount(null);
    }

    public void assignForeignCurrencyCompensationPrevisionAccount(CashAccount cashAccount) {
        getInstance().setForeignCurrencyCompensationPrevisionAccount(cashAccount);
    }


    public void clearForeignCurrencyCompensationPrevisionAccount() {
        getInstance().setForeignCurrencyCompensationPrevisionAccount(null);
    }

    public void assignPensionFundPatronalAccount(CashAccount cashAccount) {
        getInstance().setPensionFundPatronalAccount(cashAccount);
    }

    public void clearPensionFundPatronalAccount() {
        getInstance().setPensionFundPatronalAccount(null);
    }

    public void assignSocialSecurityPatronalAccount(CashAccount cashAccount) {
        getInstance().setSocialSecurityPatronalAccount(cashAccount);
    }


    public void clearSocialSecurityPatronalAccount() {
        getInstance().setSocialSecurityPatronalAccount(null);
    }
}
