package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.PurchaseDocument;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Name("accountingPurchaseDocumentDataModel")
@Scope(ScopeType.PAGE)
public class AccountingPurchaseDocumentDataModel extends QueryDataModel<Long, PurchaseDocument> {
    private static final String[] RESTRICTIONS =
            {"purchaseDocument.voucher = #{voucherUpdate}"};

    @Create
    public void init() {
        sortProperty = "purchaseDocument.date";
    }

    @Override
    public String getEjbql() {
        return "select purchaseDocument from PurchaseDocument purchaseDocument";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
