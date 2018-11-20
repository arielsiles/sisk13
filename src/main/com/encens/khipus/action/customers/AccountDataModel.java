package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.Credit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Account
 *
 * @author:
 */

@Name("accountDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CREDIT','VIEW')}")
public class AccountDataModel extends QueryDataModel<Long, Account> {

    private String firstName;
    private String lastName;

    private static final String[] RESTRICTIONS = {
            "lower(account.partner.firstName) like concat('%', concat(lower(#{accountDataModel.firstName}), '%'))",
            "lower(account.partner.lastName) like concat('%', concat(lower(#{accountDataModel.lastName}), '%'))",
            "lower(account.accountNumber) like concat('%', concat(lower(#{accountDataModel.criteria.accountNumber}), '%'))"
    };

    @Override
    public String getEjbql() {
        return "select account from Account account";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
