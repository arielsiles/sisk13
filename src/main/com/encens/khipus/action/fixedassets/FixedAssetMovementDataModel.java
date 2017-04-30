package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetMovement;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAssetMovement
 *
 * @author
 * @version 2.25
 */

@Name("fixedAssetMovementDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetMovementDataModel extends QueryDataModel<Long, FixedAssetMovement> {

    private static final String[] RESTRICTIONS =
            {
                    "lower(fixedAssetMovement.custodian) like concat(lower(#{fixedAssetMovementDataModel.criteria.custodian}),'%')",
                    "lower(fixedAssetMovement.costCenterCode) like concat(lower(#{fixedAssetMovementDataModel.criteria.costCenterCode}),'%')",
                    "fixedAssetMovement.businessUnit=#{fixedAssetMovementDataModel.criteria.businessUnit}",
                    "lower(fixedAssetMovement.lastCustodian) like concat(lower(#{fixedAssetMovementDataModel.criteria.lastCustodian}),'%')",
                    "lower(fixedAssetMovement.lastCostCenterCode) like concat(lower(#{fixedAssetMovementDataModel.criteria.lastCostCenterCode}),'%')",
                    "fixedAssetMovement.lastBusinessUnit=#{fixedAssetMovementDataModel.criteria.lastBusinessUnit}",
                    "fixedAssetMovement.movementDate) >= #{fixedAssetMovementDataModel.criteria.movementDate}",
                    "lower(fixedAssetMovement.voucherNumber) like concat(lower(#{fixedAssetMovementDataModel.criteria.voucherNumber}),'%')",
                    "fixedAssetMovement.fixedAssetVoucher =#{fixedAssetVoucher}",
            };

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "fixedAssetMovement.movementDate";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetMovement from FixedAssetMovement fixedAssetMovement";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}