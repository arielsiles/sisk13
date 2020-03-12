package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.PurchaseDocument;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Name("purchaseDocumentDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PURCHASEDOCUMENT','VIEW')}")
public class PurchaseDocumentDataModel extends QueryDataModel<Long, PurchaseDocument> {

    private Date startDate;
    private Date endDate;
    private String number;

    private static final String[] RESTRICTIONS = {
            "purchaseDocument.number = #{purchaseDocumentDataModel.number}",
            "purchaseDocument.date >= #{purchaseDocumentDataModel.startDate}",
            "purchaseDocument.date <= #{purchaseDocumentDataModel.endDate}"
    };

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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
