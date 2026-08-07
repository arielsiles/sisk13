package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.FinancesBankAccountPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Listado de cuentas bancarias (ck_ctas_bco).
 *
 * @author
 * @version 1.0
 */
@Name("financesBankAccountDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FINANCESBANKACCOUNT','VIEW')}")
public class FinancesBankAccountDataModel extends QueryDataModel<FinancesBankAccountPk, FinancesBankAccount> {

    private static final String[] RESTRICTIONS = {
            "lower(financesBankAccount.id.accountNumber) like concat(lower(#{financesBankAccountDataModel.criteria.id.accountNumber}), '%')",
            "lower(financesBankAccount.description) like concat('%', concat(lower(#{financesBankAccountDataModel.criteria.description}), '%'))",
            "lower(financesBankAccount.bankCode) like concat(lower(#{financesBankAccountDataModel.criteria.bankCode}), '%')",
            "financesBankAccount.currency = #{financesBankAccountDataModel.criteria.currency}",
            "financesBankAccount.state = #{financesBankAccountDataModel.criteria.state}"};

    @Create
    public void init() {
        sortProperty = "financesBankAccount.description";
    }

    @Override
    public String getEjbql() {
        return "select financesBankAccount from FinancesBankAccount financesBankAccount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
