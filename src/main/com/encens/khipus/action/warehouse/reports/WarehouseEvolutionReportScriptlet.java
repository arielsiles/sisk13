package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Encens S.R.L.
 * Scriptlet to calculate values for warehouse evaluation report
 *
 * @author
 * @version $Id: WarehouseEvolutionReportScriptlet.java  22-abr-2010 14:50:35$
 */
public class WarehouseEvolutionReportScriptlet extends JRDefaultScriptlet {

    private MovementDetailService movementDetailService = (MovementDetailService) Component.getInstance("movementDetailService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String productItemCode = (String) getFieldValue("productItem.productItemCode");
        String warehouseCode = (String) getFieldValue("warehouse.warehouseCode");
        Date initPeriodDateParam = (Date) getParameterValue("initPeriodDateParam", false); //with false param the parameter can be not defined
        Date endPeriodDateParam = (Date) getParameterValue("endPeriodDateParam", false); 

        BigDecimal initialQuantityValue = BigDecimal.ZERO;
        BigDecimal inputQuantityValue = BigDecimal.ZERO;
        BigDecimal outputQuantityValue = BigDecimal.ZERO;
        BigDecimal finalQuantityValue = BigDecimal.ZERO;
        BigDecimal initialAmountValue = BigDecimal.ZERO;
        BigDecimal inputAmountValue = BigDecimal.ZERO;
        BigDecimal outputAmountValue = BigDecimal.ZERO;
        BigDecimal finalAmountValue = BigDecimal.ZERO;

        //only calculate if init date param is defined
        if (initPeriodDateParam != null && endPeriodDateParam != null) {

            initialQuantityValue = calculateInitialQuantity(Constants.defaultCompanyNumber, productItemCode, warehouseCode, initPeriodDateParam);
            inputQuantityValue = movementDetailService.sumQuantityByProductItemWarehouseInRangeDate(Constants.defaultCompanyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, MovementDetailType.E, initPeriodDateParam, endPeriodDateParam);
            outputQuantityValue = movementDetailService.sumQuantityByProductItemWarehouseInRangeDate(Constants.defaultCompanyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, MovementDetailType.S, initPeriodDateParam, endPeriodDateParam);
            finalQuantityValue = BigDecimalUtil.subtract(BigDecimalUtil.sum(initialQuantityValue, inputQuantityValue), outputQuantityValue);

            initialAmountValue = calculateInitialAmount(Constants.defaultCompanyNumber, productItemCode, warehouseCode, initPeriodDateParam);
            inputAmountValue = movementDetailService.sumAmountByProductItemWarehouseInRangeDate(Constants.defaultCompanyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, MovementDetailType.E, initPeriodDateParam, endPeriodDateParam);
            outputAmountValue = movementDetailService.sumAmountByProductItemWarehouseInRangeDate(Constants.defaultCompanyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, MovementDetailType.S, initPeriodDateParam, endPeriodDateParam);
            finalAmountValue = BigDecimalUtil.subtract(BigDecimalUtil.sum(initialAmountValue, inputAmountValue), outputAmountValue);
        }

        //set in report variables
        this.setVariableValue("initialQuantityVar", initialQuantityValue);
        this.setVariableValue("inputQuantityVar", inputQuantityValue);
        this.setVariableValue("outputQuantityVar", outputQuantityValue);
        this.setVariableValue("finalQuantityVar", finalQuantityValue);

        this.setVariableValue("initialAmountVar", initialAmountValue);
        this.setVariableValue("inputAmountVar", inputAmountValue);
        this.setVariableValue("outputAmountVar", outputAmountValue);
        this.setVariableValue("finalAmountVar", finalAmountValue);
    }

    private BigDecimal calculateInitialQuantity(String companyNumber, String productItemCode, String warehouseCode, Date initPeriodDate) {
        BigDecimal initialValue = BigDecimal.ZERO;
        BigDecimal inputQuantity = movementDetailService.sumQuantityByProductItemWarehouseInBeforeDates(companyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, initPeriodDate, MovementDetailType.E);
        BigDecimal outputQuantity = movementDetailService.sumQuantityByProductItemWarehouseInBeforeDates(companyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, initPeriodDate, MovementDetailType.S);

        if (inputQuantity != null) {
            initialValue = inputQuantity;
        }
        if (outputQuantity != null) {
            initialValue = BigDecimalUtil.subtract(initialValue, outputQuantity);
        }
        return initialValue;
    }

    private BigDecimal calculateInitialAmount(String companyNumber, String productItemCode, String warehouseCode, Date initPeriodDate) {
        BigDecimal initialValue = BigDecimal.ZERO;
        BigDecimal inputAmount = movementDetailService.sumAmountByProductItemWarehouseInBeforeDates(companyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, initPeriodDate, MovementDetailType.E);
        BigDecimal outputAmount = movementDetailService.sumAmountByProductItemWarehouseInBeforeDates(companyNumber, productItemCode, warehouseCode, WarehouseVoucherState.APR, initPeriodDate, MovementDetailType.S);

        if (inputAmount != null) {
            initialValue = inputAmount;
        }
        if (outputAmount != null) {
            initialValue = BigDecimalUtil.subtract(initialValue, outputAmount);
        }
        return initialValue;
    }


}
