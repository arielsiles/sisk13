package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseState;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.Arrays;


/**
 * @author
 * @version 2.0
 */
@BusinessUnitRestrict
@Name("warehouseAction")
@Scope(ScopeType.CONVERSATION)
public class WarehouseAction extends GenericAction<Warehouse> {

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @Factory(value = "warehouse", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('WAREHOUSE','VIEW')}")
    public Warehouse initWarehouse() {
        return getInstance();
    }

    @Factory(value = "warehouseStates", scope = ScopeType.STATELESS)
    public WarehouseState[] getWarehouseStates() {
        return WarehouseState.values();
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseAction.instance}", postValidation = true)
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Warehouse instance) {
        return super.select(instance);
    }

    @End
    @Override
    @Restrict("#{s:hasPermission('WAREHOUSE','CREATE')}")
    public String create() {
        String validationOutcome = validateResponsible();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        return super.create();
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseAction.instance}")
    @Restrict("#{s:hasPermission('WAREHOUSE','UPDATE')}")
    @End
    public String update() {
        String validationOutcome = validateResponsible();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        return super.update();
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseAction.instance}")
    @Restrict("#{s:hasPermission('WAREHOUSE','DELETE')}")
    @End
    public String delete() {
        return super.delete();
    }

    public void assignResponsible(Employee employee) {
        getInstance().setResponsible(employee);
    }

    public void cleanResponsible() {
        getInstance().setResponsible(null);
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    private String validateResponsible() {
        if (null == getInstance().getResponsible()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("Warehouse.responsible"));
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Warehouse.common.message.duplicated", getInstance().getId().getWarehouseCode());
    }

    public boolean isInUse() {
        return isManaged() && warehouseCatalogService.isInUse(
                Arrays.asList(
                        "select det from MovementDetail det where det.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and det.warehouseCode='" + getInstance().getId().getWarehouseCode() + "'",
                        "select order from PurchaseOrder order where order.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and order.warehouseCode='" + getInstance().getId().getWarehouseCode() + "'",
                        "select inv from Inventory inv where  inv.id.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and inv.id.warehouseCode='" + getInstance().getId().getWarehouseCode() + "'",
                        "select det from InventoryDetail det where  det.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and det.warehouseCode='" + getInstance().getId().getWarehouseCode() + "'"
                ));
    }
    public void clearCashAccount() {
        getInstance().setCashAccount(null);
    }

    public void assignCashAccount(CashAccount cashAccount) {
        getInstance().setCashAccount(cashAccount.getAccountCode());
    }
}
