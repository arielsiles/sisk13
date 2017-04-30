package com.encens.khipus.action.fixedassets.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.util.fixedassets.FixedAssetTransferenceInventoryReportOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * @author
 * @version 2.29
 */
@Name("fixedAssetTransferenceInventoryReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetTransferenceInventoryReportAction extends GenericReportAction {
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private FixedAssetLocation fixedAssetLocation;
    private FixedAssetLocation lastFixedAssetLocation;
    private Long initFixedAssetCode;
    private Long endFixedAssetCode;
    private String barCode;
    private Date initMovementDate;
    private Date endMovementDate;
    private Date initRegistrationDate;
    private Date endRegistrationDate;
    private BigDecimal initOriginalValue;
    private BigDecimal endOriginalValue;
    private Boolean showAmounts = true;
    private FixedAssetTransferenceInventoryReportOrder reportOrder = FixedAssetTransferenceInventoryReportOrder.MOVEMENT_DATE;

    @Create
    public void init() {
        restrictions = new String[]{
                "fixedAssetMovementType.fixedAssetMovementTypeEnum=#{enumerationUtil.getEnumValue('com.encens.khipus.model.fixedassets.FixedAssetMovementTypeEnum','TRA')}",
                "fixedAssetMovement.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.fixedassets.FixedAssetMovementState','APR')}",
                "fixedAssetGroup=#{fixedAssetTransferenceInventoryReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{fixedAssetTransferenceInventoryReportAction.fixedAssetSubGroup}",
                "fixedAssetMovement.newFixedAssetLocation=#{fixedAssetTransferenceInventoryReportAction.fixedAssetLocation}",
                "fixedAssetMovement.lastFixedAssetLocation=#{fixedAssetTransferenceInventoryReportAction.lastFixedAssetLocation}",
                "fixedAsset.fixedAssetCode >= #{fixedAssetTransferenceInventoryReportAction.initFixedAssetCode}",
                "fixedAsset.fixedAssetCode <= #{fixedAssetTransferenceInventoryReportAction.endFixedAssetCode}",
                "lower(fixedAsset.barCode) like concat('%',concat(lower(#{fixedAssetTransferenceInventoryReportAction.barCode}),'%'))",
                "fixedAssetMovement.movementDate>=#{fixedAssetTransferenceInventoryReportAction.initMovementDate}",
                "fixedAssetMovement.movementDate<=#{fixedAssetTransferenceInventoryReportAction.endMovementDate}",
                "fixedAsset.registrationDate>=#{fixedAssetTransferenceInventoryReportAction.initRegistrationDate}",
                "fixedAsset.registrationDate<=#{fixedAssetTransferenceInventoryReportAction.endRegistrationDate}",
                "fixedAsset.ufvOriginalValue>=#{fixedAssetTransferenceInventoryReportAction.initOriginalValue}",
                "fixedAsset.ufvOriginalValue<=#{fixedAssetTransferenceInventoryReportAction.endOriginalValue}"
        };

    }

    protected String getEjbql() {
        return "SELECT " +
                "      fixedAssetMovement.movementDate, " +
                "      fixedAssetSubGroup.fixedAssetSubGroupCode, " +
                "      fixedAssetSubGroup.description, " +
                "      fixedAssetGroup.groupCode, " +
                "      fixedAssetGroup.description, " +
                "      fixedAsset.fixedAssetCode, " +
                "      fixedAsset.barCode, " +
                "      fixedAsset.description, " +
                "      fixedAsset.sequence, " +
                "      fixedAsset.trademark, " +
                "      fixedAsset.model, " +
                "      lastCustodian, " +
                "      lastBusinessUnit.executorUnitCode, " +
                "      lastBusinessUnit.publicity, " +
                "      lastCostCenter.code, " +
                "      lastCostCenter.description, " +
                "      lastFixedAssetLocation.name, " +
                "      newCustodian, " +
                "      newBusinessUnit.executorUnitCode, " +
                "      newBusinessUnit.publicity, " +
                "      newCostCenter.code, " +
                "      newCostCenter.description, " +
                "      newFixedAssetLocation.name, " +
                "      (fixedAsset.bsOriginalValue+(fixedAsset.improvement * fixedAsset.lastBsUfvRate)), " +
                "      (fixedAsset.bsOriginalValue+(fixedAsset.improvement * fixedAsset.lastBsUfvRate))-(fixedAsset.acumulatedDepreciation * fixedAsset.lastBsUfvRate), " +
                "      fixedAssetMovement.id " +
                " FROM FixedAssetMovement as fixedAssetMovement" +
                "      LEFT JOIN fixedAssetMovement.fixedAssetMovementType as fixedAssetMovementType" +
                "      LEFT JOIN fixedAssetMovement.fixedAsset as fixedAsset" +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                "      LEFT JOIN fixedAssetMovement.lastCustodian lastCustodian" +
                "      LEFT JOIN fixedAssetMovement.lastBusinessUnit lastBusinessUnit" +
                "      LEFT JOIN fixedAssetMovement.lastCostCenter lastCostCenter" +
                "      LEFT JOIN fixedAssetMovement.lastFixedAssetLocation lastFixedAssetLocation" +
                "      LEFT JOIN fixedAssetMovement.custodian newCustodian" +
                "      LEFT JOIN fixedAssetMovement.businessUnit newBusinessUnit" +
                "      LEFT JOIN fixedAssetMovement.costCenter newCostCenter" +
                "      LEFT JOIN fixedAssetMovement.newFixedAssetLocation newFixedAssetLocation"
                ;
    }

    @Restrict("#{s:hasPermission('FIXEDASSETTRANSFERENCEINVENTORYREPORT','VIEW')}")
    public void generateReport() {
        log.debug("generating FixedAssetTransferenceInventoryReport......................................");
        sortProperty = getReportOrder().getOrder();

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("SHOW_AMOUNTS", getShowAmounts());
        super.generateReport(
                "fixedAssetReport",
                "/fixedassets/reports/fixedAssetTransferenceInventoryReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("FixedAssetTransferenceInventory.report.title"),
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

    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
        fixedAssetSubGroup = null;
    }

    public void clearFixedAssetSubGroup() {
        setFixedAssetSubGroup(null);
    }

    public Long getInitFixedAssetCode() {
        return initFixedAssetCode;
    }

    public void setInitFixedAssetCode(Long initFixedAssetCode) {
        this.initFixedAssetCode = initFixedAssetCode;
    }

    public Long getEndFixedAssetCode() {
        return endFixedAssetCode;
    }

    public void setEndFixedAssetCode(Long endFixedAssetCode) {
        this.endFixedAssetCode = endFixedAssetCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Date getInitMovementDate() {
        return initMovementDate;
    }

    public void setInitMovementDate(Date initMovementDate) {
        this.initMovementDate = initMovementDate;
    }

    public Date getEndMovementDate() {
        return endMovementDate;
    }

    public void setEndMovementDate(Date endMovementDate) {
        this.endMovementDate = endMovementDate;
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

    public BigDecimal getInitOriginalValue() {
        return initOriginalValue;
    }

    public void setInitOriginalValue(BigDecimal initOriginalValue) {
        this.initOriginalValue = initOriginalValue;
    }

    public BigDecimal getEndOriginalValue() {
        return endOriginalValue;
    }

    public void setEndOriginalValue(BigDecimal endOriginalValue) {
        this.endOriginalValue = endOriginalValue;
    }

    public Boolean getShowAmounts() {
        if (showAmounts == null) {
            showAmounts = false;
        }
        return showAmounts;
    }

    public void setShowAmounts(Boolean showAmounts) {
        this.showAmounts = showAmounts;
    }

    public FixedAssetTransferenceInventoryReportOrder getReportOrder() {
        return reportOrder;
    }

    public void setReportOrder(FixedAssetTransferenceInventoryReportOrder reportOrder) {
        this.reportOrder = reportOrder;
    }

    public FixedAssetLocation getFixedAssetLocation() {
        return fixedAssetLocation;
    }

    public void setFixedAssetLocation(FixedAssetLocation fixedAssetLocation) {
        this.fixedAssetLocation = fixedAssetLocation;
    }

    public FixedAssetLocation getLastFixedAssetLocation() {
        return lastFixedAssetLocation;
    }

    public void setLastFixedAssetLocation(FixedAssetLocation lastFixedAssetLocation) {
        this.lastFixedAssetLocation = lastFixedAssetLocation;
    }
}
