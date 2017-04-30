package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroupPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAssetSubGroup
 *
 * @author
 * @version 2.26
 */

@Name("fixedAssetSubGroupDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','VIEW')}")
public class FixedAssetSubGroupDataModel extends QueryDataModel<FixedAssetSubGroupPk, FixedAssetSubGroup> {
    private FixedAssetGroup fixedAssetGroup;
    private CashAccount accumulatedDepreciationCashAccount;
    private CashAccount originalValueCashAccount;
    private CashAccount expenseCashAccount;
    private String fixedAssetSubGroupCode;
    private String groupCode;

    private static final String[] RESTRICTIONS = {
            "lower(fixedAssetSubGroup.id.fixedAssetSubGroupCode) like concat(lower(#{fixedAssetSubGroupDataModel.fixedAssetSubGroupCode}), '%')",
            "fixedAssetSubGroup.fixedAssetGroup = #{fixedAssetSubGroupDataModel.fixedAssetGroup}",
            "fixedAssetSubGroup.duration = #{fixedAssetSubGroupDataModel.criteria.duration}",
            "fixedAssetSubGroup.depreciationRate = #{fixedAssetSubGroupDataModel.criteria.depreciationRate}",
            "lower(fixedAssetSubGroup.description) like concat('%', concat(lower( #{fixedAssetSubGroupDataModel.criteria.description}), '%'))",
            "fixedAssetSubGroup.originalValueCashAccount = #{fixedAssetSubGroupDataModel.originalValueCashAccount}",
            "fixedAssetSubGroup.accumulatedDepreciationCashAccount = #{fixedAssetSubGroupDataModel.accumulatedDepreciationCashAccount}",
            "fixedAssetSubGroup.expenseCashAccount = #{fixedAssetSubGroupDataModel.expenseCashAccount}",
            "fixedAssetGroup.groupCode=#{fixedAssetSubGroupDataModel.groupCode}",
            "fixedAssetGroup=#{fixedAssetPurchaseOrderDetailAction.fixedAssetGroup}",
            "fixedAssetGroup=#{fixedAssetAction.fixedAssetGroup}",
            "fixedAssetGroup=#{fixedAssetDataModel.fixedAssetGroup}",
            "fixedAssetGroup=#{fixedAssetsReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{fixedAssetGroupsReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{depreciatedFixedAssetsReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{fixedAssetFileReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{fixedAssetByCustodianReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{depreciationSummaryReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{accumulatedDepreciationSummaryByFixedAssetReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{evolutionBySubGroupSummaryReportAction.fixedAssetGroup}",
            "fixedAssetGroup=#{evolutionByFixedAssetSummaryReportAction.fixedAssetGroup}",
            "fixedAssetSubGroup.originalValueAccount = #{fixedAssetSubGroupDataModel.criteria.originalValueAccount}",
            "fixedAssetSubGroup.accumulatedDepreciationAccount = #{fixedAssetSubGroupDataModel.criteria.accumulatedDepreciationAccount}"
    };

    @Create
    public void init() {
        sortProperty = "fixedAssetSubGroup.id.fixedAssetSubGroupCode";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetSubGroup from FixedAssetSubGroup fixedAssetSubGroup " +
                " left join fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void clearAccumulatedDepreciationCashAccount() {
        setAccumulatedDepreciationCashAccount(null);
    }

    public void clearExpenseCashAccount() {
        setExpenseCashAccount(null);
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
    }

    public CashAccount getAccumulatedDepreciationCashAccount() {
        return accumulatedDepreciationCashAccount;
    }

    public void setAccumulatedDepreciationCashAccount(CashAccount accumulatedDepreciationCashAccount) {
        this.accumulatedDepreciationCashAccount = accumulatedDepreciationCashAccount;
    }

    public CashAccount getExpenseCashAccount() {
        return expenseCashAccount;
    }

    public void setExpenseCashAccount(CashAccount expenseCashAccount) {
        this.expenseCashAccount = expenseCashAccount;
    }

    public CashAccount getOriginalValueCashAccount() {
        return originalValueCashAccount;
    }

    public void setOriginalValueCashAccount(CashAccount originalValueCashAccount) {
        this.originalValueCashAccount = originalValueCashAccount;
    }

    public String getFixedAssetSubGroupCode() {
        return fixedAssetSubGroupCode;
    }

    public void setFixedAssetSubGroupCode(String fixedAssetSubGroupCode) {
        this.fixedAssetSubGroupCode = fixedAssetSubGroupCode;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
    }

    @Override
    public void clear() {
        fixedAssetGroup = null;
        accumulatedDepreciationCashAccount = null;
        originalValueCashAccount = null;
        expenseCashAccount = null;
        fixedAssetSubGroupCode = null;
        groupCode = null;
        super.clear();
    }
}