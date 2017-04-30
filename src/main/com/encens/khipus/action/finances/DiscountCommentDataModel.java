package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.DiscountComment;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.fixedassets.FixedAssetVoucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for DiscountComment
 *
 * @author
 * @version 3.0
 */

@Name("discountCommentDataModel")
@Scope(ScopeType.PAGE)
public class DiscountCommentDataModel extends QueryDataModel<Long, DiscountComment> {
    private FixedAssetVoucher fixedAssetVoucher;
    private PurchaseOrder purchaseOrder;
    private RotatoryFund rotatoryFund;

    private static final String[] RESTRICTIONS =
            {
                    "discountComment.fixedAssetVoucher =#{discountCommentDataModel.fixedAssetVoucher}",
                    "discountComment.purchaseOrder =#{discountCommentDataModel.purchaseOrder}",
                    "discountComment.rotatoryFund =#{discountCommentDataModel.rotatoryFund}"
            };

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.DEFAULT_LIST.getName());
        sortProperty = "discountComment.code";
    }

    @Override
    public String getEjbql() {
        return "select discountComment from DiscountComment discountComment";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public Class<DiscountComment> getEntityClass() {
        return DiscountComment.class;
    }


    @SuppressWarnings({"UnusedDeclaration"})
    public void searchByFixedAssetVoucher() {
        FixedAssetVoucher fixedAssetVoucher = (FixedAssetVoucher) Component.getInstance("fixedAssetVoucher");
        setFixedAssetVoucher(fixedAssetVoucher);
        updateAndSearch();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void searchByFixedAssetPurchaseOrder() {
        PurchaseOrder purchaseOrder = (PurchaseOrder) Component.getInstance("fixedAssetPurchaseOrder");
        setPurchaseOrder(purchaseOrder);
        updateAndSearch();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void searchByWarehousePurchaseOrder() {
        PurchaseOrder purchaseOrder = (PurchaseOrder) Component.getInstance("warehousePurchaseOrder");
        setPurchaseOrder(purchaseOrder);
        updateAndSearch();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void searchByRotatoryFund() {
        RotatoryFund rotatoryFund = (RotatoryFund) Component.getInstance("rotatoryFund");
        setRotatoryFund(rotatoryFund);
        updateAndSearch();
    }


    public FixedAssetVoucher getFixedAssetVoucher() {
        return fixedAssetVoucher;
    }

    public void setFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher) {
        this.fixedAssetVoucher = fixedAssetVoucher;
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }
}