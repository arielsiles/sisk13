package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.warehouse.WarehouseVoucherCreateAction;
import com.encens.khipus.action.warehouse.WarehouseVoucherGeneralAction;
import com.encens.khipus.action.warehouse.WarehouseVoucherUpdateAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.query.QueryUtils;
import com.encens.khipus.util.warehouse.WarehouseUtil;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.*;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("warehouseService")
@FinancesUser
@AutoCreate
public class WarehouseServiceBean extends GenericServiceBean implements WarehouseService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;

    @In
    private FinancesPkGeneratorService financesPkGeneratorService;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private FinancesUserService financesUserService;

    @In
    private SessionUser sessionUser;

    @In
    protected Map<String, String> messages;


    @In
    private MovementDetailService movementDetailService;

    public void createWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                                       InventoryMovement inventoryMovement,
                                       MovementDetail movementDetail,
                                       Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                       Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                       List<MovementDetail> movementDetailWithoutWarnings)
            throws InventoryException, ProductItemNotFoundException {

        String financeUserCode = financesUserService.getFinancesUserCode();
        String transactionNumber = financesPkGeneratorService.getNextPK();

        warehouseVoucher.getId().setTransactionNumber(transactionNumber);

        validateOutputMovementDetail(warehouseVoucher, movementDetail);
        getEntityManager().persist(warehouseVoucher);
        getEntityManager().flush();

        createInventoryMovement(warehouseVoucher, financeUserCode, inventoryMovement);
        getEntityManager().flush();
        if (null != movementDetail) {
            persistMovementDetail(warehouseVoucher, movementDetail,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
        }
    }

    // creates and approves a partial WarehouseVoucher
    public void receivePartialVoucher(WarehouseVoucher warehouseVoucher,
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
            WarehouseVoucherApprovedException, FinancesExchangeRateNotFoundException, MovementDetailTypeException, WarehouseAccountCashNotFoundException {
        WarehouseVoucher parentWarehouseVoucher = warehouseVoucher.getParentWarehouseVoucher();
        getEntityManager().refresh(parentWarehouseVoucher);
        if(warehouseVoucher.getWarehouse().getCashAccount() == null)
            throw new WarehouseAccountCashNotFoundException();

        if (parentWarehouseVoucher.getState().equals(WarehouseVoucherState.PEN)) {
            // changes the state to WarehouseVoucherState.PAR
            approvalWarehouseVoucherService.approvePartialInputParentWarehouseVoucher(parentWarehouseVoucher.getId());
        }
        // creates a new partial voucher
        savePartialWarehouseVoucher(warehouseVoucher,
                movementDetails,
                movementDetailUnderMinimalStockMap,
                movementDetailOverMaximumStockMap,
                movementDetailWithoutWarnings);
        // approves the new partial voucher
        approvalWarehouseVoucherService.approveWarehouseVoucher(warehouseVoucher.getId(),
                getGlossMessage(warehouseVoucher, inventoryMovement),
                movementDetailUnderMinimalStockMap,
                movementDetailOverMaximumStockMap,
                movementDetailWithoutWarnings);
        try {
            findById(WarehouseVoucher.class, warehouseVoucher.getId(), true);
        } catch (EntryNotFoundException e) {
            log.debug("Won't happen because it is recently created", e);
        }

        // holds info about all parent MovementDetails quantities
        Map<MovementDetail, BigDecimal> parentMovementDetailBigDecimalMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> parentMovementDetailList = movementDetailService.findDetailListByVoucher(parentWarehouseVoucher);
        for (MovementDetail movementDetail : parentMovementDetailList) {
            parentMovementDetailBigDecimalMap.put(movementDetail, movementDetail.getQuantity());
        }

        // add all partial MovementDetails to a list
        List<MovementDetail> allPartialMovementDetailList = new ArrayList<MovementDetail>();
        for (WarehouseVoucher voucher : parentWarehouseVoucher.getPartialWarehouseVoucherList()) {
            allPartialMovementDetailList.addAll(movementDetailService.findDetailListByVoucher(voucher));
        }

        // holds info about partial MovementDetails grouped by parent MovementDetail quantity
        Map<MovementDetail, BigDecimal> parentMovementDetailPartialSumMap = new HashMap<MovementDetail, BigDecimal>();
        // sum all partial movements by parent movement
        for (MovementDetail partialMovementDetail : allPartialMovementDetailList) {
            MovementDetail parentMovementDetail = partialMovementDetail.getParentMovementDetail();
            BigDecimal result = partialMovementDetail.getQuantity();
            if (parentMovementDetailPartialSumMap.containsKey(parentMovementDetail)) {
                result = BigDecimalUtil.sum(result, parentMovementDetailPartialSumMap.get(parentMovementDetail), 6);
            }
            parentMovementDetailPartialSumMap.put(parentMovementDetail, result);
        }


        for (Map.Entry<MovementDetail, BigDecimal> movementDetailBigDecimalEntry : parentMovementDetailPartialSumMap.entrySet()) {
            MovementDetail movementDetail = movementDetailBigDecimalEntry.getKey();
            movementDetail.setResidue(BigDecimalUtil.subtract(movementDetail.getQuantity(), movementDetailBigDecimalEntry.getValue(), 6));
            getEntityManager().merge(movementDetail);
        }


        boolean parentVoucherComplete = true;

        for (Map.Entry<MovementDetail, BigDecimal> movementDetailBigDecimalEntry : parentMovementDetailBigDecimalMap.entrySet()) {
            if (null == parentMovementDetailPartialSumMap.get(movementDetailBigDecimalEntry.getKey()) || movementDetailBigDecimalEntry.getValue().compareTo(parentMovementDetailPartialSumMap.get(movementDetailBigDecimalEntry.getKey())) != 0) {
                parentVoucherComplete = false;
                break;
            }
        }
        if (parentVoucherComplete) {
            parentWarehouseVoucher.setWarehouseVoucherReceptionType(WarehouseVoucherReceptionType.RT);
            getEntityManager().merge(parentWarehouseVoucher);
        }
        getEntityManager().flush();
    }

    // creates a new partial WarehouseVoucher
    private void savePartialWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                                             List<MovementDetail> movementDetails,
                                             Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                             Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                             List<MovementDetail> movementDetailWithoutWarnings)
            throws InventoryException, ProductItemNotFoundException, EntryDuplicatedException,
            WarehouseVoucherNotFoundException, WarehouseVoucherStateException {
        WarehouseVoucher parentWarehouseVoucher = warehouseVoucher.getParentWarehouseVoucher();
        if (!existsWarehouseVoucherInDataBase(parentWarehouseVoucher.getId())) {
            throw new WarehouseVoucherNotFoundException("Cannot update the warehouse voucher because" +
                    " it was deleted by other user.");
        }

        if (isWarehouseVoucherApproved(parentWarehouseVoucher.getId())) {
            throw new WarehouseVoucherStateException(getWarehouseVoucher(parentWarehouseVoucher.getId()).getState());
        }
        // copy parent voucher data to partial voucher
        warehouseVoucher.setCompanyNumber(parentWarehouseVoucher.getCompanyNumber());
        warehouseVoucher.setContraAccount(parentWarehouseVoucher.getContraAccount());
        warehouseVoucher.setCostCenter(parentWarehouseVoucher.getCostCenter());
        warehouseVoucher.setDate(new Date());
        warehouseVoucher.setDocumentCode(parentWarehouseVoucher.getDocumentCode());
        warehouseVoucher.setDocumentType(parentWarehouseVoucher.getDocumentType());
        warehouseVoucher.setExecutorUnit(parentWarehouseVoucher.getExecutorUnit());
        warehouseVoucher.setId(new WarehouseVoucherPK(parentWarehouseVoucher.getCompanyNumber(), financesPkGeneratorService.getNextPK()));
        warehouseVoucher.setNumber(parentWarehouseVoucher.getNumber());
        warehouseVoucher.setParentWarehouseVoucher(parentWarehouseVoucher);
        warehouseVoucher.setPetitionerJobContract(parentWarehouseVoucher.getPetitionerJobContract());
        warehouseVoucher.setPurchaseOrder(parentWarehouseVoucher.getPurchaseOrder());
        warehouseVoucher.setPurchaseOrderId(parentWarehouseVoucher.getPurchaseOrderId());
        warehouseVoucher.setResponsible(parentWarehouseVoucher.getResponsible());
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setWarehouse(parentWarehouseVoucher.getWarehouse());
        try {
            saveWarehouseVoucher(warehouseVoucher, new InventoryMovement(), movementDetails,
                    movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

    // creates a new WarehouseVoucher
    /** Crear Vale independiente **/
    public void saveWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                                     InventoryMovement inventoryMovement,
                                     List<MovementDetail> movementDetails,
                                     Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                     Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                     List<MovementDetail> movementDetailWithoutWarnings) throws InventoryException, ProductItemNotFoundException {
        String financeUserCode = financesUserService.getFinancesUserCode();

        String transactionNumber = financesPkGeneratorService.getNextPK();
        //String transactionNumber = "123";

        warehouseVoucher.getId().setTransactionNumber(transactionNumber);

        for (MovementDetail movementDetail : movementDetails) {
            validateOutputMovementDetail(warehouseVoucher, movementDetail);
        }

        getEntityManager().persist(warehouseVoucher);
        getEntityManager().flush();

        createInventoryMovement(warehouseVoucher, financeUserCode, inventoryMovement);
        getEntityManager().flush();

        System.out.println("----> 260:WarehouseServiceBean.saveWarehouseVoucher: ");
        for (MovementDetail movementDetail : movementDetails) {


            BigDecimal newPrecioUnitCompra = BigDecimalUtil.multiply(movementDetail.getUnitCost(), BigDecimalUtil.toBigDecimal(100), 6);
            newPrecioUnitCompra = BigDecimalUtil.divide(newPrecioUnitCompra, BigDecimalUtil.toBigDecimal(87), 6);

            System.out.println("---> newPrecioUnitCompra: " + newPrecioUnitCompra);
            movementDetail.setUnitPurchasePrice(newPrecioUnitCompra);
            movementDetail.setPurchasePrice(BigDecimalUtil.multiply(movementDetail.getQuantity(), newPrecioUnitCompra, 6));

            System.out.println("===> movementDetail.getMovementType(): " + movementDetail.getMovementType());

            persistMovementDetail(warehouseVoucher, movementDetail,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
        }
    }

    public WarehouseVoucher findWarehouseVoucher(WarehouseVoucherPK id) throws WarehouseVoucherNotFoundException {
        if (!existsWarehouseVoucherInDataBase(id)) {
            throw new WarehouseVoucherNotFoundException("Cannot find the warehouse voucher because" +
                    " it was deleted by other user.");
        }

        WarehouseVoucher warehouseVoucher = getEntityManager().find(WarehouseVoucher.class, id);
        getEntityManager().refresh(warehouseVoucher);

        return warehouseVoucher;
    }

    public InventoryMovement findInventoryMovement(InventoryMovementPK id) {
        InventoryMovement inventoryMovement = getEntityManager().find(InventoryMovement.class, id);
        getEntityManager().refresh(inventoryMovement);

        return inventoryMovement;
    }


    public void deleteWarehouseVoucher(WarehouseVoucherPK id) throws ReferentialIntegrityException,
            WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException {

        if (!existsWarehouseVoucherInDataBase(id)) {
            throw new WarehouseVoucherNotFoundException("Cannot delete the warehouse voucher because" +
                    " it was deleted by other user.");
        }

        WarehouseVoucher warehouseVoucher = getEntityManager().find(WarehouseVoucher.class, id);
        if (isWarehouseVoucherApproved(warehouseVoucher.getId())) {
            throw new WarehouseVoucherApprovedException("Cannot delete the warehouse voucher because" +
                    " it was approved by other user.");
        }

        try {
            InventoryMovementPK inventoryMovementPK = buildInventoryMovementPK(warehouseVoucher);
            InventoryMovement inventoryMovement = getEntityManager().find(InventoryMovement.class, inventoryMovementPK);

            deleteMovementDetails(warehouseVoucher);

            getEntityManager().remove(inventoryMovement);
            getEntityManager().flush();

            getEntityManager().remove(warehouseVoucher);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateWarehouseVoucher(WarehouseVoucher warehouseVoucher,
                                       InventoryMovement inventoryMovement,
                                       Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                       Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                       List<MovementDetail> movementDetailWithoutWarnings) throws ConcurrencyException,
            WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException {

        if (!existsWarehouseVoucherInDataBase(warehouseVoucher.getId())) {
            throw new WarehouseVoucherNotFoundException("Cannot update the warehouse voucher because" +
                    " it was deleted by other user.");
        }

        if (isWarehouseVoucherApproved(warehouseVoucher.getId())) {
            throw new WarehouseVoucherApprovedException("Cannot update the warehouse voucher because" +
                    " it was approved by other user.");
        }

        try {
            WarehouseVoucher dbWarehouseVoucher = getWarehouseVoucher(warehouseVoucher.getId());

            if (warehouseVoucher.getVersion() != dbWarehouseVoucher.getVersion()) {
                throw new ConcurrencyException("The warehouse voucher was updated by other user.");
            }

            // update detail warnings
            for (MovementDetail movementDetail : inventoryMovement.getMovementDetailList()) {
                // update detail warnings
                fillMovementDetail(movementDetail, movementDetailUnderMinimalStockMap,
                        movementDetailOverMaximumStockMap,
                        movementDetailWithoutWarnings);
            }

            getEntityManager().merge(warehouseVoucher);
            getEntityManager().flush();

            getEntityManager().merge(inventoryMovement);
            getEntityManager().flush();

            getEntityManager().refresh(warehouseVoucher);
            getEntityManager().refresh(inventoryMovement);

            if (warehouseVoucher.isExecutorUnitTransfer()) {
                updateMovementDetails(warehouseVoucher,
                        warehouseVoucher.getWarehouse(),
                        MovementDetailType.S,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode());
                updateMovementDetails(warehouseVoucher,
                        warehouseVoucher.getTargetWarehouse(),
                        MovementDetailType.E,
                        warehouseVoucher.getTargetExecutorUnit(),
                        warehouseVoucher.getTargetCostCenterCode());
            } else if (warehouseVoucher.isTransfer()) {
                updateMovementDetails(warehouseVoucher,
                        warehouseVoucher.getWarehouse(),
                        MovementDetailType.S,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode());
                updateMovementDetails(warehouseVoucher,
                        warehouseVoucher.getTargetWarehouse(),
                        MovementDetailType.E,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode());
            } else {
                MovementDetailType movementDetailType = WarehouseUtil.getMovementTye(warehouseVoucher.getDocumentType());
                updateMovementDetails(warehouseVoucher,
                        warehouseVoucher.getWarehouse(),
                        movementDetailType,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode());
            }

        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        }
    }

    public void createMovementDetail(WarehouseVoucher warehouseVoucher, MovementDetail movementDetail,
                                     Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                     Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                     List<MovementDetail> movementDetailWithoutWarnings)
            throws WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException,
            InventoryException, ProductItemNotFoundException {
        if (!existsWarehouseVoucherInDataBase(warehouseVoucher.getId())) {
            throw new WarehouseVoucherNotFoundException("Cannot create the movement detail because the " +
                    "root warehouse voucher was deleted by other user.");
        }

        if (isWarehouseVoucherApproved(warehouseVoucher.getId())) {
            throw new WarehouseVoucherApprovedException("Cannot create more movement details because " +
                    "the root warehouse voucher was approved by other user.");
        }

        //work with stored warehouseVoucher object, because it maybe have changed
        WarehouseVoucher actualWarehouseVoucher = getEntityManager().find(WarehouseVoucher.class, warehouseVoucher.getId());
        getEntityManager().refresh(actualWarehouseVoucher);
        System.out.println(" 000> " + warehouseVoucher.getWarehouse());
        if (warehouseVoucher.isConsumption()
                || warehouseVoucher.isOutput()
                || warehouseVoucher.isTransfer()
                || warehouseVoucher.isExecutorUnitTransfer()) {
            approvalWarehouseVoucherService.validateOutputMovementDetail(warehouseVoucher,
                    warehouseVoucher.getWarehouse(), movementDetail, false);
        }

        persistMovementDetail(warehouseVoucher, movementDetail,
                movementDetailUnderMinimalStockMap,
                movementDetailOverMaximumStockMap,
                movementDetailWithoutWarnings);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateMovementDetail(WarehouseVoucher warehouseVoucher,
                                     MovementDetail movementDetail,
                                     Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                     Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                     List<MovementDetail> movementDetailWithoutWarnings) throws WarehouseVoucherApprovedException,
            WarehouseVoucherNotFoundException,
            MovementDetailNotFoundException,
            ConcurrencyException, InventoryException {

        if (!existsWarehouseVoucherInDataBase(warehouseVoucher.getId())) {
            throw new WarehouseVoucherNotFoundException("Cannot update the movement detail because" +
                    " the root warehouse voucher was deleted by other user.");
        }

        if (isWarehouseVoucherApproved(warehouseVoucher.getId())) {
            throw new WarehouseVoucherApprovedException("Cannot update the movement detail because" +
                    " the root warehouse voucher was approved by other user.");
        }

        if (!existsMovementDetailInDataBase(movementDetail.getId())) {
            detach(movementDetail);
            throw new MovementDetailNotFoundException("Cannot update the movement detail because" +
                    " it was deleted by other user.");
        }

        MovementDetail dbMovementDetail = getMovementDetail(movementDetail.getId());

        if (movementDetail.getVersion() != dbMovementDetail.getVersion()) {
            throw new ConcurrencyException("The movement detail was updated by other user");
        }

        if (warehouseVoucher.isConsumption() || warehouseVoucher.isExecutorUnitTransfer()) {
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getUnitCost(), movementDetail.getQuantity(), 6);
            movementDetail.setAmount(amount);
        }

        if (warehouseVoucher.isConsumption()
                || warehouseVoucher.isOutput()
                || warehouseVoucher.isTransfer()
                || warehouseVoucher.isExecutorUnitTransfer()) {
            approvalWarehouseVoucherService.validateOutputMovementDetail(warehouseVoucher,
                    warehouseVoucher.getWarehouse(), movementDetail, true);
        }

        if (warehouseVoucher.isTransfer() || warehouseVoucher.isExecutorUnitTransfer()) {
            MovementDetail targetMovementDetail = getTargetMovementDetail(movementDetail);
            targetMovementDetail.setMovementDetailDate(movementDetail.getMovementDetailDate());
            targetMovementDetail.setProductItem(movementDetail.getProductItem());
            targetMovementDetail.setQuantity(movementDetail.getQuantity());
            targetMovementDetail.setAmount(movementDetail.getAmount());
            targetMovementDetail.setProductItemAccount(movementDetail.getProductItemAccount());
            targetMovementDetail.setUnitCost(movementDetail.getUnitCost());

            getEntityManager().merge(targetMovementDetail);
            getEntityManager().flush();
        }
        // update detail warnings
        fillMovementDetail(movementDetail, movementDetailUnderMinimalStockMap,
                movementDetailOverMaximumStockMap,
                movementDetailWithoutWarnings);
        getEntityManager().merge(movementDetail);
        getEntityManager().flush();
    }

    public void deleteMovementDetail(WarehouseVoucher warehouseVoucher,
                                     MovementDetail movementDetail) throws WarehouseVoucherNotFoundException,
            MovementDetailNotFoundException, WarehouseVoucherApprovedException {
        if (!existsWarehouseVoucherInDataBase(warehouseVoucher.getId())) {
            throw new WarehouseVoucherNotFoundException("Cannot delete the movement detail because" +
                    " the root warehouse voucher was deleted by other user.");
        }

        if (isWarehouseVoucherApproved(warehouseVoucher.getId())) {
            throw new WarehouseVoucherApprovedException("Cannot delete the movement detail because" +
                    " the root warehouse voucher was approved by other user.");
        }

        if (!existsMovementDetailInDataBase(movementDetail.getId())) {
            detach(movementDetail);
            throw new MovementDetailNotFoundException("Cannot delete the movement detail because " +
                    "it was deleted by other user");
        }

        if (warehouseVoucher.isTransfer() || warehouseVoucher.isExecutorUnitTransfer()) {
            MovementDetail targetMovementDetail = getTargetMovementDetail(movementDetail);
            getEntityManager().remove(targetMovementDetail);
            getEntityManager().flush();
        }
        getEntityManager().remove(movementDetail);
        getEntityManager().flush();
    }

    public MovementDetail readMovementDetail(WarehouseVoucher warehouseVoucher, MovementDetail movementDetail)
            throws WarehouseVoucherNotFoundException,
            MovementDetailNotFoundException {

        if (!existsWarehouseVoucherInDataBase(warehouseVoucher.getId())) {
            throw new WarehouseVoucherNotFoundException("Cannot read movement detail because" +
                    " the root warehouse voucher was deleted by other user.");
        }

        if (!existsMovementDetailInDataBase(movementDetail.getId())) {
            detach(movementDetail);
            throw new MovementDetailNotFoundException("Cannot read movement detail because it was deleted by other user.");
        }

        MovementDetail entity = getEntityManager().find(MovementDetail.class, movementDetail.getId());
        getEntityManager().refresh(entity);
        return entity;
    }

    public boolean existsWarehouseVoucherInDataBase(WarehouseVoucherPK id) {
        try {
            return null != getWarehouseVoucher(id);
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("Cannot find the WarehouseVoucher entity for id=" + id);
        }

        return false;
    }

    @SuppressWarnings(value = "unchecked")
    public boolean isEmptyWarehouseVoucher(WarehouseVoucherPK id) {
        boolean result = false;

        try {
            WarehouseVoucher warehouseVoucher = getWarehouseVoucher(id);

            List<MovementDetail> movementDetails = listEm.createNamedQuery("MovementDetail.findByTransactionNumber").
                    setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                    setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).getResultList();

            result = null == movementDetails || movementDetails.isEmpty();
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("Cannot detect if warehouse voucher is empty or not because it does not exist in the database, by default return false.");
        }

        return result;
    }

    private boolean existsMovementDetailInDataBase(Long id) {
        boolean result = false;
        try {
            result = null != getMovementDetail(id);
        } catch (MovementDetailNotFoundException e) {
            log.debug("Cannot find the MovementDetail entity for id=" + id);
        }


        return result;
    }

    public boolean isWarehouseVoucherApproved(WarehouseVoucherPK id) {
        boolean result = false;

        try {
            WarehouseVoucher warehouseVoucher = getWarehouseVoucher(id);
            result = WarehouseVoucherState.APR.equals(warehouseVoucher.getState());
            if (result) {
                WarehouseVoucher newWarehouseVoucher = getEntityManager().find(WarehouseVoucher.class, id);
                getEntityManager().refresh(newWarehouseVoucher);
            }

        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("The WarehouseVoucher was deleted.");
        }

        return result;
    }

    public boolean isWarehouseVoucherPendant(WarehouseVoucherPK id) {
        boolean result = false;

        try {
            WarehouseVoucher warehouseVoucher = getWarehouseVoucher(id);
            result = WarehouseVoucherState.PAR.equals(warehouseVoucher.getState());
            if (result) {
                WarehouseVoucher newWarehouseVoucher = getEntityManager().find(WarehouseVoucher.class, id);
                getEntityManager().refresh(newWarehouseVoucher);
            }

        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("The WarehouseVoucher was deleted.", e);
        }

        return result;
    }

    public boolean isWarehouseVoucherPartial(WarehouseVoucher warehouseVoucher) throws WarehouseVoucherNotFoundException {
        return isWarehouseVoucherInState(warehouseVoucher, WarehouseVoucherState.PAR);
    }

    private boolean isWarehouseVoucherInState(WarehouseVoucher warehouseVoucher, WarehouseVoucherState warehouseVoucherState)
            throws WarehouseVoucherNotFoundException {
        WarehouseVoucher dbWarehouseVoucher = getWarehouseVoucher(warehouseVoucher.getId());
        return warehouseVoucherState.equals(dbWarehouseVoucher.getState());
    }

    private void createInventoryMovement(WarehouseVoucher warehouseVoucher,
                                         String financeUserCode,
                                         InventoryMovement inventoryMovement) throws PersistenceException {
        inventoryMovement.getId().setState(warehouseVoucher.getState().name());
        inventoryMovement.getId().setTransactionNumber(warehouseVoucher.getId().getTransactionNumber());

        inventoryMovement.setUserNumber(financeUserCode);
        inventoryMovement.setCreationDate(warehouseVoucher.getDate());
        inventoryMovement.setMovementDate(warehouseVoucher.getDate());
        getEntityManager().persist(inventoryMovement);
    }

    private void persistMovementDetail(WarehouseVoucher warehouseVoucher,
                                       MovementDetail movementDetail,
                                       Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                       Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                       List<MovementDetail> movementDetailWithoutWarnings)
            throws PersistenceException, ProductItemNotFoundException {
        WarehouseVoucherCreateAction warehouseVoucherCreateAction = (WarehouseVoucherCreateAction) Component.getInstance("warehouseVoucherCreateAction");
        WarehouseVoucherUpdateAction warehouseVoucherUpdateAction = (WarehouseVoucherUpdateAction) Component.getInstance("warehouseVoucherUpdateAction");
        WarehouseVoucherGeneralAction warehouseVoucherGeneralAction = warehouseVoucherCreateAction != null ? warehouseVoucherCreateAction : warehouseVoucherUpdateAction;

        MovementDetailType detailType = WarehouseUtil.getMovementTye(warehouseVoucher.getDocumentType());
        if (warehouseVoucher.isTransfer() || warehouseVoucher.isExecutorUnitTransfer() || warehouseVoucher.isTransferProduct()) {
            detailType = MovementDetailType.S;
        }
        movementDetail.setMovementType(detailType);

        warehouseVoucherGeneralAction.buildValidateQuantityMappings(movementDetail);
        // update detail warnings
        fillMovementDetail(movementDetail,
                movementDetailUnderMinimalStockMap,
                movementDetailOverMaximumStockMap,
                movementDetailWithoutWarnings);
        if (warehouseVoucher.isConsumption() || warehouseVoucher.isExecutorUnitTransfer()) {
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getUnitCost(), movementDetail.getQuantity(), 6);
            movementDetail.setAmount(amount);
        }

        if (warehouseVoucher.isTransfer() || warehouseVoucher.isExecutorUnitTransfer()) {
            persistMovementDetail(warehouseVoucher, movementDetail, detailType, warehouseVoucher.getWarehouse());

            MovementDetail outputDetail = buildOutputMovementDetail(warehouseVoucher, movementDetail);
            warehouseVoucherGeneralAction.buildValidateQuantityMappings(outputDetail);
            // update detail warnings for output detail
            fillMovementDetail(outputDetail,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            persistMovementDetail(warehouseVoucher, outputDetail, MovementDetailType.E, warehouseVoucher.getTargetWarehouse());
        } else {
            persistMovementDetail(warehouseVoucher, movementDetail, detailType, warehouseVoucher.getWarehouse());
        }
    }

    private void persistMovementDetail(WarehouseVoucher warehouseVoucher,
                                       MovementDetail movementDetail,
                                       MovementDetailType type,
                                       Warehouse warehouse) throws PersistenceException {
        if (null == movementDetail.getExecutorUnit()) {
            movementDetail.setExecutorUnit(warehouseVoucher.getExecutorUnit());
        }

        if (null == movementDetail.getCostCenterCode() || "".equals(movementDetail.getCostCenterCode().trim())) {
            movementDetail.setCostCenterCode(warehouseVoucher.getCostCenterCode());
        }

        movementDetail.setState(warehouseVoucher.getState());
        movementDetail.setTransactionNumber(warehouseVoucher.getId().getTransactionNumber());
        movementDetail.setMovementType(type);
        movementDetail.setWarehouseCode(warehouse.getId().getWarehouseCode());
        movementDetail.setMovementDetailDate(warehouseVoucher.getDate());

        getEntityManager().persist(movementDetail);
        getEntityManager().flush();
    }

    @SuppressWarnings(value = "unchecked")
    private void updateMovementDetails(WarehouseVoucher warehouseVoucher,
                                       Warehouse warehouse,
                                       MovementDetailType movementDetailType,
                                       BusinessUnit businessUnit,
                                       String costCenterCode) {

        List<MovementDetail> movementDetails = getEntityManager().
                createNamedQuery("MovementDetail.findByMovementDetailType").
                setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).
                setParameter("movementType", movementDetailType).getResultList();

        for (int i = 0; i < movementDetails.size(); i++) {
            MovementDetail movementDetail = movementDetails.get(i);
            movementDetail.setWarehouseCode(warehouse.getId().getWarehouseCode());
            movementDetail.setExecutorUnit(businessUnit);
            movementDetail.setCostCenterCode(costCenterCode);

            getEntityManager().merge(movementDetail);
            getEntityManager().flush();
            getEntityManager().refresh(movementDetail);
        }
    }

    private MovementDetail buildOutputMovementDetail(WarehouseVoucher warehouseVoucher, MovementDetail movementDetail) {
        MovementDetail outputDetail = new MovementDetail();
        outputDetail.setProductItem(movementDetail.getProductItem());
        outputDetail.setWarehouse(warehouseVoucher.getTargetWarehouse());
        outputDetail.setAmount(movementDetail.getAmount());
        outputDetail.setQuantity(movementDetail.getQuantity());
        outputDetail.setProductItemAccount(movementDetail.getProductItemAccount());
        if (warehouseVoucher.isExecutorUnitTransfer()) {
            outputDetail.setExecutorUnit(warehouseVoucher.getTargetExecutorUnit());
            outputDetail.setCostCenterCode(warehouseVoucher.getTargetCostCenterCode());
            outputDetail.setUnitCost(movementDetail.getUnitCost());
            outputDetail.setAmount(movementDetail.getAmount());
        }
        if (warehouseVoucher.isTransfer()) {
            outputDetail.setExecutorUnit(movementDetail.getExecutorUnit());
            outputDetail.setCostCenterCode(movementDetail.getCostCenterCode());
        }

        outputDetail.setSourceId(movementDetail.getId());
        outputDetail.setMovementDetailDate(movementDetail.getMovementDetailDate());
        outputDetail.setMeasureUnit(movementDetail.getMeasureUnit());
        outputDetail.setMovementType(MovementDetailType.E);
        return outputDetail;
    }

    @SuppressWarnings(value = "unchecked")
    private void deleteMovementDetails(WarehouseVoucher warehouseVoucher) {

        if (warehouseVoucher.isTransfer()) {
            getEntityManager().createNamedQuery("MovementDetail.deleteTargetByTransactionNumber").
                    setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                    setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).executeUpdate();
            getEntityManager().flush();
        }

        getEntityManager().createNamedQuery("MovementDetail.deleteByTransactionNumber").
                setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).executeUpdate();
        getEntityManager().flush();
    }

    private InventoryMovementPK buildInventoryMovementPK(WarehouseVoucher warehouseVoucher) {
        return new InventoryMovementPK(warehouseVoucher.getId().getCompanyNumber(),
                warehouseVoucher.getId().getTransactionNumber(),
                warehouseVoucher.getState().name());
    }

    private MovementDetail getTargetMovementDetail(MovementDetail sourceMovementDetail) {
        return (MovementDetail) getEntityManager().createNamedQuery("MovementDetail.findBySourceMovementDetail").
                setParameter("sourceMovementDetail", sourceMovementDetail.getId()).getSingleResult();
    }

    private WarehouseVoucher getWarehouseVoucher(WarehouseVoucherPK id) throws WarehouseVoucherNotFoundException {
        WarehouseVoucher warehouseVoucher = listEm.find(WarehouseVoucher.class, id);

        if (null == warehouseVoucher) {
            throw new WarehouseVoucherNotFoundException("Cannot find the WarehouseVoucher entity for id=" + id);
        }

        return warehouseVoucher;
    }

    private MovementDetail getMovementDetail(Long id) throws MovementDetailNotFoundException {
        MovementDetail movementDetail = listEm.find(MovementDetail.class, id);

        if (null == movementDetail) {
            throw new MovementDetailNotFoundException("Cannot find the MovementDetail entity for id=" + id);
        }

        return movementDetail;
    }

    private void validateOutputMovementDetail(WarehouseVoucher warehouseVoucher,
                                              MovementDetail movementDetail) throws InventoryException {
        if (null != movementDetail && (warehouseVoucher.isConsumption()
                || warehouseVoucher.isOutput()
                || warehouseVoucher.isTransfer()
                || warehouseVoucher.isTransferProduct()
                || warehouseVoucher.isExecutorUnitTransfer())) {
            approvalWarehouseVoucherService.validateOutputMovementDetail(warehouseVoucher,
                    warehouseVoucher.getWarehouse(), movementDetail, false);
        }
    }

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
    public void fillMovementDetail(MovementDetail movementDetail,
                                   Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                   Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                   List<MovementDetail> movementDetailWithoutWarnings) {
        if (movementDetailUnderMinimalStockMap.containsKey(movementDetail)) {
            movementDetail.setWarning(MessageUtils.getMessage("MovementDetail.underMinimalStockWarning"));
        }
        if (movementDetailOverMaximumStockMap.containsKey(movementDetail)) {
            movementDetail.setWarning(MessageUtils.getMessage("MovementDetail.overMaximumStockWarning"));
        }
        if (movementDetailWithoutWarnings.contains(movementDetail)) {
            movementDetail.setWarning(messages.get("MovementDetail.idealWarning"));
        }
    }

    private String[] getGlossMessage(WarehouseVoucher warehouseVoucher, InventoryMovement inventoryMovement) {
        String gloss[] = new String[2];
        String dateString = DateUtils.format(warehouseVoucher.getDate(), MessageUtils.getMessage("patterns.date"));
        String productCodes = QueryUtils.toQueryParameter(movementDetailService.findDetailProductCodeByVoucher(warehouseVoucher));
        String documentName = warehouseVoucher.getDocumentType().getName();
        String sourceWarehouseName = warehouseVoucher.getWarehouse().getName();
        String movementDescription = inventoryMovement.getDescription();

        if (warehouseVoucher.isExecutorUnitTransfer()) {
            String targetWarehouseName = warehouseVoucher.getWarehouse().getName();
            gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.outTransferenceGloss", documentName, sourceWarehouseName, targetWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
            gloss[1] = MessageUtils.getMessage("WarehouseVoucher.message.inTransferenceGloss", documentName, sourceWarehouseName, targetWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        } else {
            String voucherTypeName = messages.get(warehouseVoucher.getDocumentType().getWarehouseVoucherType().getResourceKey());
            gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.gloss", voucherTypeName, documentName, sourceWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        }

        return gloss;

    }

    public Warehouse findWarehouseByCode(String warehouseCode){
        Warehouse warehouse = (Warehouse) em.createNamedQuery("Warehouse.findByCode")
                .setParameter("warehouseCode", warehouseCode)
                .getSingleResult();
        return warehouse;
    }

    @Override
    public BigDecimal findAmountOrderByCodArt(String codArt,Gestion gestion,Date date) {
        BigDecimal amountOrderByCodArt = BigDecimal.ZERO;
        Date startDate = DateUtils.firstDayOfYear(gestion.getYear());

        amountOrderByCodArt = (BigDecimal) em.createNativeQuery("select IFNULL(sum(art.cantidad + art.REPOSICION + art.PROMOCION),0)\n" +
                " from khipus.pedidos ped\n" +
                "INNER  JOIN khipus.articulos_pedido art\n" +
                "on art.idpedidos = ped.idpedidos\n" +
                "where ped.estado_pedido = 'PENDIENTE'\n" +
                "and ped.FECHA_ENTREGA between :startDate and :dateDelivery\n" +
                "and art.cod_art = :codArt\n")
                .setParameter("codArt",codArt)
                .setParameter("startDate",startDate, TemporalType.DATE)
                .setParameter("dateDelivery",date, TemporalType.DATE)
                .getSingleResult();

        if(amountOrderByCodArt == null)
             return BigDecimal.ZERO;

        return amountOrderByCodArt;
    }

    @Override
    public BigDecimal findAmountOrderByCodArt(String codArt,Gestion gestion) {
        BigDecimal amountOrderByCodArt = BigDecimal.ZERO;
        Date startDate = DateUtils.firstDayOfYear(gestion.getYear());
            amountOrderByCodArt = (BigDecimal) em.createNativeQuery("select IFNULL(sum(art.cantidad + art.REPOSICION + art.PROMOCION),0)\n" +
                    "from khipus.pedidos ped\n" +
                    "INNER  JOIN khipus.articulos_pedido art\n" +
                    "on art.idpedidos = ped.idpedidos\n" +
                    "where ped.estado_pedido = 'PENDIENTE'\n" +
                    "and ped.FECHA_ENTREGA >= :startDate \n" +
                    "and art.cod_art = :codArt\n")
                    .setParameter("codArt",codArt)
                    .setParameter("startDate",startDate, TemporalType.DATE)
                    .getSingleResult();
        if(amountOrderByCodArt == null)
            return BigDecimal.ZERO;

        return amountOrderByCodArt;
    }

    @Override
    public BigDecimal findExpectedAmountOrderProduction(String codArt,Gestion gestion) {
        BigDecimal expectedAmountOrderProduction;
        Date startDate = DateUtils.firstDayOfYear(gestion.getYear());
        Date endDate = DateUtils.lastDayOfYear(gestion.getYear());

        expectedAmountOrderProduction = (BigDecimal) em.createNativeQuery("SELECT sum(op.cantidadesperada) FROM ordenproduccion op\n" +
                "inner join composicionproducto cp\n" +
                "on op.idcomposicionproducto = cp.idcomposicionproducto\n" +
                "inner join metaproductoproduccion mp\n" +
                "on mp.idmetaproductoproduccion = cp.idproductoprocesado\n" +
                "inner join planificacionproduccion pp\n" +
                "on pp.IDPLANIFICACIONPRODUCCION = op.IDPLANIFICACIONPRODUCCION " +
                "where mp.cod_art = :codArt\n" +
                "and op.estadoorden = 'PENDING'" +
                "and pp.fecha between :startDate and :endDate")
                .setParameter("codArt",codArt)
                .setParameter("startDate",startDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .getSingleResult();

        if(expectedAmountOrderProduction == null)
            return BigDecimal.ZERO;

        return expectedAmountOrderProduction;
    }

    public BigDecimal findProducedAmountRepro(String codArt,Gestion gestion) {
        BigDecimal expectedAmountOrderProduction;
        Date startDate = DateUtils.firstDayOfYear(gestion.getYear());
        Date endDate = DateUtils.lastDayOfYear(gestion.getYear());

        expectedAmountOrderProduction = (BigDecimal) em.createNativeQuery("select ifnull(sum(ps.cantidad),0) from productobase pb\n" +
                "inner join productosimple ps\n" +
                "on pb.idproductobase = ps.idproductobase\n" +
                "inner join planificacionproduccion pp\n" +
                "on pp.idplanificacionproduccion = pb.idplanificacionproduccion\n" +
                "inner join productosimpleprocesado psp\n" +
                "on ps.idproductosimple = psp.idproductosimple\n" +
                "inner join metaproductoproduccion mp\n" +
                "on mp.idmetaproductoproduccion = psp.idmetaproductoproduccion\n" +
                "where mp.cod_art = :codArt\n" +
                "and ps.estado = 'EXECUTED'\n" +
                "and pp.fecha between :startDate and :endDate")
                .setParameter("codArt",codArt)
                .setParameter("startDate",startDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .getSingleResult();

        if(expectedAmountOrderProduction == null)
            return BigDecimal.ZERO;

        return expectedAmountOrderProduction;
    }



    @Override
    public BigDecimal findProducedAmountOrderProduction(String codArt,Gestion gestion) {
        BigDecimal producedAmountOrderProduction;
        Date startDate = DateUtils.firstDayOfYear(gestion.getYear());
        Date endDate = DateUtils.lastDayOfYear(gestion.getYear());

        producedAmountOrderProduction = (BigDecimal) em.createNativeQuery("SELECT sum(op.cantidadproducida) FROM ordenproduccion op\n" +
                "inner join composicionproducto cp\n" +
                "on op.idcomposicionproducto = cp.idcomposicionproducto\n" +
                "inner join metaproductoproduccion mp\n" +
                "on mp.idmetaproductoproduccion = cp.idproductoprocesado\n" +
                "inner join planificacionproduccion pp\n" +
                "on pp.IDPLANIFICACIONPRODUCCION = op.IDPLANIFICACIONPRODUCCION " +
                "where mp.cod_art = :codArt\n" +
                "and op.estadoorden = 'EXECUTED'\n"+
                "and pp.fecha between :startDate and :endDate")
                .setParameter("codArt",codArt)
                .setParameter("startDate",startDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .getSingleResult();

        if(producedAmountOrderProduction == null)
            return BigDecimal.ZERO;

        return producedAmountOrderProduction;
    }

    public WarehouseDocumentType findWarehouseDocumentType(WarehouseVoucherType warehouseVoucherType){

        WarehouseDocumentType warehouseDocumentType = (WarehouseDocumentType)getEntityManager()
                .createNamedQuery("WarehouseDocumentType.findWarehouseDocumentType")
                .setParameter("warehouseVoucherType", warehouseVoucherType).getSingleResult();

        return warehouseDocumentType;
    }

    @Override
    public void createTransferInventoryPeriod(List<InventoryPeriod> inventoryPeriodList) {

        for (InventoryPeriod inventoryPeriod : inventoryPeriodList){
            em.persist(inventoryPeriod);
            em.flush();
        }
    }

    public Boolean wasTransferred(String warehouseCode, Integer year, Integer month){

        Boolean result = Boolean.FALSE;

        Long count = (Long)em.createQuery("select count(*) from InventoryPeriod inventory " +
                " where inventory.warehouseCode =:warehouseCode " +
                " and inventory.year =:year " +
                " and inventory.month =:month ")
                .setParameter("warehouseCode", warehouseCode)
                .setParameter("year", year)
                .setParameter("month", month)
                .getSingleResult();

        System.out.println("--------------->>>>> count: " + count);

        if (count > 0) result = Boolean.TRUE;

        return result;
    }


}
