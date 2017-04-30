package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountPk;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * CashAccountDataModel
 *
 * @author
 * @version 2.0
 */
@Name("cashAccountDataModel")
@Scope(ScopeType.PAGE)
public class CashAccountDataModel extends QueryDataModel<CashAccountPk, CashAccount> {
    /*private static final String[] RESTRICTIONS =
            {"lower(cashAccount.accountCode) like  concat(lower(#{cashAccountDataModel.criteria.accountCode}), '%')",
                    "lower(cashAccount.description) like  concat('%',concat(lower(#{cashAccountDataModel.criteria.description}), '%'))",
                    "cashAccount.hasAccountingPermission = #{cashAccountDataModel.criteria.hasAccountingPermission}",
                    "cashAccount.hasTreasuryPermission = #{cashAccountDataModel.criteria.hasTreasuryPermission}",
                    "cashAccount.hasPayableAccountsPermission = #{cashAccountDataModel.criteria.hasPayableAccountsPermission}",
                    "cashAccount.hasFixedAssetsPermission = #{cashAccountDataModel.criteria.hasFixedAssetsPermission}",
                    "cashAccount.hasWarehousePermission = #{cashAccountDataModel.criteria.hasWarehousePermission}",
                    "cashAccount.hasReceivableAccountsPermission = #{cashAccountDataModel.criteria.hasReceivableAccountsPermission}",
                    "cashAccount.hasCostCenter = #{cashAccountDataModel.criteria.hasCostCenter}",
                    "cashAccount.movementAccount = #{cashAccountDataModel.movementAccount}",
                    "cashAccount.active = #{cashAccountDataModel.active}",
                    "cashAccount.accountType = #{cashAccountDataModel.criteria.accountType}",
                    "cashAccount.accountClass = #{cashAccountDataModel.criteria.accountClass}",
                    "cashAccount.currency = #{cashAccountDataModel.criteria.currency}"};*/

    private static final String[] RESTRICTIONS =
            {"lower(cashAccount.accountCode) like  concat(lower(#{cashAccountDataModel.criteria.accountCode}), '%')",
             "lower(cashAccount.description) like  concat('%',concat(lower(#{cashAccountDataModel.criteria.description}), '%'))",
             "cashAccount.movementAccount = #{cashAccountDataModel.movementAccount}",
             "cashAccount.active = #{cashAccountDataModel.active}",
             "cashAccount.accountType = #{cashAccountDataModel.criteria.accountType}",
             /*"cashAccount.accountClass = #{cashAccountDataModel.criteria.accountClass}",*/
             "cashAccount.currency = #{cashAccountDataModel.criteria.currency}"
            };

    private Boolean movementAccount = Boolean.TRUE;
    private Boolean active = Boolean.TRUE;

    @Create
    public void init() {
        sortProperty = "cashAccount.accountCode";
    }

    @Override
    public String getEjbql() {
        return "select cashAccount from CashAccount cashAccount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Boolean getMovementAccount() {
        return movementAccount;
    }

    public void setMovementAccount(Boolean movementAccount) {
        this.movementAccount = Boolean.TRUE.equals(movementAccount) ? movementAccount : null;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void searchByAccountClass(String accountClass) {
        getCriteria().setAccountClass(accountClass);
        getCriteria().setCurrency(null);
        updateAndSearch();
    }

    public void searchByAccountClassAndCurrency(String accountClass, String currency) {
        getCriteria().setAccountClass(accountClass);
        getCriteria().setCurrency(FinancesCurrencyType.valueOf(currency));
        updateAndSearch();
    }

    public void searchByCurrency(String currency) {
        getCriteria().setAccountClass(null);
        getCriteria().setCurrency(FinancesCurrencyType.valueOf(currency));
        updateAndSearch();
    }
}
