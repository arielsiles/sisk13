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
 * @version 2.22
 */
@Name("rotatoryFundCashAccountDataModel")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundCashAccountDataModel extends QueryDataModel<CashAccountPk, CashAccount> {
    private static final String[] RESTRICTIONS =
            {"lower(cashAccount.accountCode) like  concat(lower(#{rotatoryFundCashAccountDataModel.criteria.accountCode}), '%')",
                    "lower(cashAccount.description) like  concat('%',concat(lower(#{rotatoryFundCashAccountDataModel.criteria.description}), '%'))",
                    "cashAccount.hasAccountingPermission = #{rotatoryFundCashAccountDataModel.criteria.hasAccountingPermission}",
                    "cashAccount.hasTreasuryPermission = #{rotatoryFundCashAccountDataModel.criteria.hasTreasuryPermission}",
                    "cashAccount.hasPayableAccountsPermission = #{rotatoryFundCashAccountDataModel.criteria.hasPayableAccountsPermission}",
                    "cashAccount.hasFixedAssetsPermission = #{rotatoryFundCashAccountDataModel.criteria.hasFixedAssetsPermission}",
                    "cashAccount.hasWarehousePermission = #{rotatoryFundCashAccountDataModel.criteria.hasWarehousePermission}",
                    "cashAccount.hasReceivableAccountsPermission = #{rotatoryFundCashAccountDataModel.criteria.hasReceivableAccountsPermission}",
                    "cashAccount.hasCostCenter = #{rotatoryFundCashAccountDataModel.criteria.hasCostCenter}",
                    "cashAccount.movementAccount = #{cashAccountDataModel.movementAccount}",
                    "cashAccount.active = #{rotatoryFundCashAccountDataModel.active}",
                    "cashAccount.accountType = #{rotatoryFundCashAccountDataModel.criteria.accountType}",
                    "cashAccount.accountClass = #{rotatoryFundCashAccountDataModel.criteria.accountClass}",
                    "cashAccount.currency = #{rotatoryFundCollection.sourceCurrency}",
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
        updateAndSearch();
    }

    public void searchByAccountClassAndCurrency(String accountClass, String currency) {
        getCriteria().setAccountClass(accountClass);
        getCriteria().setCurrency(FinancesCurrencyType.valueOf(currency));
        updateAndSearch();
    }

}