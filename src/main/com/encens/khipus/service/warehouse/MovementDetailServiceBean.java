package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Encens S.R.L.
 * Service to management movement detail entity
 *
 * @author
 * @version $Id: ${NAME}.java  16-abr-2010 18:05:10$
 */
@Stateless
@Name("movementDetailService")
@AutoCreate
public class MovementDetailServiceBean implements MovementDetailService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private GenericService genericService;

    public MovementDetailServiceBean() {
    }

    /**
     * Sum quantity by product item, state, movement date and movement type
     *
     * @param companyNumber
     * @param productItemCode
     * @param state
     * @param movementDetailDate
     * @param movementDetailType
     * @return BigDecimal
     */
    public BigDecimal sumQuantityByProductItemInBeforeDates(String companyNumber, String productItemCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType) {
        log.debug("Executing sumQuantityByProductItemInBeforeDates service.........................");

        BigDecimal sumValue = null;
        sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumQuantityByProductItemStateDateType").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("state", state).
                setParameter("movementDetailDate", movementDetailDate).
                setParameter("movementDetailType", movementDetailType).
                getSingleResult();
        return sumValue;
    }

    /**
     * Sum quantity by product item, warehouse, state, type in days before of filter date
     *
     * @param companyNumber
     * @param productItemCode
     * @param warehouseCode
     * @param state
     * @param movementDetailDate
     * @param movementDetailType
     * @return BigDecimal
     */
    public BigDecimal sumQuantityByProductItemWarehouseInBeforeDates(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType) {
        log.debug("Executing sumQuantityByProductItemWarehouseInBeforeDates service.........................");

        BigDecimal sumValue = null;
        sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumQuantityByProductItemWarehouseStateTypeInBeforeDates").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("warehouseCode", warehouseCode).
                setParameter("state", state).
                setParameter("movementDetailDate", movementDetailDate).
                setParameter("movementDetailType", movementDetailType).
                getSingleResult();
        return sumValue;
    }

    /**
     * Sum quantity in range date by product item, state, movement type
     *
     * @param companyNumber
     * @param productItemCode
     * @param state
     * @param movementDetailType
     * @param initDate
     * @param endDate
     * @return
     */
    public BigDecimal sumQuantityByProductItemWarehouseInRangeDate(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, MovementDetailType movementDetailType, Date initDate, Date endDate) {
        log.debug("Executing sumQuantityByProductItemWarehouseInRangeDate service.........................");

        BigDecimal sumValue = null;
        sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumQuantityByProductItemWarehouseStateTypeInRangeDate").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("warehouseCode", warehouseCode).
                setParameter("state", state).
                setParameter("movementDetailType", movementDetailType).
                setParameter("initDate", initDate).
                setParameter("endDate", endDate).
                getSingleResult();
        return (sumValue != null) ? sumValue : BigDecimal.ZERO;
    }

    /**
     * Sum amount by product item, state, movement date and movement type
     *
     * @param companyNumber
     * @param productItemCode
     * @param state
     * @param movementDetailDate
     * @param movementDetailType
     * @return BigDecimal
     */
    public BigDecimal sumAmountByProductItemInBeforeDates(String companyNumber, String productItemCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType) {
        log.debug("Executing sumAmountByProductItemInBeforeDates service.........................");

        BigDecimal sumValue = null;
        sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumAmountByProductItemStateDateType").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("state", state).
                setParameter("movementDetailDate", movementDetailDate).
                setParameter("movementDetailType", movementDetailType).
                getSingleResult();
        return sumValue;
    }

    /**
     * Sum amount by product item, warehouse, state, type in days before of filter date
     *
     * @param companyNumber
     * @param productItemCode
     * @param warehouseCode
     * @param state
     * @param movementDetailDate
     * @param movementDetailType
     * @return BigDecimal
     */
    public BigDecimal sumAmountByProductItemWarehouseInBeforeDates(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType) {
        log.debug("Executing sumAmountByProductItemWarehouseInBeforeDates service.........................");

        BigDecimal sumValue = null;
        sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumAmountByProductItemWarehouseStateTypeInBeforeDates").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("warehouseCode", warehouseCode).
                setParameter("state", state).
                setParameter("movementDetailDate", movementDetailDate).
                setParameter("movementDetailType", movementDetailType).
                getSingleResult();
        return sumValue;
    }

    /**
     * Sum amount in range date by product item, state, movement type
     *
     * @param companyNumber
     * @param productItemCode
     * @param state
     * @param movementDetailType
     * @param initDate
     * @param endDate
     * @return BigDecimal
     */
    public BigDecimal sumAmountByProductItemWarehouseInRangeDate(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, MovementDetailType movementDetailType, Date initDate, Date endDate) {
        log.debug("Executing sumAmountByProductItemWarehouseInRangeDate service.........................");

        BigDecimal sumValue = null;
        sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumAmountByProductItemWarehouseStateTypeInRangeDate").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("warehouseCode", warehouseCode).
                setParameter("state", state).
                setParameter("movementDetailType", movementDetailType).
                setParameter("initDate", initDate).
                setParameter("endDate", endDate).
                getSingleResult();
        return (sumValue != null) ? sumValue : BigDecimal.ZERO;
    }

    /**
     * Calculate initial quantity value to kardex until defined date,
     * this is: input_quantity - output_quantity
     *
     * @param companyNumber
     * @param productItemCode
     * @param movementDetailDate
     * @return BigDecimal
     */
    public BigDecimal calculateInitialQuantityToKardex(String companyNumber, String productItemCode, Date movementDetailDate) {
        BigDecimal initialValue = BigDecimal.ZERO;
        BigDecimal inputQuantity = sumQuantityByProductItemInBeforeDates(companyNumber, productItemCode, WarehouseVoucherState.APR, movementDetailDate, MovementDetailType.E);
        BigDecimal outputQuantity = sumQuantityByProductItemInBeforeDates(companyNumber, productItemCode, WarehouseVoucherState.APR, movementDetailDate, MovementDetailType.S);

        if (inputQuantity != null) {
            initialValue = inputQuantity;
        }
        if (outputQuantity != null) {
            initialValue = BigDecimalUtil.subtract(initialValue, outputQuantity);
        }

        return initialValue;
    }

    /**
     * Calculate initial amount value to kardex until defined date,
     * this is: input_amount - output_amount
     *
     * @param companyNumber
     * @param productItemCode
     * @param movementDetailDate
     * @return BigDecimal
     */
    public BigDecimal calculateInitialAmountToKardex(String companyNumber, String productItemCode, Date movementDetailDate) {
        BigDecimal initialValue = BigDecimal.ZERO;
        BigDecimal inputAmount = sumAmountByProductItemInBeforeDates(companyNumber, productItemCode, WarehouseVoucherState.APR, movementDetailDate, MovementDetailType.E);
        BigDecimal outputAmount = sumAmountByProductItemInBeforeDates(companyNumber, productItemCode, WarehouseVoucherState.APR, movementDetailDate, MovementDetailType.S);

        if (inputAmount != null) {
            initialValue = inputAmount;
        }
        if (outputAmount != null) {
            initialValue = BigDecimalUtil.subtract(initialValue, outputAmount, 6);
        }

        return initialValue;
    }

    /**
     * Calculates the sum of quantities from movements using some filters
     *
     * @param companyNumber      Company id
     * @param productItemCode    ProductItem code
     * @param state              Movement voucher State
     * @param movementDetailType Movement type
     * @param warehouseCode      Warehouse code
     * @return the sum of quantities
     */
    public BigDecimal sumMovementsQuantity(String companyNumber, String productItemCode, WarehouseVoucherState state, MovementDetailType movementDetailType, String warehouseCode) {
        log.debug("Executing sumMovementsQuantity service.........................");

        BigDecimal res = null;
        res = (BigDecimal) em.createNamedQuery("MovementDetail.sumMovementsQuantity").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("state", state).
                setParameter("movementDetailType", movementDetailType).
                setParameter("warehouseCode", warehouseCode).
                getSingleResult();
        return (res != null) ? res : BigDecimal.ZERO;
    }

    /**
     * Calculates the sum of quantities from movements using some filters
     *
     * @param companyNumber      Company id
     * @param productItemCode    ProductItem code
     * @param state              Movement voucher State
     * @param movementDetailType Movement type
     * @param warehouseCode      Warehouse code
     * @param initDate           The values will be calculated from this date
     * @param endDate            The values will be calculated to this date
     * @return the sum of quantities
     */
    public BigDecimal sumMovementsQuantityFromTo(String companyNumber, String productItemCode, WarehouseVoucherState state, MovementDetailType movementDetailType, String warehouseCode,
                                                 Date initDate, Date endDate) {
        log.debug("Executing sumMovementsQuantityFrom " + initDate + " to: " + endDate + " service.........................");

        BigDecimal res = (BigDecimal) em.createNamedQuery("MovementDetail.sumMovementsQuantityFromToDate").
                setParameter("initDate", initDate).
                setParameter("endDate", endDate).
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("state", state).
                setParameter("movementDetailType", movementDetailType).
                setParameter("warehouseCode", warehouseCode).
                getSingleResult();
        return (res != null) ? res : BigDecimal.ZERO;
    }

    /**
     * Calculates the sum of quantities from movements using some filters
     *
     * @param companyNumber      Company id
     * @param productItemCode    ProductItem code
     * @param state              Movement voucher State
     * @param movementDetailType Movement type
     * @param warehouseCode      Warehouse code
     * @param endDate            The values will be calculated to this date
     * @return the sum of quantities
     */
    public BigDecimal sumMovementsQuantityUpTo(String companyNumber, String productItemCode, WarehouseVoucherState state, MovementDetailType movementDetailType, String warehouseCode,
                                               Date endDate) {
        log.debug("Executing sumMovementsQuantityTo to: " + endDate + " service.........................");

        BigDecimal res = (BigDecimal) em.createNamedQuery("MovementDetail.sumMovementsQuantityUpToDate").
                setParameter("endDate", endDate).
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("state", state).
                setParameter("movementDetailType", movementDetailType).
                setParameter("warehouseCode", warehouseCode).
                getSingleResult();
        return (res != null) ? res : BigDecimal.ZERO;
    }

    /**
     * Find last Movement detail by company number, product item, warehouse and movement as approved
     *
     * @param companyNumber
     * @param productItemCode
     * @param warehouseCode
     * @return Movement detail
     */
    public MovementDetail findLastMovementDetail(String companyNumber, String productItemCode, String warehouseCode) {

        if (ValidatorUtil.isBlankOrNull(companyNumber) ||
                ValidatorUtil.isBlankOrNull(productItemCode) ||
                ValidatorUtil.isBlankOrNull(warehouseCode)) {
            return null;
        }

        MovementDetail movementDetail = null;

        Long movementDetailId = (Long) em.createNamedQuery("MovementDetail.findLastMovement").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("warehouseCode", warehouseCode).
                setParameter("state", WarehouseVoucherState.APR).getSingleResult();

        if (movementDetailId != null && movementDetailId > 0) {
            try {
                movementDetail = genericService.findById(MovementDetail.class, movementDetailId);
            } catch (EntryNotFoundException e) {
                log.debug("Not found Movement detail with id:" + movementDetailId, e);
            }
        }

        return movementDetail;
    }

    /**
     * calculate all warehouse voucher movement detail amounts
     *
     * @param companyNumber     company number
     * @param state             warehouse state
     * @param transactionNumber transaction number
     * @return BigDecimal
     */
    public BigDecimal sumWarehouseVoucherMovementDetailAmount(String companyNumber, WarehouseVoucherState state, String transactionNumber) {
        log.debug("Executing sumWarehouseVoucherMovementDetailAmount service.........................");

        BigDecimal sumValue = (BigDecimal) em.createNamedQuery("MovementDetail.sumAmountByStateTransactionNumberSourceIdNull").
                setParameter("companyNumber", companyNumber).
                setParameter("state", state).
                setParameter("transactionNumber", transactionNumber).
                getSingleResult();
        return (sumValue != null) ? sumValue : BigDecimal.ZERO;
    }


    @SuppressWarnings(value = "unchecked")
    public List<MovementDetail> findDetailByVoucherAndType(WarehouseVoucher warehouseVoucher, MovementDetailType movementDetailType) {
        return em.createNamedQuery("MovementDetail.findByMovementDetailType").
                setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).
                setParameter("movementType", movementDetailType).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<MovementDetail> findDetailListByVoucher(WarehouseVoucher warehouseVoucher) {
        return em.createNamedQuery("MovementDetail.findByWarehouseVoucher")
                .setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber())
                .setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber())
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<String> findDetailProductCodeByVoucher(WarehouseVoucher warehouseVoucher) {
        return em.createNamedQuery("MovementDetail.findProductCodeByVoucher").
                setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).getResultList();
    }

    @Override
    public void updateAmountTotal(String numTransaction,BigDecimal amount )
    {
        em.createQuery("update MovementDetail movementDetail set movementDetail.amount =:amount where transactionNumber =:numTransaction")
          .setParameter("numTransaction",numTransaction)
          .setParameter("amount",amount)
          .executeUpdate();
    }
}
