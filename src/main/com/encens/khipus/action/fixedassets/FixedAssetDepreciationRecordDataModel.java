package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetDepreciationRecord;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAssetDepreciationRecord
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetDepreciationRecordDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetDepreciationRecordDataModel extends QueryDataModel<Long, FixedAssetDepreciationRecord> {
    private static final String[] RESTRICTIONS =
            {"lower(fixedAssetDepreciationRecord.costCenterCode) like concat(lower(#{fixedAssetDepreciationRecordDataModel.criteria.costCenterCode}),'%')",
                    "lower(fixedAssetDepreciationRecord.custodian) like concat(lower(#{fixedAssetDepreciationRecordDataModel.criteria.custodian}),'%')",
                    "lower(fixedAssetDepreciationRecord.depreciationRate) like concat(lower(#{fixedAssetDepreciationRecordDataModel.criteria.depreciationRate}),'%')",
                    "fixedAssetDepreciationRecord.fixedAssetCode=#{fixedAssetDepreciationRecordDataModel.criteria.fixedAsset}",
                    "fixedAssetDepreciationRecord.depreciationDate=#{fixedAssetDepreciationRecordDataModel.criteria.depreciationDate}",
                    "fixedAssetDepreciationRecord.organizationalUnitCode=#{fixedAssetDepreciationRecordDataModel.criteria.businessUnit}"};

    @Create
    public void init() {
        sortProperty = "fixedAssetDepreciationRecord.fixedAssetCode";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetDepreciationRecord from FixedAssetDepreciationRecord fixedAssetDepreciationRecord";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}