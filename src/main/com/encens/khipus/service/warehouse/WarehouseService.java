package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.warehouse.*;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.0
 */
@Local
public interface WarehouseService extends GenericService {
    void createWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                                InventoryMovement inventoryMovement,
                                MovementDetail movementDetail,
                                Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                List<MovementDetail> movementDetailWithoutWarnings) throws InventoryException, ProductItemNotFoundException;

    void saveWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                              InventoryMovement inventoryMovement,
                              List<MovementDetail> movementDetails,
                              Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                              Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                              List<MovementDetail> movementDetailWithoutWarnings) throws InventoryException, ProductItemNotFoundException;

    WarehouseVoucher findWarehouseVoucher(WarehouseVoucherPK id) throws WarehouseVoucherNotFoundException;

    InventoryMovement findInventoryMovement(InventoryMovementPK id);

    boolean isWarehouseVoucherApproved(WarehouseVoucherPK id);

    boolean existsWarehouseVoucherInDataBase(WarehouseVoucherPK id);

    boolean isEmptyWarehouseVoucher(WarehouseVoucherPK id);


    void updateWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                                InventoryMovement inventoryMovement,
                                Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                List<MovementDetail> movementDetailWithoutWarnings) throws ConcurrencyException,
            WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException;

    void updateMovementDetail(WarehouseVoucher warehouseVoucher,
                              MovementDetail movementDetail,
                              Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                              Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                              List<MovementDetail> movementDetailWithoutWarnings) throws WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException,
            MovementDetailNotFoundException,
            ConcurrencyException, InventoryException;

    void deleteWarehouseVoucher(WarehouseVoucherPK id) throws ReferentialIntegrityException,
            WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException;

    void createMovementDetail(WarehouseVoucher warehouseVoucher, MovementDetail movementDetail,
                              Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                              Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                              List<MovementDetail> movementDetailWithoutWarnings) throws
            WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException, InventoryException, ProductItemNotFoundException;

    void deleteMovementDetail(WarehouseVoucher warehouseVoucher,
                              MovementDetail movementDetail) throws WarehouseVoucherNotFoundException,
            MovementDetailNotFoundException,
            WarehouseVoucherApprovedException;

    MovementDetail readMovementDetail(WarehouseVoucher warehouseVoucher,
                                      MovementDetail movementDetail) throws WarehouseVoucherNotFoundException,
            MovementDetailNotFoundException;

    /**
     * Fills the warning attribute according to the Maps and List mappings
     *
     * @param movementDetail                the instance to modify
     * @param movementDetailUnderMinimalStockMap
     *                                      the map that holds under minimal stock movementDetails
     * @param movementDetailOverMaximumStockMap
     *                                      the map that holds over maximum stock movementDetails
     * @param movementDetailWithoutWarnings the list that holds movementDetails without warnings
     */
    void fillMovementDetail(MovementDetail movementDetail,
                            Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                            Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                            List<MovementDetail> movementDetailWithoutWarnings);

    // creates and approves a partial WarehouseVoucher
    void receivePartialVoucher(WarehouseVoucher warehouseVoucher,
                               InventoryMovement inventoryMovement,
                               List<MovementDetail> movementDetails,
                               Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                               Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                               List<MovementDetail> movementDetailWithoutWarnings)
            throws InventoryException, ProductItemNotFoundException, WarehouseVoucherNotFoundException,
            EntryDuplicatedException, WarehouseVoucherStateException, ConcurrencyException,
            ReferentialIntegrityException, ProductItemAmountException, InventoryUnitaryBalanceException,
            WarehouseVoucherEmptyException, InventoryProductItemNotFoundException,
            CompanyConfigurationNotFoundException, FinancesCurrencyNotFoundException,
            WarehouseVoucherApprovedException, FinancesExchangeRateNotFoundException, MovementDetailTypeException, WarehouseAccountCashNotFoundException;

    boolean isWarehouseVoucherPendant(WarehouseVoucherPK id);

    public Warehouse findWarehouseByCode(String warehouseCode);

    BigDecimal findAmountOrderByCodArt(String codArt,Gestion gestion,Date date);

    BigDecimal findExpectedAmountOrderProduction(String codArt,Gestion gestion);

    BigDecimal findProducedAmountOrderProduction(String codArt,Gestion gestion);

    public BigDecimal findProducedAmountRepro(String codArt,Gestion gestion);

    BigDecimal findAmountOrderByCodArt(String codArt,Gestion gestion);
}
