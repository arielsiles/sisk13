package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetVoucher;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Data model for FixedAssetVoucher
 *
 * @author
 * @version 2.24
 */

@Name("fixedAssetVoucherDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetVoucherDataModel extends QueryDataModel<Long, FixedAssetVoucher> {

    private FixedAsset fixedAsset;
    private CostCenter costCenter;
    private Employee responsible;
    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS =
            {
                    "lower(fixedAssetVoucher.custodianJobContract) like concat(lower(#{fixedAssetVoucherDataModel.criteria.custodianJobContract}),'%')",
                    "lower(fixedAssetVoucher.costCenterCode) like concat(lower(#{fixedAssetVoucherDataModel.criteria.costCenterCode}),'%')",
                    "fixedAssetVoucher.businessUnit=#{fixedAssetVoucherDataModel.criteria.businessUnit}",
                    "fixedAssetVoucher.movementDate) >= #{fixedAssetVoucherDataModel.criteria.movementDate}",
                    "lower(fixedAssetVoucher.voucherCode) like concat('%', concat(lower(#{fixedAssetVoucherDataModel.criteria.voucherCode}),'%'))",
                    "lower(fixedAssetVoucher.cause) like concat('%', concat(lower(#{fixedAssetVoucherDataModel.criteria.cause}),'%'))",
                    "fixedAssetVoucher.state=#{fixedAssetVoucherDataModel.criteria.state}",
                    "fixedAssetVoucher.fixedAssetVoucherType=#{fixedAssetVoucherDataModel.criteria.fixedAssetVoucherType}",
                    "costCenter=#{fixedAssetVoucherDataModel.costCenter}",
                    "fixedAssetVoucher.createdById=#{fixedAssetVoucherDataModel.responsible.id}",
                    "fixedAssetVoucher.movementDate>=#{fixedAssetVoucherDataModel.startDate}",
                    "fixedAssetVoucher.movementDate<=#{fixedAssetVoucherDataModel.endDate}",
                    "fixedAssetVoucher.id in (select fixedAssetVoucher.id" +
                            " from FixedAssetMovement movement" +
                            " left join movement.fixedAsset fixedAsset" +
                            " left join movement.fixedAssetVoucher fixedAssetVoucher" +
                            " where fixedAsset=#{fixedAssetVoucherDataModel.fixedAsset})"

            };

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "fixedAssetVoucher.voucherCode";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetVoucher from FixedAssetVoucher fixedAssetVoucher " +
                " left join fetch fixedAssetVoucher.custodianJobContract custodianJobContract" +
                " left join fetch custodianJobContract.contract contract" +
                " left join fetch contract.employee employee" +
                " left join fetch fixedAssetVoucher.costCenter costCenter";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public void clearFixedAsset() {
        setFixedAsset(null);
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public void cleanResponsible() {
        setResponsible(null);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public void clear() {
        setFixedAsset(null);
        setCostCenter(null);
        setResponsible(null);
        setStartDate(null);
        setEndDate(null);
        super.clear();
    }
}