package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the depreciated fixed asset report action
 *
 * @author
 * @version 2.0.2
 */

@Name("depreciatedFixedAssetsReportAction")
@Scope(ScopeType.PAGE)
public class DepreciatedFixedAssetsReportAction extends GenericReportAction {
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private FixedAssetLocation fixedAssetLocation;
    private Long code;
    private String barCode;
    private String description;
    private Date initRegistrationDate;
    private Date endRegistrationDate;
    private BigDecimal initTotalValue;
    private BigDecimal endTotalValue;
    private BigDecimal initDepreciation;
    private BigDecimal endDepreciation;
    private BigDecimal initAccumulatedDepreciation;
    private BigDecimal endAccumulatedDepreciation;

    @Create
    public void init() {
        restrictions = new String[]{
                "businessUnit=#{depreciatedFixedAssetsReportAction.businessUnit}",
                "costCenter=#{depreciatedFixedAssetsReportAction.costCenter}",
                "fixedAssetGroup=#{depreciatedFixedAssetsReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{depreciatedFixedAssetsReportAction.fixedAssetSubGroup}",
                "fixedAssetLocation=#{depreciatedFixedAssetsReportAction.fixedAssetLocation}",
                "fixedAsset.fixedAssetCode = #{depreciatedFixedAssetsReportAction.code}",
                "lower(fixedAsset.barCode) like concat('%',concat(lower(#{depreciatedFixedAssetsReportAction.barCode}),'%'))",
                "lower(fixedAsset.description) like concat(lower(#{depreciatedFixedAssetsReportAction.description}),'%')",
                "fixedAsset.registrationDate>=#{depreciatedFixedAssetsReportAction.initRegistrationDate}",
                "fixedAsset.registrationDate<=#{depreciatedFixedAssetsReportAction.endRegistrationDate}",
                "fixedAsset.depreciationRate>=#{depreciatedFixedAssetsReportAction.initDepreciation}",
                "fixedAsset.depreciationRate<=#{depreciatedFixedAssetsReportAction.endDepreciation}",
                "(fixedAsset.ufvOriginalValue + fixedAsset.improvement)>=#{depreciatedFixedAssetsReportAction.initTotalValue}",
                "(fixedAsset.ufvOriginalValue + fixedAsset.improvement)<=#{depreciatedFixedAssetsReportAction.endTotalValue}",
                "fixedAsset.acumulatedDepreciation>=#{depreciatedFixedAssetsReportAction.initAccumulatedDepreciation}",
                "fixedAsset.acumulatedDepreciation<=#{depreciatedFixedAssetsReportAction.endAccumulatedDepreciation}",
                "fixedAsset.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.fixedassets.FixedAssetState','TDP')}"
        };

        sortProperty = "fixedAsset.fixedAssetSubGroup.id, fixedAsset.id";
    }

    protected String getEjbql() {
        return "SELECT fixedAsset.id, " +
                "      fixedAsset.description, " +
                "      fixedAssetLocation.name, " +
                "      fixedAsset.registrationDate, " +
                "      fixedAsset.depreciationRate, " +
                "      fixedAsset.ufvOriginalValue, " +
                "      fixedAsset.improvement, " +
                "      fixedAsset.acumulatedDepreciation, " +
                "      (fixedAsset.acumulatedDepreciation * fixedAsset.lastBsUfvRate), " +
                "      fixedAsset.fixedAssetSubGroup.id, " +
                "      fixedAsset.fixedAssetSubGroup.description, " +
                "      (fixedAsset.ufvOriginalValue + fixedAsset.improvement), " +
                "      (fixedAsset.bsOriginalValue + (fixedAsset.lastBsUfvRate * fixedAsset.improvement)), " +
                "      fixedAsset.barCode " +
                "FROM  FixedAsset as fixedAsset" +
                "      LEFT JOIN fixedAsset.businessUnit businessUnit" +
                "      LEFT JOIN fixedAsset.costCenter costCenter" +
                "      LEFT JOIN fixedAsset.fixedAssetLocation fixedAssetLocation" +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup";
    }

    public void generateReport() {
        log.debug("generating depreciatedFixedAssetReport......................................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "depreciatedFixedAssetReport",
                "/fixedassets/reports/depreciatedFixedAssetsReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("DepreciatedFixedAssets.report.title"),
                reportParameters);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        costCenter = null;
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

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getInitRegistrationDate() {
        return initRegistrationDate;
    }

    public void setInitRegistrationDate(Date initRegistrationDate) {
        this.initRegistrationDate = initRegistrationDate;
    }

    public Date getEndRegistrationDate() {
        return endRegistrationDate;
    }

    public void setEndRegistrationDate(Date endRegistrationDate) {
        this.endRegistrationDate = endRegistrationDate;
    }

    public BigDecimal getEndTotalValue() {
        return endTotalValue;
    }

    public void setEndTotalValue(BigDecimal endTotalValue) {
        this.endTotalValue = endTotalValue;
    }

    public BigDecimal getInitTotalValue() {
        return initTotalValue;
    }

    public void setInitTotalValue(BigDecimal initTotalValue) {
        this.initTotalValue = initTotalValue;
    }

    public BigDecimal getInitDepreciation() {
        return initDepreciation;
    }

    public void setInitDepreciation(BigDecimal initDepreciation) {
        this.initDepreciation = initDepreciation;
    }

    public BigDecimal getEndDepreciation() {
        return endDepreciation;
    }

    public void setEndDepreciation(BigDecimal endDepreciation) {
        this.endDepreciation = endDepreciation;
    }

    public BigDecimal getInitAccumulatedDepreciation() {
        return initAccumulatedDepreciation;
    }

    public void setInitAccumulatedDepreciation(BigDecimal initAccumulatedDepreciation) {
        this.initAccumulatedDepreciation = initAccumulatedDepreciation;
    }

    public BigDecimal getEndAccumulatedDepreciation() {
        return endAccumulatedDepreciation;
    }

    public void setEndAccumulatedDepreciation(BigDecimal endAccumulatedDepreciation) {
        this.endAccumulatedDepreciation = endAccumulatedDepreciation;
    }

    public FixedAssetLocation getFixedAssetLocation() {
        return fixedAssetLocation;
    }

    public void setFixedAssetLocation(FixedAssetLocation fixedAssetLocation) {
        this.fixedAssetLocation = fixedAssetLocation;
    }
}
