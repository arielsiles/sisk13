package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Encens S.R.L.
 * Scriplet to calculate values to articles report
 *
 * @author
 * @version $Id: ArticlesReportScriptlet.java  06-may-2010 17:12:40$
 */
public class ArticlesReportScriptlet extends JRDefaultScriptlet {

    private MovementDetailService movementDetailService = (MovementDetailService) Component.getInstance("movementDetailService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        String companyNumber = (String) getFieldValue("productItem.id.companyNumber");
        String productItemCode = (String) getFieldValue("productItem.code");
        String warehouseCode = (String) getFieldValue("warehouse.code");

        BigDecimal unitBalance = getFieldAsBigDecimal("inventory.unitaryBalance");
        BigDecimal unitCost = getFieldAsBigDecimal("productItem.unitCost");
        BigDecimal totalCost = calculateTotalCost(unitBalance, unitCost);

        MovementDetail movementDetail = movementDetailService.findLastMovementDetail(companyNumber, productItemCode, warehouseCode);
        Date movementDate = null;
        String movementType = null;
        if (movementDetail != null) {
            movementDate = movementDetail.getMovementDetailDate();
            movementType = MessageUtils.getMessage(movementDetail.getMovementType().getResourceKey());
        }

        //set in report variables
        this.setVariableValue("totalCostVar", totalCost);
        this.setVariableValue("lastMovementDateVar", movementDate);
        this.setVariableValue("movementTypeVar", movementType);
    }


    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }

    private BigDecimal calculateTotalCost(BigDecimal unitBalance, BigDecimal unitCost) {
        BigDecimal total = null;
        if (unitBalance != null && unitCost != null) {
            total = BigDecimalUtil.multiply(unitBalance, unitCost);
        }
        return total;
    }
}
