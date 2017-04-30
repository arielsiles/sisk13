package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.Voucher;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * VoucherDataModel
 *
 * @author
 * @version 2.26
 */
@Name("voucherDataModel")
@Scope(ScopeType.PAGE)
/*@Restrict("#{s:hasPermission('ORGANIZATION','VIEW')}")*/
public class VoucherDataModel extends QueryDataModel<Long, Voucher> {

    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS =
            {"lower(voucher.documentNumber) like concat('%', concat(lower(#{voucherDataModel.criteria.documentNumber}), '%'))",
             "lower(voucher.gloss) like concat('%', concat(lower(#{voucherDataModel.criteria.gloss}), '%'))",
             "lower(voucher.documentType) like concat('%', concat(lower(#{voucherDataModel.criteria.documentType}), '%'))",
             "voucher.date >= #{voucherDataModel.startDate}",
             "voucher.date <= #{voucherDataModel.endDate}"
            };

    @Create
    public void init() {
        sortProperty = "voucher.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select voucher from Voucher voucher";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void filterByDocumentNumber(String documentNumber){
        getCriteria().setDocumentType(documentNumber);
        updateAndSearch();
    }

    public void filterByDocumentType(String documentType){
        getCriteria().setDocumentType(documentType);
        updateAndSearch();
    }

    public void filterByGloss(String gloss){
        getCriteria().setGloss(gloss);
        updateAndSearch();
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
}
