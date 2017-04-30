package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.model.warehouse.WarehousePK;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.WarehouseService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Encens S.R.L.
 *
 *
 * @author
 * @version 2.3
 */
public class EstimationStockReportScriptlet extends JRDefaultScriptlet {

    private WarehouseService warehouseService = (WarehouseService) Component.getInstance("warehouseService");
    private EstimationStockReportAction estimationStockReportAction = (EstimationStockReportAction) Component.getInstance("estimationStockReportAction");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String codArt = (String) this.getFieldValue("inventory.articleCode");
        BigDecimal unitaryBalance = (BigDecimal) this.getFieldValue("inventory.unitaryBalance");
        Date date = estimationStockReportAction.getDate();
        Gestion gestion = estimationStockReportAction.getGestion();

        BigDecimal totalOrders;
        if(date == null)
        {
            totalOrders = warehouseService.findAmountOrderByCodArt(codArt,gestion);
        }
        else
        totalOrders = warehouseService.findAmountOrderByCodArt(codArt,gestion,date);

        BigDecimal totalExpectedAmount = warehouseService.findExpectedAmountOrderProduction(codArt,gestion);
        BigDecimal totalProducedAmount = warehouseService.findProducedAmountOrderProduction(codArt,gestion);
        totalProducedAmount = totalProducedAmount.add(warehouseService.findProducedAmountRepro(codArt,gestion));
        BigDecimal totalProducedAmountPlusBalance = unitaryBalance.add(totalProducedAmount);
        BigDecimal totalBalanceLessTotalOrder = unitaryBalance.subtract(totalOrders);
        BigDecimal totalProducedAmountPlusBalanceLessOrders = unitaryBalance.subtract(totalOrders).add(totalProducedAmount);
        BigDecimal totalExpectedAmountPlusBalanceLessOrders = totalExpectedAmount.add(totalProducedAmountPlusBalanceLessOrders);

        this.setVariableValue("totalOrders", totalOrders);
        this.setVariableValue("totalPlanned",totalExpectedAmount);
        this.setVariableValue("totalProduced",totalProducedAmount);
        this.setVariableValue("totalBalanceLessTotalOrder",totalBalanceLessTotalOrder);
        this.setVariableValue("totalProducedAmountPlusBalance",totalProducedAmountPlusBalance);
        this.setVariableValue("totalPlannedPlusBalanceLessTotalOrder", totalExpectedAmountPlusBalanceLessOrders);
        this.setVariableValue("totalPoducedPlusBalanceLessTotalOrder", totalProducedAmountPlusBalanceLessOrders);

    }


}
