package com.encens.khipus.action.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAssetGroupPk;
import com.encens.khipus.model.fixedassets.FixedAssetMovementTypeEnum;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroupPk;
import com.encens.khipus.service.fixedassets.FixedAssetDepreciationRecordService;
import com.encens.khipus.service.fixedassets.FixedAssetMovementService;
import com.encens.khipus.util.CurrencyValuesContainer;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Encens S.R.L.
 * This class implements the fixed asset evolution summary by sub group report scriptlet, his gets the positive movements and negative movements
 * and depreciations for the fixedAssetGroups.
 *
 * @author
 * @version 2.25
 */
public class EvolutionBySubGroupSummaryReportScriptlet extends JRDefaultScriptlet {
    private FixedAssetMovementService fixedAssetMovementService =
            (FixedAssetMovementService) Component.getInstance("fixedAssetMovementService");

    private FixedAssetDepreciationRecordService fixedAssetDepreciationRecordService =
            (FixedAssetDepreciationRecordService) Component.getInstance("fixedAssetDepreciationRecordService");

    private EvolutionBySubGroupSummaryReportAction evolutionBySubGroupSummaryReportAction =
            (EvolutionBySubGroupSummaryReportAction) Component.getInstance("evolutionBySubGroupSummaryReportAction");


    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        //SubGroupId
        FixedAssetGroupPk fixedAssetGroupPk = (FixedAssetGroupPk) this.getFieldValue("fixedAssetGroup.id");
        FixedAssetSubGroupPk fixedAssetSubGroupPk = (FixedAssetSubGroupPk) this.getFieldValue("fixedAssetSubGroup.id");
        //If init and end date don't exists, then use min and max date
        Date initDate = evolutionBySubGroupSummaryReportAction.getInitDateRange();
        Date endDate = evolutionBySubGroupSummaryReportAction.getEndDateRange();
        if (initDate == null) {
            initDate = new Date(Calendar.getInstance().getMinimum(Calendar.YEAR), Calendar.JANUARY, 1);
        }
        if (endDate == null) {
            endDate = new Date(Calendar.getInstance().getMaximum(Calendar.YEAR), Calendar.DECEMBER, 29);
        }
        //Get previousRemainder
        CurrencyValuesContainer movementsSumValues = fixedAssetMovementService.getMovementsSumBySubGroupUpTo(fixedAssetGroupPk, fixedAssetSubGroupPk, initDate);
        BigDecimal ufvMovementsSum = movementsSumValues.getUfvValue();
        BigDecimal bsMovementsSum = movementsSumValues.getBsValue();

        if (ufvMovementsSum == null) {
            ufvMovementsSum = BigDecimal.ZERO;
        }
        if (bsMovementsSum == null) {
            bsMovementsSum = BigDecimal.ZERO;
        }
        //Get depreciations
        CurrencyValuesContainer depreciationsContainer = fixedAssetDepreciationRecordService.getDepreciationAmountForGroupAndSubGroupUpTo(fixedAssetGroupPk,
                fixedAssetSubGroupPk,
                initDate);
        BigDecimal ufvDepreciation = depreciationsContainer.getUfvValue();
        BigDecimal bsDepreciation = depreciationsContainer.getBsValue();
        if (ufvDepreciation != null) {
            ufvMovementsSum = ufvMovementsSum.subtract(ufvDepreciation);
        }
        if (bsDepreciation != null) {
            bsMovementsSum = bsMovementsSum.subtract(bsDepreciation);
        }

        this.setVariableValue("ufvPreviousRemainder", ufvMovementsSum);
        this.setVariableValue("bsPreviousRemainder", bsMovementsSum);

        //For total value
        BigDecimal ufvTotalAmount = ufvMovementsSum;
        BigDecimal bsTotalAmount = bsMovementsSum;

        //Positive movements (Registration)
        CurrencyValuesContainer positiveMovementsSumValues = fixedAssetMovementService.getMovementsSumByGroupAndSubGroup(fixedAssetGroupPk, fixedAssetSubGroupPk,
                initDate, endDate, FixedAssetMovementTypeEnum.ALT);
        BigDecimal ufvPositiveMovementsSum = positiveMovementsSumValues.getUfvValue();
        BigDecimal bsPositiveMovementsSum = positiveMovementsSumValues.getBsValue();

        if (ufvPositiveMovementsSum != null) {
            this.setVariableValue("ufvPositiveMovementsAmount", ufvPositiveMovementsSum);
            ufvTotalAmount = ufvTotalAmount.add(ufvPositiveMovementsSum);
        } else {
            this.setVariableValue("ufvPositiveMovementsAmount", BigDecimal.ZERO);
        }

        if (bsPositiveMovementsSum != null) {
            this.setVariableValue("bsPositiveMovementsAmount", bsPositiveMovementsSum);
            bsTotalAmount = bsTotalAmount.add(bsPositiveMovementsSum);
        } else {
            this.setVariableValue("bsPositiveMovementsAmount", BigDecimal.ZERO);
        }
        //Negative movements (discharge)
        CurrencyValuesContainer negativeMovementsSumValues = fixedAssetMovementService.getMovementsSumByGroupAndSubGroup(fixedAssetGroupPk, fixedAssetSubGroupPk,
                initDate, endDate, FixedAssetMovementTypeEnum.BAJ);
        BigDecimal ufvNegativeMovementsSum = negativeMovementsSumValues.getUfvValue();
        BigDecimal bsNegativeMovementsSum = negativeMovementsSumValues.getBsValue();
        if (ufvNegativeMovementsSum != null) {
            this.setVariableValue("ufvNegativeMovementsAmount", ufvNegativeMovementsSum);
            ufvTotalAmount = ufvTotalAmount.subtract(ufvNegativeMovementsSum);
        } else {
            this.setVariableValue("ufvNegativeMovementsAmount", BigDecimal.ZERO);
        }

        if (bsNegativeMovementsSum != null) {
            this.setVariableValue("bsNegativeMovementsAmount", bsNegativeMovementsSum);
            bsTotalAmount = bsTotalAmount.subtract(bsNegativeMovementsSum);
        } else {
            this.setVariableValue("bsNegativeMovementsAmount", BigDecimal.ZERO);
        }

        this.setVariableValue("ufvTotalAmount", ufvTotalAmount);
        this.setVariableValue("bsTotalAmount", bsTotalAmount);
    }
}