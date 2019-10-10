package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.BankAccount;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for BankAccount
 *
 * @author
 */

@Name("bankAccountDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BANKACCOUNT','VIEW')}")
public class BankAccountDataModel extends QueryDataModel<Long, BankAccount> {
    private static final String[] RESTRICTIONS = {
            "bankAccount.accountNumber like concat(#{bankAccountDataModel.criteria.accountNumber}, '%')",
            "bankAccount.clientCod like concat(#{bankAccountDataModel.criteria.clientCod}, '%')",
            "bankAccount.bankEntity = #{bankAccountDataModel.criteria.bankEntity}",
            "bankAccount.employee.idNumber like concat(#{bankAccountDataModel.idNumber}, '%')"};

    private String idNumber;

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    @Create
    public void init() {
        sortProperty = "bankAccount.employee.firstName,bankAccount.employee.maidenName,bankAccount.employee.firstName";
    }

    @Override
    public String getEjbql() {
        return "select bankAccount from BankAccount bankAccount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}