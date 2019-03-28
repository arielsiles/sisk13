package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.warehouse.InventoryPeriod;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;

/**
 * OrganizationAction
 *
 * @author
 * @version 2.26
 */
@Name("inventoryPeriodAction")
@Scope(ScopeType.CONVERSATION)
public class InventoryPeriodAction extends GenericAction<InventoryPeriod> {


    private Date startDate;
    private Date endDate;

    private Warehouse warehouse;
    private CashAccount cashAccount;

    public void generateInventoryClosing(){


        System.out.println("----> Mes: " + DateUtils.getCurrentMonth(startDate));
        System.out.println("----> Year: " + DateUtils.getCurrentYear(startDate));
        System.out.println("----> " + warehouse.getFullName());
        System.out.println("----> " + startDate);
        System.out.println("----> " + endDate);
        System.out.println("----> " + cashAccount.getFullName());

    }


    public void cleanWarehouseField() {
        warehouse = null;
    }

    public void clearAccount() {
        setCashAccount(null);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }
}
