package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the fixed asset groups report action
 *
 * @author
 * @version 2.0.2
 */
@Name("fixedAssetGroupsReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetGroupsReportAction extends GenericReportAction {
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private BigDecimal initDepreciationRate;
    private BigDecimal endDepreciationRate;
    private Integer initDuration;
    private Integer endDuration;

    @Create
    public void init() {
        restrictions = new String[]{
                "fixedAssetGroup=#{fixedAssetGroupsReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{fixedAssetGroupsReportAction.fixedAssetSubGroup}",
                "fixedAssetSubGroup.depreciationRate>=#{fixedAssetGroupsReportAction.initDepreciationRate}",
                "fixedAssetSubGroup.depreciationRate<=#{fixedAssetGroupsReportAction.endDepreciationRate}",
                "fixedAssetSubGroup.duration>=#{fixedAssetGroupsReportAction.initDuration}",
                "fixedAssetSubGroup.duration<=#{fixedAssetGroupsReportAction.endDuration}"
        };

        sortProperty = "fixedAssetGroup.id";
    }

    protected String getEjbql() {
        return "SELECT fixedAssetGroup.id, " +
                "      fixedAssetGroup.description, " +
                "      fixedAssetSubGroup.id, " +
                "      fixedAssetSubGroup.description, " +
                "      fixedAssetSubGroup.depreciationRate, " +
                "      fixedAssetSubGroup.duration, " +
                "      fixedAssetSubGroup.originalValueCashAccount.accountCode, " +
                "      fixedAssetSubGroup.originalValueCashAccount.description, " +
                "      fixedAssetSubGroup.accumulatedDepreciationCashAccount.accountCode, " +
                "      fixedAssetSubGroup.accumulatedDepreciationCashAccount.description, " +
                "      fixedAssetSubGroup.expenseCashAccount.accountCode, " +
                "      fixedAssetSubGroup.expenseCashAccount.description " +
                "FROM  FixedAssetSubGroup as fixedAssetSubGroup" +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup";
    }

    public void generateReport() {
        log.debug("generating fixedAssetGroupsReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "fixedAssetGroupsReport",
                "/fixedassets/reports/fixedAssetGroupsReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("menu.finances.fixedassets.fixedAssetGroupsReport"),
                reportParameters);
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        if (null != fixedAssetGroup) {
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, fixedAssetGroup.getId());
            fixedAssetSubGroup = null;
        } else {
            this.fixedAssetGroup = null;
        }
    }

    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
        fixedAssetSubGroup = null;
    }

    public void clearFixedAssetSubGroup() {
        setFixedAssetSubGroup(null);
    }

    public FixedAssetSubGroup getFixedAssetSubGroup() {
        return fixedAssetSubGroup;
    }

    public void setFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        if (fixedAssetSubGroup != null) {
            this.fixedAssetSubGroup = getEntityManager().find(FixedAssetSubGroup.class, fixedAssetSubGroup.getId());
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, this.fixedAssetSubGroup.getFixedAssetGroup().getId());
        } else {
            this.fixedAssetSubGroup = null;
        }
    }

    public BigDecimal getInitDepreciationRate() {
        return initDepreciationRate;
    }

    public void setInitDepreciationRate(BigDecimal initDepreciationRate) {
        this.initDepreciationRate = initDepreciationRate;
    }

    public BigDecimal getEndDepreciationRate() {
        return endDepreciationRate;
    }

    public void setEndDepreciationRate(BigDecimal endDepreciationRate) {
        this.endDepreciationRate = endDepreciationRate;
    }

    public Integer getInitDuration() {
        return initDuration;
    }

    public void setInitDuration(Integer initDuration) {
        this.initDuration = initDuration;
    }

    public Integer getEndDuration() {
        return endDuration;
    }

    public void setEndDuration(Integer endDuration) {
        this.endDuration = endDuration;
    }
}
