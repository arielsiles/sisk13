package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for PurchaseOrderCause
 *
 * @author
 * @version 2.26
 */

@Name("purchaseOrderCauseDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PURCHASEORDERCAUSE','VIEW')}")
public class PurchaseOrderCauseDataModel extends QueryDataModel<Long, PurchaseOrderCause> {

    private static final String[] RESTRICTIONS =
            {
                    "lower(purchaseOrderCause.code) like concat(lower(#{purchaseOrderCauseDataModel.criteria.code}),'%')",
                    "lower(purchaseOrderCause.name) like concat(lower(#{purchaseOrderCauseDataModel.criteria.name}),'%')",
                    "lower(purchaseOrderCause.description) like concat(lower(#{purchaseOrderCauseDataModel.criteria.description}),'%')",
                    "purchaseOrderCause.requiresFixedAssets =#{purchaseOrderCauseDataModel.criteria.requiresFixedAssets}"
            };

    @Create
    public void init() {
        sortProperty = "purchaseOrderCause.name";
    }

    @Override
    public String getEjbql() {
        return "select purchaseOrderCause from PurchaseOrderCause purchaseOrderCause";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}