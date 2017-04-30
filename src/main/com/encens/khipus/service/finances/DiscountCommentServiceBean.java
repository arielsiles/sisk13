package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.DiscountCommentNotFoundException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.exception.fixedassets.FixedAssetVoucherAnnulledException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.DiscountComment;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.fixedassets.FixedAssetVoucherService;
import com.encens.khipus.service.purchases.PurchaseOrderService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 0.3
 */
@Stateless
@Name("discountCommentService")
@AutoCreate
public class DiscountCommentServiceBean extends GenericServiceBean implements DiscountCommentService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In(required = false)
    private User currentUser;

    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private FixedAssetVoucherService fixedAssetVoucherService;
    @In
    private PurchaseOrderService purchaseOrderService;
    @In
    private RotatoryFundService rotatoryFundService;

    @Override
    public void createDiscountComment(DiscountComment discountComment)
            throws EntryDuplicatedException, FixedAssetVoucherAnnulledException,
            PurchaseOrderNullifiedException, RotatoryFundNullifiedException {
        try {
            validate(discountComment);
            discountComment.setCode(sequenceGeneratorService.nextValue(Constants.DISCOUNTCOMMENT_CODE_SEQUENCE));
            discountComment.setCreatedBy(currentUser);
            discountComment.setCreationDate(new Date());
            getEntityManager().persist(discountComment);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateDiscountComment(DiscountComment discountComment)
            throws
            FixedAssetVoucherAnnulledException, RotatoryFundNullifiedException,
            PurchaseOrderNullifiedException, ConcurrencyException, EntryDuplicatedException, DiscountCommentNotFoundException {
        DiscountComment dbDiscountComment;
        try {
            dbDiscountComment = findInDataBase(discountComment.getId());
        } catch (DiscountCommentNotFoundException e) {
            detach(discountComment);
            throw e;
        }
        validate(discountComment);
        try {
            discountComment.setUpdatedBy(currentUser);
            discountComment.setUpdateDate(new Date());
            getEntityManager().merge(discountComment);
            getEntityManager().flush();
            getEntityManager().refresh(discountComment);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }

    @Override
    public void deleteDiscountComment(DiscountComment discountComment)
            throws RotatoryFundNullifiedException, PurchaseOrderNullifiedException,
            FixedAssetVoucherAnnulledException, ReferentialIntegrityException,
            ConcurrencyException, DiscountCommentNotFoundException {
        findInDataBase(discountComment.getId());
        validate(discountComment);
        super.delete(discountComment);
    }

    private void validate(DiscountComment discountComment)
            throws FixedAssetVoucherAnnulledException, PurchaseOrderNullifiedException, RotatoryFundNullifiedException {
        if (null != discountComment.getFixedAssetVoucher()
                && fixedAssetVoucherService.isFixedAssetVoucherNullified(discountComment.getFixedAssetVoucher())) {
            throw new FixedAssetVoucherAnnulledException();
        } else if (null != discountComment.getPurchaseOrder()
                && purchaseOrderService.isPurchaseOrderNullified(discountComment.getPurchaseOrder())) {
            throw new PurchaseOrderNullifiedException();
        } else if (null != discountComment.getRotatoryFund()
                && rotatoryFundService.isRotatoryFundNullified(discountComment.getRotatoryFund())) {
            throw new RotatoryFundNullifiedException();
        }
    }

    /**
     * Finds with event entity manager a DiscountComment
     *
     * @param id the id which identifies the DiscountComment
     * @return the database DiscountComment instance
     */
    public DiscountComment findInDataBase(Long id) throws DiscountCommentNotFoundException {
        DiscountComment discountComment = eventEm.find(DiscountComment.class, id);
        if (null == discountComment) {
            throw new DiscountCommentNotFoundException();
        }
        return discountComment;
    }

    public DiscountComment findDiscountComment(Long id) throws DiscountCommentNotFoundException {
        findInDataBase(id);
        DiscountComment discountComment = getEntityManager().find(DiscountComment.class, id);
        getEntityManager().refresh(discountComment);
        return discountComment;
    }

    @SuppressWarnings({"unchecked"})
    public List<Object[]> findCauseByPurchaseOrderId(Long purchaseOrderId) {
        return eventEm.createNamedQuery("DiscountComment.findCauseByPurchaseOrderId")
                .setParameter("purchaseOrderId", purchaseOrderId)
                .getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public List<Object[]> findCauseByRotatoryFundId(Long rotatoryFundId) {
        return eventEm.createNamedQuery("DiscountComment.findCauseByRotatoryFundId")
                .setParameter("rotatoryFundId", rotatoryFundId)
                .getResultList();
    }
}
