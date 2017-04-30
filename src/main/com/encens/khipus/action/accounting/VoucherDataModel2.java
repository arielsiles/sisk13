package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.Voucher2;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * VoucherDataModel
 *
 * @author
 * @version 2.26
 */
@Name("voucherDataModel2")
@Scope(ScopeType.PAGE)
/*@Restrict("#{s:hasPermission('ORGANIZATION','VIEW')}")*/
public class VoucherDataModel2 extends QueryDataModel<Long, Voucher2> {

    private static final String[] RESTRICTIONS =
            {"lower(voucher.transactionNumber) like concat('%', concat(lower(#{voucherDataModel2.criteria.transactionNumber}), '%'))",
             "lower(voucher.gloss) like concat('%', concat(lower(#{voucherDataModel2.criteria.gloss}), '%'))",
             "lower(voucher.documentType) like concat('%', concat(lower(#{voucherDataModel2.criteria.documentType}), '%'))"};

    @Create
    public void init() {
        sortProperty = "voucher.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select voucher from Voucher2 voucher";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
