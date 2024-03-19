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
 *
 *
 * @author
 * @version 2.26
 */
@Name("diaryBookDataModel")
@Scope(ScopeType.PAGE)
public class DiaryBookDataModel extends QueryDataModel<Long, Voucher> {

    private Date startDate;
    private Date endDate;
    private String documentNumber;

    private static final String[] RESTRICTIONS =
            {"voucher.documentNumber = #{diaryBookDataModel.documentNumber}",
             "lower(voucher.documentType) like concat('%', concat(lower(#{diaryBookDataModel.criteria.documentType}), '%'))",
             "voucher.date >= #{diaryBookDataModel.startDate}",
             "voucher.date <= #{diaryBookDataModel.endDate}"
            };

    @Create
    public void init() {
        sortProperty = "voucher.date, voucher.id";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return  "select voucher from Voucher voucher " +
                "where voucher.state <> 'ANL'";
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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}
