package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetMovement;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.fixedassets.FixedAssetDepreciationRecordService;
import com.encens.khipus.service.fixedassets.FixedAssetMovementService;
import com.encens.khipus.util.BigDecimalUtil;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * This class implements the fixed asset file report scriptlet in order to calculate the improvement amount
 *
 * @author
 * @version 2.25
 */
public class FixedAssetFileReportScriptlet  extends JRDefaultScriptlet {

    private FinancesExchangeRateService financesExchangeRateService = (FinancesExchangeRateService) Component.getInstance("financesExchangeRateService");
    private FixedAssetMovementService fixedAssetMovementService = (FixedAssetMovementService) Component.getInstance("fixedAssetMovementService");
    private FixedAssetDepreciationRecordService fixedAssetDepreciationRecordService= (FixedAssetDepreciationRecordService) Component.getInstance("fixedAssetDepreciationRecordService");
    @Logger
    protected Log log;
    
    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        BigDecimal originalValue= (BigDecimal) this.getFieldValue("fixedAsset.ufvOriginalValue");
       if(originalValue!=null){
            try {
                BigDecimal ufvLastExchangeValue=financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name());
                Double currentOriginalValue=originalValue.doubleValue()*ufvLastExchangeValue.doubleValue();
                FixedAsset fixedAsset= (FixedAsset) this.getFieldValue("fixedAsset");
                if(fixedAsset!=null){
                    FixedAssetMovement approvedRegistrationMovement=fixedAssetMovementService.findApprovedRegistrationMovement(fixedAsset);
                    if(approvedRegistrationMovement!=null){
                        Double bsUfvRate=approvedRegistrationMovement.getBsUfvRate().doubleValue();
                        Double firstOriginalValue=originalValue.doubleValue()*bsUfvRate;
                        Double deltaOriginalValue=currentOriginalValue-firstOriginalValue;
                        Double bsAccumulatedDepreciationSum=fixedAssetDepreciationRecordService.getBsDepreciationsSum(fixedAsset);
                        if(bsAccumulatedDepreciationSum!=null){
                            BigDecimal accumulatedDepreciation= (BigDecimal) this.getFieldValue("fixedAsset.accumulatedDepreciation");
                            if(accumulatedDepreciation!=null){
                                Double bsAccumulatedDepreciation=(accumulatedDepreciation.doubleValue()*ufvLastExchangeValue.doubleValue());
                                Double res=deltaOriginalValue-(bsAccumulatedDepreciation-bsAccumulatedDepreciationSum);
                                this.setVariableValue("updatingValueVar", BigDecimalUtil.roundBigDecimal(new BigDecimal(res)));
                            }
                        }
                    }
                }
            } catch (FinancesCurrencyNotFoundException e) {
                log.error("Currency not found... ",e);
            } catch (FinancesExchangeRateNotFoundException e) {
                log.error("ExchangeRate not found",e);
            }
       }
    }


}
