package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.DiscountCommentNotFoundException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.exception.fixedassets.FixedAssetVoucherAnnulledException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.DiscountComment;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;

/**
 * @author
 * @version 0.3
 */
@Local
public interface DiscountCommentService extends GenericService {

    void createDiscountComment(DiscountComment discountComment)
            throws EntryDuplicatedException, FixedAssetVoucherAnnulledException, PurchaseOrderNullifiedException, RotatoryFundNullifiedException;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    void updateDiscountComment(DiscountComment discountComment)
            throws
            FixedAssetVoucherAnnulledException, RotatoryFundNullifiedException,
            PurchaseOrderNullifiedException, ConcurrencyException, EntryDuplicatedException, DiscountCommentNotFoundException;

    /**
     * Finds with event entity manager a DiscountComment
     *
     * @param id the id which identifies the DiscountComment
     * @return the database DiscountComment instance
     */
    DiscountComment findInDataBase(Long id) throws DiscountCommentNotFoundException;

    void deleteDiscountComment(DiscountComment discountComment)
            throws RotatoryFundNullifiedException, PurchaseOrderNullifiedException,
            FixedAssetVoucherAnnulledException, ReferentialIntegrityException, ConcurrencyException, DiscountCommentNotFoundException;

    DiscountComment findDiscountComment(Long id) throws DiscountCommentNotFoundException;

    @SuppressWarnings({"unchecked"})
    List<Object[]> findCauseByPurchaseOrderId(Long purchaseOrderId);

    @SuppressWarnings({"unchecked"})
    List<Object[]> findCauseByRotatoryFundId(Long rotatoryFundId);
}
