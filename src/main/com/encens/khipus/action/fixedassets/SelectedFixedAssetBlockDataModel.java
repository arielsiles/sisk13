package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAsset;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAsset
 *
 * @author
 * @version 2.24
 */

@Name("selectedFixedAssetBlockDataModel")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('FIXEDASSET','VIEW')}")
public class SelectedFixedAssetBlockDataModel extends QueryDataModel<Long, FixedAsset> {
    private static final String[] RESTRICTIONS =
            {
                    "fixedAsset.id in (#{fixedAssetVoucherAction.selectedFixedAssetIdList})"
            };

    @Create
    public void init() {
        /* this dataModel needs to work with listEm because all the fixedAssets involved in a voucher needs to be rendered
        * to identify them and let the user drop from selection */
        sortProperty = "fixedAsset.fixedAssetCode";
    }

    @Override
    public String getEjbql() {
        return "select fixedAsset from FixedAsset fixedAsset" +
                " left join fetch fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                " left join fetch fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                " left join fetch fixedAsset.businessUnit businessUnit" +
                " left join fetch fixedAsset.costCenter costCenter" +
                " left join fetch fixedAsset.custodianJobContract custodianJobContract " +
                " left join fetch custodianJobContract.contract contract" +
                " left join fetch contract.employee employee";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}