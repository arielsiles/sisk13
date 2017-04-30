package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetGroupPk;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroupPk;
import com.encens.khipus.util.CurrencyValuesContainer;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;

/**
 * FixedAssetDepreciationRecordService
 *
 * @author
 * @version 2.3
 */
@Local
public interface FixedAssetDepreciationRecordService extends GenericService {
    void createFixedAssetDepreciationRecord(FixedAsset fixedAsset, Date lastDayOfCurrentProcessMonth, BigDecimal lastDayOfMonthUfvExchangeRate);

    CurrencyValuesContainer getDepreciationAmountForGroupUpTo(FixedAssetGroupPk fixedAssetGroupId, Date dateRange);

    CurrencyValuesContainer getDepreciationAmountForGroupAndSubGroupUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                                         FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                         Date dateRange);

    CurrencyValuesContainer getDepreciationAmountForFixedAssetUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                                   FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                   Long fixedAssetId,
                                                                   Date dateRange);

    Double getBsDepreciationsSum(FixedAsset fixedAsset);
}
