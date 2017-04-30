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

/**
 * Encens S.R.L.
 * This class implements valued warehouse residue report in order to get the
 *
 * @author
 * @version 2.3
 */
public class ValuedWarehouseResidueReportScriptlet extends JRDefaultScriptlet {

    private MovementDetailService movementDetailService = (MovementDetailService) Component.getInstance("movementDetailService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        ProductItemPK productItemId = (ProductItemPK) this.getFieldValue("productItem.id");
        WarehousePK warehouseId = (WarehousePK) this.getFieldValue("warehouse.id");
        BigDecimal unitaryBalance = (BigDecimal) this.getFieldValue("inventory.unitaryBalance");
        BigDecimal unitCost = (BigDecimal) this.getFieldValue("productItem.unitCost");


        BigDecimal entryQuantity = movementDetailService.sumMovementsQuantity(productItemId.getCompanyNumber(), productItemId.getProductItemCode(),
                WarehouseVoucherState.PEN, MovementDetailType.E, warehouseId.getWarehouseCode());
        this.setVariableValue("pendingEntries", entryQuantity);

        BigDecimal exitQuantity = movementDetailService.sumMovementsQuantity(productItemId.getCompanyNumber(), productItemId.getProductItemCode(),
                WarehouseVoucherState.PEN, MovementDetailType.S, warehouseId.getWarehouseCode());
        this.setVariableValue("pendingEgresses", exitQuantity);

        BigDecimal remainingPlusPending = unitaryBalance.add(entryQuantity).subtract(exitQuantity);
        this.setVariableValue("remainingPlusPending", remainingPlusPending);
        this.setVariableValue("totalAmount", unitCost.multiply(remainingPlusPending));

    }


}
