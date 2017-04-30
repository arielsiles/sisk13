package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetPayment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.24
 */
@Name("fixedAssetPaymentDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetPaymentDataModel extends QueryDataModel<Long, FixedAssetPayment> {
    private static final String[] RESTRICTIONS = {
    };

    @Create
    public void init() {
        sortProperty = "fixedAssetPayment.creationDate";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetPayment from FixedAssetPayment fixedAssetPayment";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}