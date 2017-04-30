package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.model.warehouse.WarehousePK;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.service.warehouse.MovementDetailService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Encens S.R.L.
 * This class implements the general valued evolution report in order to get the
 *
 * @author
 * @version 2.26
 */
public class GeneralValuedEvolutionReportScriptlet extends JRDefaultScriptlet {

    private MovementDetailService movementDetailService = (MovementDetailService) Component.getInstance("movementDetailService");
    private GeneralValuedEvolutionReportAction generalValuedEvolutionReportAction = (GeneralValuedEvolutionReportAction) Component.getInstance("generalValuedEvolutionReportAction");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        ProductItemPK productItemId = (ProductItemPK) this.getFieldValue("productItem.id");
        WarehousePK warehouseId = (WarehousePK) this.getFieldValue("warehouse.id");
        BigDecimal unitaryBalance = (BigDecimal) this.getFieldValue("inventory.unitaryBalance");
        BigDecimal unitCost = (BigDecimal) this.getFieldValue("productItem.unitCost");

        Date initDate=generalValuedEvolutionReportAction.getInitDate();
        Date endDate=generalValuedEvolutionReportAction.getEndDate();
        //Previous values
        BigDecimal previousEntryQuantity = movementDetailService.sumMovementsQuantityUpTo(productItemId.getCompanyNumber(), productItemId.getProductItemCode(),
                WarehouseVoucherState.APR, MovementDetailType.E, warehouseId.getWarehouseCode(),initDate);

        BigDecimal previousExitQuantity = movementDetailService.sumMovementsQuantityUpTo(productItemId.getCompanyNumber(), productItemId.getProductItemCode(),
                WarehouseVoucherState.APR, MovementDetailType.S, warehouseId.getWarehouseCode(),initDate);
        BigDecimal previousUnitaryBalance=previousEntryQuantity.subtract(previousExitQuantity);
        this.setVariableValue("previousUnitaryBalance",previousUnitaryBalance);
        //Next values
        BigDecimal entryQuantity = movementDetailService.sumMovementsQuantityFromTo(productItemId.getCompanyNumber(), productItemId.getProductItemCode(),
                WarehouseVoucherState.APR, MovementDetailType.E, warehouseId.getWarehouseCode(),initDate,endDate);
        this.setVariableValue("approvedEntries", entryQuantity);

        BigDecimal exitQuantity = movementDetailService.sumMovementsQuantityFromTo(productItemId.getCompanyNumber(), productItemId.getProductItemCode(),
                WarehouseVoucherState.APR, MovementDetailType.S, warehouseId.getWarehouseCode(),initDate, endDate);
        this.setVariableValue("approvedEgresses", exitQuantity);

        BigDecimal remainingPlusPending = previousUnitaryBalance.add(entryQuantity).subtract(exitQuantity);
        this.setVariableValue("remainingPlusPending", remainingPlusPending);
        this.setVariableValue("totalAmount", unitCost.multiply(remainingPlusPending));

    }


}
