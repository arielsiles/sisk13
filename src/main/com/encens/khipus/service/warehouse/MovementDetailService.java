package com.encens.khipus.service.warehouse;

import com.encens.khipus.model.employees.MovementType;
import com.encens.khipus.model.warehouse.*;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: MovementDetailService.java  16-abr-2010 18:07:35$
 */
@Local
public interface MovementDetailService {
    BigDecimal sumQuantityByProductItemInBeforeDates(String companyNumber, String productItemCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType);

    BigDecimal sumAmountByProductItemInBeforeDates(String companyNumber, String productItemCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType);

    BigDecimal calculateInitialQuantityToKardex(String companyNumber, String productItemCode, Date movementDetailDate);

    BigDecimal calculateInitialAmountToKardex(String companyNumber, String productItemCode, Date movementDetailDate);

    BigDecimal sumQuantityByProductItemWarehouseInBeforeDates(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType);

    BigDecimal sumAmountByProductItemWarehouseInBeforeDates(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, Date movementDetailDate, MovementDetailType movementDetailType);

    BigDecimal sumAmountByProductItemWarehouseInRangeDate(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, MovementDetailType movementDetailType, Date initDate, Date endDate);

    BigDecimal sumQuantityByProductItemWarehouseInRangeDate(String companyNumber, String productItemCode, String warehouseCode, WarehouseVoucherState state, MovementDetailType movementDetailType, Date initDate, Date endDate);

    BigDecimal sumMovementsQuantity(String companyNumber, String productItemCode, WarehouseVoucherState state, MovementDetailType movementDetailType, String warehouseCode);

    MovementDetail findLastMovementDetail(String companyNumber, String productItemCode, String warehouseCode);

    BigDecimal sumWarehouseVoucherMovementDetailAmount(String companyNumber, WarehouseVoucherState state, String transactionNumber);

    BigDecimal sumMovementsQuantityFromTo(String companyNumber, String productItemCode, WarehouseVoucherState state, MovementDetailType movementDetailType, String warehouseCode,
                                          Date initDate, Date endDate);

    BigDecimal sumMovementsQuantityUpTo(String companyNumber, String productItemCode, WarehouseVoucherState state, MovementDetailType movementDetailType, String warehouseCode,
                                        Date endDate);

    @SuppressWarnings(value = "unchecked")
    List<MovementDetail> findDetailByVoucherAndType(WarehouseVoucher warehouseVoucher, MovementDetailType movementDetailType);

    @SuppressWarnings(value = "unchecked")
    List<String> findDetailProductCodeByVoucher(WarehouseVoucher warehouseVoucher);

    @SuppressWarnings(value = "unchecked")
    List<MovementDetail> findDetailListByVoucher(WarehouseVoucher warehouseVoucher);

    @SuppressWarnings(value = "unchecked")
    public List<MovementDetail> findDetailListByProductAndDate(String productItemCode, Date startDate, Date endDate);

    public void updateAmountTotal(String numTransaction,BigDecimal amount );

    @SuppressWarnings(value = "unchecked")
    public List<MovementDetail> findListMovementByWarehouseAndType(String warehouseCode, Date startDate, Date endDate, MovementDetailType movementDetailType);

    public List<MovementDetail> findListMovementByWarehouseAndTypeNull(String warehouseCode, Date startDate, Date endDate, MovementDetailType movementDetailType);
}
