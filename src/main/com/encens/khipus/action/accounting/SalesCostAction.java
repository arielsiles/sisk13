package com.encens.khipus.action.accounting;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseType;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;

/**
 * OrganizationAction
 *
 * @author
 * @version 2.26
 */
@Name("salesCostAction")
@Scope(ScopeType.CONVERSATION)
public class SalesCostAction extends GenericAction {

    @In
    private VoucherAccoutingService voucherAccoutingService;

    private Warehouse warehouse;
    private Date startDate;
    private Date endDate;

    public void generateCostOfSales()throws CompanyConfigurationNotFoundException {

        if (this.warehouse.getWarehouseType().equals(WarehouseType.DAIRY)){
            voucherAccoutingService.createCostOfSale_MilkProducts(startDate, endDate);
            voucherAccoutingService.createCostOfSale_MilkProductsReplacement(startDate, endDate);
            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(2), "4470610218"); //Degustacion
            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(3), "4460310104"); //Refrigerio
        }

        if (this.warehouse.getWarehouseType().equals(WarehouseType.VETERINARY)){
            voucherAccoutingService.createCostOfSale_VeterinaryProducts(startDate, endDate);
        }

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
}
