package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.model.warehouse.MovementDetailType;
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
 * Scriptlet to calculate values to kardex report
 *
 * @author
 * @version $Id: KardexReportScriptlet.java  16-abr-2010 17:36:48$
 */
public class KardexReportScriptlet extends JRDefaultScriptlet {

    private MovementDetailService movementDetailService = (MovementDetailService) Component.getInstance("movementDetailService");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        super.beforeGroupInit(s);
        String productItemGroupName = "productItemGroup";

        if (s.equals(productItemGroupName)) {

            String productItemCode = (String) getFieldValue("movementDetail.productItemCode");
            Date initPeriodDateParam = (Date) getParameterValue("initPeriodDateParam", false); //with false param the parameter can be not defined 

            BigDecimal initialQuantityValue = BigDecimal.ZERO;
            BigDecimal initialAmountValue = BigDecimal.ZERO;
            //only calculate if init date param is defined
            if (initPeriodDateParam != null) {
                initialQuantityValue = movementDetailService.calculateInitialQuantityToKardex(Constants.defaultCompanyNumber, productItemCode, initPeriodDateParam);
                initialAmountValue = movementDetailService.calculateInitialAmountToKardex(Constants.defaultCompanyNumber, productItemCode, initPeriodDateParam);
            }

            //initialize group values
            this.setVariableValue("initialQuantityVar", initialQuantityValue);
            this.setVariableValue("initialAmountVar", initialAmountValue);
            this.setVariableValue("residueQuantityVar", initialQuantityValue);
            this.setVariableValue("residueAmountVar", initialAmountValue);
        }
    }


    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        BigDecimal quantity = getFieldAsBigDecimal("movementDetail.quantity");
        BigDecimal amount = getFieldAsBigDecimal("movementDetail.amount");
        MovementDetailType movementDetailType = (MovementDetailType) getFieldValue("movementDetail.movementType");

        //calculate input output values
        BigDecimal inputQuantity = null;
        BigDecimal outputQuantity = null;
        BigDecimal residueQuantity = getVariableAsBigDecimal("residueQuantityVar");
        BigDecimal inputAmount = null;
        BigDecimal outputAmount = null;
        BigDecimal residueAmount = getVariableAsBigDecimal("residueAmountVar");

        if (movementDetailType != null) {
            if (quantity != null) {
                if (MovementDetailType.E.equals(movementDetailType)) {
                    inputQuantity = quantity;
                    residueQuantity = BigDecimalUtil.sum(residueQuantity, inputQuantity);
                } else if (MovementDetailType.S.equals(movementDetailType)) {
                    outputQuantity = quantity;
                    residueQuantity = BigDecimalUtil.subtract(residueQuantity, outputQuantity);
                }
            }
            if (amount != null) {
                if (MovementDetailType.E.equals(movementDetailType)) {
                    inputAmount = amount;
                    residueAmount = BigDecimalUtil.sum(residueAmount, inputAmount);
                } else if (MovementDetailType.S.equals(movementDetailType)) {
                    outputAmount = amount;
                    residueAmount = BigDecimalUtil.subtract(residueAmount, outputAmount);
                }
            }
        }

        //set in report variables
        this.setVariableValue("inputQuantityVar", inputQuantity);
        this.setVariableValue("outputQuantityVar", outputQuantity);
        this.setVariableValue("residueQuantityVar", residueQuantity);
        this.setVariableValue("inputAmountVar", inputAmount);
        this.setVariableValue("outputAmountVar", outputAmount);
        this.setVariableValue("residueAmountVar", residueAmount);
    }


    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }

    private BigDecimal getVariableAsBigDecimal(String varName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getVariableValue(varName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }
}
