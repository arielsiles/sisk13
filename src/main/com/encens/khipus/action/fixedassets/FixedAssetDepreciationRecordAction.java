package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAssetDepreciationRecord;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for FixedAssetDepreciationRecordAction
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetDepreciationRecordAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetDepreciationRecordAction extends GenericAction<FixedAssetDepreciationRecord> {

    @Factory(value = "fixedAssetDepreciationRecord", scope = ScopeType.STATELESS)
    public FixedAssetDepreciationRecord initFixedAssetDepreciationRecord() {
        /* by default the base currency for this entity is Bs
        * because this is the national currency the strongest currency type*/
        getInstance().setCurrency(FinancesCurrencyType.P);
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "month";
    }
}