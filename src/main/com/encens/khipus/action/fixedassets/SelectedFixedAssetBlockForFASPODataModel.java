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
 * Data model for FixedAsset in FixedAssetPurchaseOrder context
 *
 * @author
 * @version 2.26
 */

@Name("selectedFixedAssetBlockForFASPODataModel")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('FIXEDASSET','VIEW')}")
public class SelectedFixedAssetBlockForFASPODataModel extends QueryDataModel<Long, FixedAsset> {
    private static final String[] RESTRICTIONS =
            {
                    "fixedAsset.id in (#{fixedAssetPurchaseOrderAction.selectedFixedAssetIdList})"
            };

    @Create
    public void init() {
        /* this dataModel needs to work with listEm because all the fixedAssets involved in a voucher needs to be rendered
        * to identify them and let the user drop from selection */
        sortProperty = "fixedAsset.fixedAssetCode";
    }

    @Override
    public String getEjbql() {
        return "select fixedAsset from FixedAsset fixedAsset";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}