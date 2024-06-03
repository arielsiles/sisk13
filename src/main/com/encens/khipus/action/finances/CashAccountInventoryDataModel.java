package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountPk;
import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.model.finances.ExpenseType;
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
@Name("cashAccountInventoryDataModel")
@Scope(ScopeType.PAGE)
public class CashAccountInventoryDataModel extends QueryDataModel<CashAccountPk, CashAccount> {

    private static final String[] RESTRICTIONS =
            {"lower(cashAccount.accountCode) like  concat(lower(#{cashAccountInventoryDataModel.criteria.accountCode}), '%')",
             "lower(cashAccount.description) like  concat('%',concat(lower(#{cashAccountInventoryDataModel.criteria.description}), '%'))",
             "cashAccount.hasWarehousePermission = #{cashAccountInventoryDataModel.hasWarehousePermission}",
             "cashAccount.expenseType = #{cashAccountInventoryDataModel.expenseType}",
             "cashAccount.movementAccount = #{cashAccountInventoryDataModel.movementAccount}",
             "cashAccount.accountType = #{cashAccountInventoryDataModel.cashAccountType}",
             "cashAccount.currency = #{cashAccountInventoryDataModel.criteria.currency}"
            };

    private Boolean movementAccount;
    private Boolean active = Boolean.TRUE;
    private CashAccountType cashAccountType;

    private Boolean hasWarehousePermission = Boolean.TRUE;
    private ExpenseType expenseType;

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
        this.active = Boolean.TRUE.equals(active) ? active : null;
    }


    public CashAccountType getCashAccountType() {
        return cashAccountType;
    }

    public void setCashAccountType(CashAccountType cashAccountType) {
        this.cashAccountType = cashAccountType;
    }

    public Boolean getHasWarehousePermission() {
        return hasWarehousePermission;
    }

    public void setHasWarehousePermission(Boolean hasWarehousePermission) {
        this.hasWarehousePermission = hasWarehousePermission;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }
}
