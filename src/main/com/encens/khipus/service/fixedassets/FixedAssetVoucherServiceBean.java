package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.fixedassets.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.VoucherBuilder;
import com.encens.khipus.util.VoucherDetailBuilder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Service implementation of FixedAssetVoucherService
 *
 * @author
 * @version 3.5.2.2
 */

@Stateless
@Name("fixedAssetVoucherService")
@AutoCreate
public class FixedAssetVoucherServiceBean extends GenericServiceBean implements FixedAssetVoucherService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In(required = false)
    private User currentUser;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private FixedAssetService fixedAssetService;
    @In
    private FixedAssetMovementService fixedAssetMovementService;
    @In
    private FinancesUserService financesUserService;
    @In
    private VoucherService voucherService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private PurchaseOrderFixedAssetPartService purchaseOrderFixedAssetPartService;
    @In
    private FixedAssetPartService fixedAssetPartService;

    /* Create the fixedAssets registration corresponding to a purchase order*/

    public void registration(FixedAssetVoucher fixedAssetVoucher)
            throws EntryDuplicatedException,
            DuplicatedFixedAssetCodeException, CompanyConfigurationNotFoundException {

        PurchaseOrder purchaseOrder = null;
        try {
            purchaseOrder = findById(PurchaseOrder.class, fixedAssetVoucher.getPurchaseOrder().getId());
        } catch (EntryNotFoundException ignored) {
        }

        if (null != purchaseOrder) {
            cleanUnusedData(fixedAssetVoucher);
            if (purchaseOrder.getPurchaseOrderCause().isFixedassetPurchase()) {
                List<FixedAsset> fixedAssetList = fixedAssetService.findFixedAssetByPurchaseOrderAndState(purchaseOrder, FixedAssetState.PEN);
                for (FixedAsset eventFixedAsset : fixedAssetList) {
                    try {
                        FixedAsset fixedAsset;
                        fixedAsset = fixedAssetService.findById(FixedAsset.class, eventFixedAsset.getId());
                        FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
                        fixedAssetMovement = createFixedAssetRegistrationMovement(fixedAssetMovement, fixedAsset, fixedAssetVoucher);
                        assignCode(fixedAssetVoucher);
                        updateCode(fixedAssetVoucher);
                        getEntityManager().persist(fixedAssetVoucher);
                        getEntityManager().flush();

                        fixedAssetMovement.setFixedAssetVoucher(fixedAssetVoucher);
                        getEntityManager().persist(fixedAssetMovement);
                        getEntityManager().flush();
                    } catch (EntryNotFoundException e) {
                        log.debug("An instance have been deleted by another user");
                    }
                }
            } else {
                List<PurchaseOrderFixedAssetPart> purchaseOrderFixedAssetPartList = purchaseOrderFixedAssetPartService.getPurchaseOrderFixedAssetPartList(purchaseOrder);
                assignCode(fixedAssetVoucher);
                updateCode(fixedAssetVoucher);
                getEntityManager().persist(fixedAssetVoucher);
                getEntityManager().flush();

                BigDecimal multipliedFactor = (CollectionDocumentType.INVOICE.equals(purchaseOrder.getDocumentType())) ? Constants.VAT_COMPLEMENT : BigDecimal.ONE;

                for (PurchaseOrderFixedAssetPart orderFixedAssetPart : purchaseOrderFixedAssetPartList) {
                    /*create an movement*/
                    FixedAsset fixedAsset = orderFixedAssetPart.getFixedAsset();
                    FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
                    fixedAssetMovement.setCreationDate(new Date());
                    // Always pendant state
                    fixedAssetMovement.setState(FixedAssetMovementState.PEN);
                    fixedAssetMovement.setFixedAssetMovementType(fixedAssetVoucher.getFixedAssetVoucherType());
                    fixedAssetMovement.setFixedAsset(fixedAsset);
                    fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
                    fixedAssetMovement.setFixedAssetVoucher(fixedAssetVoucher);
                    fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
                    fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
                    fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
                    fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
                    fixedAssetMovement.setCause(orderFixedAssetPart.getFullName());
                    fixedAssetMovement.setBsAmount(BigDecimalUtil.multiply(orderFixedAssetPart.getUnitPrice(), multipliedFactor));
                    getEntityManager().persist(fixedAssetMovement);
                }
                getEntityManager().flush();

            }
        }
    }

    /* Create the fixedAssets registration corresponding to a purchase order*/

    public void approveRegistration(FixedAssetVoucher fixedAssetVoucher)
            throws EntryDuplicatedException,
            DuplicatedFixedAssetCodeException, CompanyConfigurationNotFoundException,
            FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucherException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {

        FixedAssetVoucher dataBaseFixedAssetVoucher = findInDataBase(fixedAssetVoucher.getId());
        /* check if there is another voucher of type alt in approved state*/
        if (!canApproveRegistration(fixedAssetVoucher)) {
            throw new FixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucherException("Another fixedAssetVoucher have already approve the registration of the fixedassets related to the purchase order");
        }
        PurchaseOrder purchaseOrder = null;
        try {
            purchaseOrder = findById(PurchaseOrder.class, fixedAssetVoucher.getPurchaseOrder().getId());
        } catch (EntryNotFoundException ignored) {
        }
        if (null != purchaseOrder && canChangeFixedAssetVoucher(dataBaseFixedAssetVoucher)) {
            cleanUnusedData(fixedAssetVoucher);
            fixedAssetVoucher.setState(FixedAssetVoucherState.APR);

            if (purchaseOrder.getPurchaseOrderCause().isFixedassetPurchase()) {
                Voucher voucherForGeneration = createAccountEntryForApprovedFixedAssetsByPurchaseOrder(fixedAssetVoucher);
                fixedAssetVoucher.setTransactionNumber(voucherForGeneration.getTransactionNumber());

                List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, listEm);
                for (FixedAssetMovement eventFixedAssetMovement : fixedAssetMovementList) {
                    try {
                        FixedAsset fixedAsset = fixedAssetService.findById(FixedAsset.class, eventFixedAssetMovement.getFixedAsset().getId());
                        fixedAsset.setFixedAssetLocation(fixedAssetVoucher.getFixedAssetLocation());
                        fixedAsset.setState(FixedAssetState.VIG);
                        fixedAsset.setRegistrationDate(new Date());
                        fixedAsset.setCustodianJobContract(fixedAssetVoucher.getCustodianJobContract());
                        fixedAsset = fixedAssetService.generateCodes(fixedAsset);
                        if (!getEntityManager().contains(fixedAsset)) {
                            getEntityManager().merge(fixedAsset);
                        }
                        getEntityManager().flush();
                        eventFixedAssetMovement.setNewFixedAssetLocation(fixedAssetVoucher.getFixedAssetLocation());
                        eventFixedAssetMovement.setTransactionNumber(voucherForGeneration.getTransactionNumber());
                        eventFixedAssetMovement.setState(FixedAssetMovementState.APR);
                        eventFixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));
                        fixedAssetVoucher.setUpdatedBy(currentUser);
                        getEntityManager().flush();
                    } catch (EntryNotFoundException e) {
                        log.error(e, "An instance have been deleted by another user");
                    }
                }
            } else if (purchaseOrder.getPurchaseOrderCause().isFixedassetPartsPurchase()) {
                Voucher voucherForGeneration = createAccountEntryForApprovedFixedAssetPartByPurchaseOrder(fixedAssetVoucher);
                fixedAssetVoucher.setTransactionNumber(voucherForGeneration.getTransactionNumber());
                fixedAssetVoucher.setState(FixedAssetVoucherState.APR);
                fixedAssetVoucher.setUpdatedBy(currentUser);
                fixedAssetVoucher.setMovementDate(new Date());

                if (!getEntityManager().contains(fixedAssetVoucher)) {
                    getEntityManager().merge(fixedAssetVoucher);
                }
                getEntityManager().flush();

                List<PurchaseOrderFixedAssetPart> purchaseOrderFixedAssetPartList = purchaseOrderFixedAssetPartService.getPurchaseOrderFixedAssetPartList(purchaseOrder);
                fixedAssetPartService.createFixedAssetParts(purchaseOrderFixedAssetPartList);
            }
        }
    }

    private BigDecimal getCurrentExchangeRate(FinancesCurrencyType currency, BigDecimal susExchangeRate, BigDecimal ufvExchangeRate) {
        return FinancesCurrencyType.D.equals(currency) ? susExchangeRate : FinancesCurrencyType.U.equals(currency) ? ufvExchangeRate : BigDecimal.ONE;
    }

    private FixedAssetMovement createFixedAssetRegistrationMovement(FixedAssetMovement fixedAssetMovement, FixedAsset fixedAsset, FixedAssetVoucher fixedAssetVoucher)
            throws CompanyConfigurationNotFoundException {
        fillDefaultValuesForFixedAssetMovement(fixedAsset, fixedAssetMovement);

        fixedAssetMovement.setBsSusRate(fixedAsset.getBsSusRate());
        fixedAssetMovement.setBsUfvRate(fixedAsset.getBsUfvRate());
        fixedAssetMovement.setUfvAmount(fixedAsset.getUfvOriginalValue());
        fixedAssetMovement.setBsAmount(fixedAsset.getBsOriginalValue());
        fixedAssetMovement.setInitialDepreciation(fixedAsset.getDepreciation());
        fixedAssetMovement.setCustodian(fixedAssetVoucher.getCustodianJobContract().getContract().getEmployee());
        fixedAssetMovement.setCostCenterCode(fixedAssetVoucher.getCostCenterCode());
        fixedAssetMovement.setBusinessUnit(fixedAssetVoucher.getBusinessUnit());
        fixedAssetMovement.setFixedAssetMovementType(fixedAssetVoucher.getFixedAssetVoucherType());
        fixedAssetMovement.setFixedAssetMovementAccount(companyConfigurationService.findCompanyConfiguration().getFixedAssetInTransitAccountCode());
        fixedAssetMovement.setCause(fixedAssetVoucher.getPurchaseOrder().getGloss());
        return fixedAssetMovement;
    }

    private void fillDefaultValuesForFixedAssetMovement(FixedAsset fixedAsset, FixedAssetMovement fixedAssetMovement) {
        // Always current date
        fixedAssetMovement.setMovementDate(new Date());
        fixedAssetMovement.setCreationDate(new Date());
        fixedAssetMovement.setState(FixedAssetMovementState.PEN);
        fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
        fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
        fixedAssetMovement.setFixedAsset(fixedAsset);
    }

    @SuppressWarnings({"NullableProblems"})
    private void cleanUnusedData(FixedAssetVoucher fixedAssetVoucher) {
        if (null != fixedAssetVoucher.getFixedAssetVoucherType()) {
            /* for ALT and TRA*/
            if (!fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
                fixedAssetVoucher.setFixedAssetPayment(null);
            } else {
                if (null != fixedAssetVoucher.getFixedAssetPayment() && !fixedAssetVoucher.getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                    fixedAssetVoucher.getFixedAssetPayment().setCheckDestination(null);
                }
            }
            fixedAssetVoucher.setCurrency(null);
            fixedAssetVoucher.setCurrency(null);
            fixedAssetVoucher.setBsAmount(null);
            fixedAssetVoucher.setSusAmount(null);
            fixedAssetVoucher.setUfvAmount(null);
            fixedAssetVoucher.setBsSusRate(null);
            fixedAssetVoucher.setBsUfvRate(null);
            if (fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)) {
                fixedAssetVoucher.setPurchaseOrder(null);
                fixedAssetVoucher.setCause(null);
            }
            if (fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)) {
                fixedAssetVoucher.setPurchaseOrder(null);
                fixedAssetVoucher.setCustodianJobContract(null);
                fixedAssetVoucher.setBusinessUnit(null);
                fixedAssetVoucher.setCostCenter(null);
            }
        }
    }

    /* create transference of a set of fixedAssets corresponding to a selected list of Fixed Assets*/

    public void transference(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetInvalidStateException {

        validateFixedAssetsStates(fixedAssetVoucher, selectedFixedAssetList);

        try {
            assignCode(fixedAssetVoucher);
            updateCode(fixedAssetVoucher);
            cleanUnusedData(fixedAssetVoucher);
            getEntityManager().persist(fixedAssetVoucher);
            for (FixedAsset fixedAsset : selectedFixedAssetList) {
                /*create an movement*/
                FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
                fixedAssetMovement.setMovementDate(new Date());
                fixedAssetMovement.setCreationDate(new Date());
                // Always pendant state
                fixedAssetMovement.setState(FixedAssetMovementState.PEN);
                fixedAssetMovement.setCustodian(fixedAssetVoucher.getCustodianJobContract().getContract().getEmployee());
                fixedAssetMovement.setCostCenter(fixedAssetVoucher.getCostCenter());
                fixedAssetMovement.setBusinessUnit(fixedAssetVoucher.getBusinessUnit());
                fixedAssetMovement.setFixedAsset(fixedAsset);
                fixedAssetMovement.setFixedAssetMovementType(fixedAssetVoucher.getFixedAssetVoucherType());
                fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
                fixedAssetMovement.setFixedAssetVoucher(fixedAssetVoucher);
                getEntityManager().persist(fixedAssetMovement);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new EntryDuplicatedException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException,
            FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException,
            FixedAssetMovementInvalidStateException {

        validateFixedAssetsStates(fixedAssetVoucher, selectedFixedAssetList);
        validateFixedAssetMovementsStates(fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, listEm));

        FixedAssetVoucher dataBaseFixedAssetVoucher = findInDataBase(fixedAssetVoucher.getId());
        if (canChangeFixedAssetVoucher(dataBaseFixedAssetVoucher)) {
            cleanUnusedData(fixedAssetVoucher);
            if (fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)
                    || fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)
                    || fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
                List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucherAndState(fixedAssetVoucher, FixedAssetMovementState.PEN);
                /*add all the items added to the fixedAssetList if any*/
                for (FixedAsset fixedAsset : selectedFixedAssetList) {
                    boolean found = false;
                    int i = 0;
                    while (i < fixedAssetMovementList.size() && !found) {
                        log.debug("i" + i);
                        FixedAssetMovement fixedAssetMovement = fixedAssetMovementList.get(i);
                        log.debug(fixedAsset.getId() + " equals " + fixedAssetMovement.getFixedAsset().getId());
                        if (fixedAsset.getId().equals(fixedAssetMovement.getFixedAsset().getId())) {
                            found = true;
                        }
                        i++;
                    }
                    if (!found) {
                        /*create a movement*/
                        FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
                        fixedAssetMovement.setMovementDate(new Date());
                        fixedAssetMovement.setCreationDate(new Date());
                        // Always pendant state
                        fixedAssetMovement.setState(FixedAssetMovementState.PEN);
                        fixedAssetMovement.setFixedAsset(fixedAsset);
                        fixedAssetMovement.setFixedAssetMovementType(fixedAssetVoucher.getFixedAssetVoucherType());
                        fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
                        fixedAssetMovement.setFixedAssetVoucher(fixedAssetVoucher);
                        if (fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.ALT)
                                || fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)) {
                            fixedAssetMovement.setCustodian(fixedAssetVoucher.getCustodianJobContract().getContract().getEmployee());
                            fixedAssetMovement.setCostCenter(fixedAssetVoucher.getCostCenter());
                            fixedAssetMovement.setBusinessUnit(fixedAssetVoucher.getBusinessUnit());
                        }
                        if (fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)
                                || fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
                            fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
                            fixedAssetMovement.setCostCenter(fixedAsset.getCostCenter());
                            fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
                        }
                        getEntityManager().persist(fixedAssetMovement);

                        fixedAssetMovementList.add(fixedAssetMovement);
                    }
                }
                /*drop all the removed items if any*/
                for (FixedAssetMovement fixedAssetMovement : fixedAssetMovementList) {
                    boolean found = false;
                    int i = 0;
                    while (i < selectedFixedAssetList.size() && !found) {
                        FixedAsset fixedAsset = selectedFixedAssetList.get(i);
                        log.debug(fixedAssetMovement.getFixedAsset().getId() + " equals " + fixedAsset.getId());
                        if (fixedAssetMovement.getFixedAsset().getId().equals(fixedAsset.getId())) {
                            found = true;
                        }
                        i++;
                    }
                    if (!found) {
                        FixedAssetMovement containerFixedAssetMovement = getEntityManager().find(FixedAssetMovement.class, fixedAssetMovement.getId());
                        getEntityManager().remove(containerFixedAssetMovement);
                    }
                }
            }
            if (!getEntityManager().contains(fixedAssetVoucher)) {
                getEntityManager().merge(fixedAssetVoucher);
            }
            getEntityManager().flush();
        }
    }

    public void approveTransference(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException,
            FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException {


        validateFixedAssetsStates(fixedAssetVoucher, selectedFixedAssetList);
        validateFixedAssetMovementsStates(fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, listEm));


        FixedAssetVoucher dataBaseFixedAssetVoucher = findInDataBase(fixedAssetVoucher.getId());
        if (canChangeFixedAssetVoucher(dataBaseFixedAssetVoucher)) {

            try {
                updateFixedAssetVoucher(fixedAssetVoucher, selectedFixedAssetList);
                List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucherAndState(fixedAssetVoucher, FixedAssetMovementState.PEN);
                for (FixedAssetMovement eventFixedAssetMovement : fixedAssetMovementList) {
                    FixedAsset fixedAsset;
                    FixedAsset databaseFixedAsset = listEm.find(FixedAsset.class, eventFixedAssetMovement.getFixedAsset().getId());
                    FixedAssetMovement fixedAssetMovement;
                    fixedAsset = getEntityManager().find(FixedAsset.class, eventFixedAssetMovement.getFixedAsset().getId());
                    fixedAssetMovement = getEntityManager().find(FixedAssetMovement.class, eventFixedAssetMovement.getId());

                    fixedAsset.setCustodianJobContract(fixedAssetVoucher.getCustodianJobContract());
                    fixedAsset.setBusinessUnit(fixedAssetVoucher.getBusinessUnit());
                    fixedAsset.setCostCenter(fixedAssetVoucher.getCostCenter());
                    fixedAsset.setCostCenter(fixedAssetVoucher.getCostCenter());
                    fixedAsset.setFixedAssetLocation(fixedAssetVoucher.getFixedAssetLocation());

                    if (fixedAssetVoucher.getCustodianJobContract().getContract().getEmployee().getId().compareTo(databaseFixedAsset.getCustodianJobContract().getContract().getEmployee().getId()) != 0) {
                        fixedAssetMovement.setLastCustodian(databaseFixedAsset.getCustodianJobContract().getContract().getEmployee());
                    }
                    if (fixedAssetVoucher.getBusinessUnit().getId().compareTo(databaseFixedAsset.getBusinessUnit().getId()) != 0) {
                        fixedAssetMovement.setLastBusinessUnit(databaseFixedAsset.getBusinessUnit());
                    }
                    if (fixedAssetVoucher.getCostCenter().getId().getCode().compareTo(
                            databaseFixedAsset.getCostCenter().getId().getCode()) != 0) {
                        fixedAssetMovement.setLastCostCenter(databaseFixedAsset.getCostCenter());
                    }

                    fixedAssetMovement.setNewFixedAssetLocation(fixedAssetVoucher.getFixedAssetLocation());
                    fixedAssetMovement.setLastFixedAssetLocation(databaseFixedAsset.getFixedAssetLocation());

                    fixedAssetMovement.setMovementDate(new Date());
                    fixedAssetMovement.setState(FixedAssetMovementState.APR);
                    fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));

                    fixedAssetVoucher.setState(FixedAssetVoucherState.APR);
                    fixedAssetVoucher.setUpdatedBy(currentUser);

                    if (!getEntityManager().contains(fixedAsset)) {
                        getEntityManager().merge(fixedAsset);
                    }

                    if (!getEntityManager().contains(fixedAssetMovement)) {
                        getEntityManager().merge(fixedAssetMovement);
                    }
                    if (!getEntityManager().contains(fixedAssetVoucher)) {
                        getEntityManager().merge(fixedAssetVoucher);
                    }
                    getEntityManager().flush();
                }
                getEntityManager().flush();
            } catch (OptimisticLockException e) {
                throw new ConcurrencyException(e);
            } catch (PersistenceException e) {
                throw new EntryDuplicatedException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* create transference of a set of fixedAssets corresponding to a selected list of Fixed Assets*/

    public void discharge(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetInvalidStateException {

        validateFixedAssetsStates(fixedAssetVoucher, selectedFixedAssetList);

        try {
            assignCode(fixedAssetVoucher);
            updateCode(fixedAssetVoucher);
            cleanUnusedData(fixedAssetVoucher);
            getEntityManager().persist(fixedAssetVoucher);
            getEntityManager().flush();
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            String provisionByTangibleFixedAssetObsolescenceAccountCode = companyConfiguration.getProvisionByTangibleFixedAssetObsolescenceAccountCode();
            for (FixedAsset fixedAsset : selectedFixedAssetList) {
                /*create an movement*/
                FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
                fixedAssetMovement.setMovementDate(new Date());
                fixedAssetMovement.setCreationDate(new Date());
                // Always pendant state
                fixedAssetMovement.setState(FixedAssetMovementState.PEN);
                fixedAssetMovement.setFixedAssetMovementType(fixedAssetVoucher.getFixedAssetVoucherType());
                fixedAssetMovement.setFixedAsset(fixedAsset);
                fixedAssetMovement.setFixedAssetMovementAccount(provisionByTangibleFixedAssetObsolescenceAccountCode);
                fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
                fixedAssetMovement.setFixedAssetVoucher(fixedAssetVoucher);
                fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
                fixedAssetMovement.setCostCenter(fixedAsset.getCostCenter());
                fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
                fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
                getEntityManager().persist(fixedAssetMovement);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new EntryDuplicatedException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void approveDischarge(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException {

        validateFixedAssetsStates(fixedAssetVoucher, selectedFixedAssetList);
        validateFixedAssetMovementsStates(fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, listEm));

        FixedAssetVoucher dataBaseFixedAssetVoucher = findInDataBase(fixedAssetVoucher.getId());
        if (canChangeFixedAssetVoucher(dataBaseFixedAssetVoucher)) {

            try {
                Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, fixedAssetVoucher.getCause());
                voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());

                updateFixedAssetVoucher(fixedAssetVoucher, selectedFixedAssetList);
                List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucherAndState(fixedAssetVoucher, FixedAssetMovementState.PEN);
                for (FixedAssetMovement eventFixedAssetMovement : fixedAssetMovementList) {
                    FixedAsset fixedAsset;
                    FixedAssetMovement fixedAssetMovement;
                    fixedAsset = getEntityManager().find(FixedAsset.class, eventFixedAssetMovement.getFixedAsset().getId());
                    fixedAssetMovement = getEntityManager().find(FixedAssetMovement.class, eventFixedAssetMovement.getId());

                    fixedAsset.setState(FixedAssetState.BAJ);
                    fixedAsset.setEndDate(new Date());
                    fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));

                    fixedAssetMovement.setMovementDate(new Date());
                    fixedAssetMovement.setState(FixedAssetMovementState.APR);
                    fixedAssetMovement.setUfvAmount(
                            BigDecimalUtil.sum(fixedAsset.getImprovement(),
                                    BigDecimalUtil.subtract(
                                            fixedAsset.getUfvOriginalValue(),
                                            fixedAsset.getAcumulatedDepreciation()
                                    )
                            )
                    );
                    fixedAssetMovement.setCause(fixedAssetVoucher.getCause());
                    /* It is necessary to compute each movement because they may have different rates, because when a improvement is registered
                    its lastMonthBsSusRate and lastMonthBsUfvRate are the rates given by the date retrieved from the ARGCTC table */

                    BigDecimal bsImprovement = BigDecimal.ZERO;
                    List<FixedAssetMovement> fixedAssetItemMovementList =
                            fixedAssetMovementService.findFixedAssetMovementListByFixedAssetByMovementTypeAndState(fixedAsset, FixedAssetMovementTypeEnum.MEJ, FixedAssetMovementState.APR);
                    for (FixedAssetMovement fixedAssetMovementAux : fixedAssetItemMovementList) {
                        bsImprovement = BigDecimalUtil.multiply(fixedAssetMovementAux.getUfvAmount(), fixedAssetMovementAux.getLastMonthBsUfvRate());
                    }
                    BigDecimal bsAmount =
                            BigDecimalUtil.sum(
                                    BigDecimalUtil.multiply(
                                            BigDecimalUtil.subtract(
                                                    fixedAsset.getUfvOriginalValue(),
                                                    fixedAsset.getAcumulatedDepreciation()
                                            ),
                                            fixedAsset.getLastBsUfvRate()
                                    ),
                                    bsImprovement
                            );
                    fixedAssetMovement.setBsAmount(bsAmount);
                    fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
                    fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
                    fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
                    fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());

                    voucherForGeneration.addVoucherDetail(
                            VoucherDetailBuilder.newDebitVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                                    fixedAsset.getCostCenterCode(),
                                    fixedAssetMovement.getFixedAssetMovementCashAccount(),
                                    fixedAssetMovement.getBsAmount(),
                                    FinancesCurrencyType.P,
                                    Constants.BASE_CURRENCY_EXCHANGE_RATE));
                    voucherForGeneration.addVoucherDetail(
                            VoucherDetailBuilder.newDebitVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                                    fixedAsset.getCostCenterCode(),
                                    fixedAsset.getFixedAssetSubGroup().getAccumulatedDepreciationCashAccount(),
                                    BigDecimalUtil.multiply(fixedAsset.getAcumulatedDepreciation(),
                                            fixedAsset.getLastBsUfvRate()),
                                    FinancesCurrencyType.P, Constants.BASE_CURRENCY_EXCHANGE_RATE));
                    voucherForGeneration.addVoucherDetail(
                            VoucherDetailBuilder.newCreditVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                                    fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                                    BigDecimalUtil.sum(
                                            BigDecimalUtil.multiply(fixedAsset.getLastBsUfvRate(),
                                                    fixedAsset.getUfvOriginalValue()
                                            ),
                                            bsImprovement
                                    ),
                                    FinancesCurrencyType.P, Constants.BASE_CURRENCY_EXCHANGE_RATE));

                    if (!getEntityManager().contains(fixedAsset)) {
                        getEntityManager().merge(fixedAsset);
                    }
                    if (!getEntityManager().contains(fixedAssetMovement)) {
                        getEntityManager().merge(fixedAssetMovement);
                    }
                    getEntityManager().flush();
                }
                voucherService.create(voucherForGeneration);

                fixedAssetVoucher.setState(FixedAssetVoucherState.APR);
                fixedAssetVoucher.setUpdatedBy(currentUser);
                fixedAssetVoucher.setTransactionNumber(voucherForGeneration.getTransactionNumber());
                if (!getEntityManager().contains(fixedAssetVoucher)) {
                    getEntityManager().merge(fixedAssetVoucher);
                }
                getEntityManager().flush();

                for (FixedAssetMovement eventFixedAssetMovement : fixedAssetMovementList) {
                    FixedAssetMovement fixedAssetMovement;
                    fixedAssetMovement = getEntityManager().find(FixedAssetMovement.class, eventFixedAssetMovement.getId());
                    fixedAssetMovement.setTransactionNumber(voucherForGeneration.getTransactionNumber());
                    if (!getEntityManager().contains(fixedAssetMovement)) {
                        try {
                            getEntityManager().merge(fixedAssetMovement);
                        } catch (OptimisticLockException e) {
                            log.debug(e);
                            throw new ConcurrencyException(e);
                        } catch (PersistenceException ee) {
                            log.debug(ee);
                            throw new EntryDuplicatedException(ee);
                        }
                    }
                    getEntityManager().flush();
                }
                getEntityManager().flush();
            } catch (OptimisticLockException e) {
                throw new ConcurrencyException(e);
            } catch (PersistenceException e) {
                throw new EntryDuplicatedException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void improve(FixedAssetVoucher fixedAssetVoucher, HashMap<FixedAsset, FixedAssetMovement> selectedFixedAssetListMap, FixedAssetPayment fixedAssetPayment)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetInvalidStateException {

        validateFixedAssetsStates(fixedAssetVoucher, new ArrayList<FixedAsset>(selectedFixedAssetListMap.keySet()));

        try {

            fixedAssetPayment.setPayCurrency(FinancesCurrencyType.P);
            fixedAssetPayment.setState(FixedAssetPaymentState.PENDING);
            fixedAssetPayment.setCreationDate(new Date());

            if (fixedAssetPayment.getExchangeRate() == null) {
                fixedAssetPayment.setExchangeRate(BigDecimal.ONE);
            }
            getEntityManager().persist(fixedAssetPayment);
            getEntityManager().flush();

            assignCode(fixedAssetVoucher);
            updateCode(fixedAssetVoucher);
            cleanUnusedData(fixedAssetVoucher);
            getEntityManager().persist(fixedAssetVoucher);
            getEntityManager().flush();
            fixedAssetVoucher.setFixedAssetPayment(fixedAssetPayment);
            if (null != fixedAssetVoucher.getFixedAssetPayment() && !fixedAssetVoucher.getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                fixedAssetVoucher.getFixedAssetPayment().setCheckDestination(null);
            }

            for (FixedAsset fixedAsset : selectedFixedAssetListMap.keySet()) {
                /*create an movement*/
                FixedAssetMovement fixedAssetMovement = selectedFixedAssetListMap.get(fixedAsset);
                fixedAssetMovement.setCreationDate(new Date());
                // Always pendant state
                fixedAssetMovement.setState(FixedAssetMovementState.PEN);
                fixedAssetMovement.setFixedAssetMovementType(fixedAssetVoucher.getFixedAssetVoucherType());
                fixedAssetMovement.setFixedAsset(fixedAsset);
                fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
                fixedAssetMovement.setFixedAssetVoucher(fixedAssetVoucher);
                fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
                fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
                fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
                fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());

                fixedAssetMovement.setFixedAssetPayment(fixedAssetPayment);

                getEntityManager().persist(fixedAssetMovement);
            }
            getEntityManager().flush();

        } catch (OptimisticLockException e) {
            log.error("An unexpected error have happened rolling back", e);
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            log.error("An unexpected error have happened rolling back", e);
            throw new EntryDuplicatedException(e);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            throw new RuntimeException(e);
        }
    }

    public void approveImprovement(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList, FixedAssetPayment fixedAssetPayment)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException {

        validateFixedAssetsStates(fixedAssetVoucher, selectedFixedAssetList);
        validateFixedAssetMovementsStates(fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, listEm));

        FixedAssetVoucher dataBaseFixedAssetVoucher = findInDataBase(fixedAssetVoucher.getId());
        if (canChangeFixedAssetVoucher(dataBaseFixedAssetVoucher)) {
            try {

                updateFixedAssetVoucher(fixedAssetVoucher, selectedFixedAssetList);
                List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucherAndState(fixedAssetVoucher, FixedAssetMovementState.PEN);

                /*bank vs original value bankVsOriginalValueVoucher */

                Voucher bankVsOriginalValueVoucher = null;
                CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

                BigDecimal bankExchangeRate = fixedAssetPayment.getExchangeRate();
                BigDecimal payExchangeRate = fixedAssetPayment.getExchangeRate();


                /* if the payment currency is in $us so convert to equivalent in bs */
                BigDecimal payAmount = FinancesCurrencyType.D.equals(fixedAssetPayment.getPayCurrency()) ?
                        BigDecimalUtil.multiply(fixedAssetPayment.getPayAmount(), payExchangeRate) : fixedAssetPayment.getPayAmount();
                if (PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(fixedAssetPayment.getPaymentType())) {
                    Long sequenceNumber = sequenceGeneratorService.nextValue(Constants.FIXEDASSET_PAYMENT_DOCUMENT_SEQUENCE);
                    bankVsOriginalValueVoucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                            Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                            Constants.BANKACCOUNT_VOUCHERTYPE_DEBITNOTE_DOCTYPE,
                            Constants.FIXEDASSET_PAYMENT_DOCNUMBER_PREFFIX + sequenceNumber,
                            fixedAssetPayment.getBankAccountNumber(),
                            fixedAssetPayment.getSourceAmount(),
                            fixedAssetPayment.getSourceCurrency(),
                            bankExchangeRate,
                            fixedAssetPayment.getDescription());
                } else if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(fixedAssetPayment.getPaymentType())) {
                    bankVsOriginalValueVoucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                            Constants.CHECK_VOUCHERTYPE_FORM,
                            Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                            fixedAssetPayment.getBankAccountNumber(),
                            fixedAssetPayment.getBeneficiaryName(),
                            fixedAssetPayment.getSourceAmount(),
                            fixedAssetPayment.getSourceCurrency(),
                            bankExchangeRate,
                            fixedAssetPayment.getCheckDestination(),
                            fixedAssetPayment.getDescription());
                } else if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(fixedAssetPayment.getPaymentType())) {
                    bankVsOriginalValueVoucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, fixedAssetPayment.getDescription());
                }


                /* improve account vs original value account*/
                Voucher improveVsOriginalValueVoucher = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, fixedAssetPayment.getDescription());
                improveVsOriginalValueVoucher.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());

                for (FixedAssetMovement eventFixedAssetMovement : fixedAssetMovementList) {
                    FixedAsset fixedAsset;
                    FixedAssetMovement fixedAssetMovement;
                    fixedAsset = getEntityManager().find(FixedAsset.class, eventFixedAssetMovement.getFixedAsset().getId());
                    fixedAssetMovement = getEntityManager().find(FixedAssetMovement.class, eventFixedAssetMovement.getId());

                    fixedAssetMovement.setBsUfvRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name()));
                    fixedAssetMovement.setBsSusRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));

                    fixedAssetMovement.setLastMonthBsUfvRate(fixedAssetMovement.getBsUfvRate());
                    fixedAssetMovement.setLastMonthBsSusRate(fixedAssetMovement.getBsSusRate());
                    fixedAssetMovement.setCause(fixedAssetVoucher.getCause());
                    fixedAssetMovement.setUfvAmount(BigDecimalUtil.divide(fixedAssetMovement.getBsAmount(), fixedAssetMovement.getBsUfvRate()));
                    fixedAssetMovement.setSusAmount(BigDecimalUtil.divide(fixedAssetMovement.getBsAmount(), fixedAssetMovement.getBsSusRate()));

                    fixedAsset.setImprovement(
                            BigDecimalUtil.sum(fixedAsset.getImprovement(), fixedAssetMovement.getUfvAmount()));
                    getEntityManager().merge(fixedAsset);
                    getEntityManager().flush();

                    // Always approved state
                    fixedAssetMovement.setState(FixedAssetMovementState.APR);
                    fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
                    fixedAssetMovement.setMovementDate(new Date());
                    fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
                    fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
                    fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
                    fixedAssetMovement.setFixedAssetMovementAccount(fixedAsset.getFixedAssetSubGroup().getImprovementAccount());
                    fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));

                    fixedAssetPayment.setState(FixedAssetPaymentState.APPROVED);
                    fixedAssetPayment.setCreationDate(new Date());

                    if (fixedAssetPayment.getExchangeRate() == null) {
                        fixedAssetPayment.setExchangeRate(BigDecimal.ONE);
                    }
                    getEntityManager().persist(fixedAssetPayment);
                    getEntityManager().flush();
                    fixedAssetMovement.setFixedAssetPayment(fixedAssetPayment);


                    /********* bank vs original value account***********/
                    BigDecimal voucherAmountNationalAmount = fixedAssetMovement.getBsAmount();

                    if (bankVsOriginalValueVoucher != null) {
                        /* in amount put movement amount*/
                        bankVsOriginalValueVoucher.setUserNumber(companyConfigurationService.findDefaultTreasuryUserNumber());
                        if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(fixedAssetPayment.getPaymentType())) {
                            bankVsOriginalValueVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                                    fixedAsset.getCostCenterCode(),
                                    fixedAssetPayment.getCashBoxCashAccount(),
                                    voucherAmountNationalAmount,
                                    fixedAssetPayment.getCashBoxCashAccount().getCurrency(),
                                    bankExchangeRate));
                        }

                        bankVsOriginalValueVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                                fixedAsset.getCostCenterCode(),
                                fixedAsset.getFixedAssetSubGroup().getImprovementCashAccount(),
                                fixedAssetMovement.getBsAmount(),
                                FinancesCurrencyType.P,
                                BigDecimal.ONE));
                        BigDecimal balanceAmount = BigDecimalUtil.subtract(payAmount, voucherAmountNationalAmount);
                        if (balanceAmount.doubleValue() > 0) {
                            bankVsOriginalValueVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                                    companyConfiguration.getBalanceExchangeRateAccount(),
                                    balanceAmount,
                                    FinancesCurrencyType.P,
                                    BigDecimal.ONE));
                        } else if (balanceAmount.doubleValue() < 0) {
                            bankVsOriginalValueVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                                    companyConfiguration.getBalanceExchangeRateAccount(),
                                    balanceAmount.abs(),
                                    FinancesCurrencyType.P,
                                    BigDecimal.ONE));
                        }
                        voucherService.create(bankVsOriginalValueVoucher);

                        fixedAssetPayment.setTransactionNumber(bankVsOriginalValueVoucher.getTransactionNumber());
                        fixedAssetMovement.setTransactionNumber(bankVsOriginalValueVoucher.getTransactionNumber());
                        getEntityManager().flush();
                    }

                    /* improvement account vs original value account */
                    improveVsOriginalValueVoucher.addVoucherDetail(
                            VoucherDetailBuilder.newDebitVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                                    fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                                    fixedAssetMovement.getBsAmount(),
                                    FinancesCurrencyType.P, BigDecimal.ONE)
                    );
                    improveVsOriginalValueVoucher.addVoucherDetail(
                            VoucherDetailBuilder.newCreditVoucherDetail(
                                    fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                                    fixedAsset.getFixedAssetSubGroup().getImprovementCashAccount(),
                                    fixedAssetMovement.getBsAmount(),
                                    FinancesCurrencyType.P, BigDecimal.ONE)
                    );
                    voucherService.create(improveVsOriginalValueVoucher);
                    fixedAssetMovement.setTransactionNumber(improveVsOriginalValueVoucher.getTransactionNumber());
                    fixedAssetMovement.setState(FixedAssetMovementState.APR);


                    fixedAssetVoucher.setState(FixedAssetVoucherState.APR);
                    fixedAssetVoucher.setUpdatedBy(currentUser);
                    if (!getEntityManager().contains(fixedAsset)) {
                        getEntityManager().merge(fixedAsset);
                    }
                    if (!getEntityManager().contains(fixedAssetMovement)) {
                        getEntityManager().merge(fixedAssetMovement);
                    }
                    if (!getEntityManager().contains(fixedAssetVoucher)) {
                        getEntityManager().merge(fixedAssetVoucher);
                    }
                    getEntityManager().flush();
                }
            } catch (OptimisticLockException e) {
                log.error("An unexpected error have happened rolling back", e);
                throw new ConcurrencyException(e);
            } catch (PersistenceException e) {
                log.error("An unexpected error have happened rolling back", e);
                throw new EntryDuplicatedException(e);
            } catch (Exception e) {
                log.error("An unexpected error have happened ...", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void assignCode(FixedAssetVoucher fixedAssetVoucher) {
        fixedAssetVoucher.setVoucherCode(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.FIXEDASSET_VOUCHER_SEQUENCE)));
    }

    private void updateCode(FixedAssetVoucher fixedAssetVoucher) {
        fixedAssetVoucher.setVoucherCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.FIXEDASSET_VOUCHER_SEQUENCE)));
    }

    public Voucher createAccountEntryForApprovedFixedAssetsByPurchaseOrder(FixedAssetVoucher fixedAssetVoucher) throws CompanyConfigurationNotFoundException {
        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, fixedAssetVoucher.getCause());
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
        List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, listEm);

        for (FixedAssetMovement fixedAssetMovement : fixedAssetMovementList) {
            FixedAsset fixedAsset = fixedAssetMovement.getFixedAsset();
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                            fixedAsset.getCostCenterCode(),
                            fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                            fixedAsset.getBsOriginalValue(),
                            FinancesCurrencyType.P,
                            BigDecimal.ONE
                    )
            );
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newCreditVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                            fixedAsset.getCostCenterCode(),
                            companyConfiguration.getFixedAssetInTransitAccount(),
                            fixedAsset.getBsOriginalValue(),
                            FinancesCurrencyType.P, BigDecimal.ONE));
        }

        voucherService.create(voucherForGeneration);

        return voucherForGeneration;
    }

    public Voucher createAccountEntryForApprovedFixedAssetPartByPurchaseOrder(FixedAssetVoucher fixedAssetVoucher) throws CompanyConfigurationNotFoundException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {

        List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucherAndState(fixedAssetVoucher, FixedAssetMovementState.PEN);

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        /* improve account vs original value account*/
        Voucher improvementVoucher = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, fixedAssetVoucher.getCause());
        improvementVoucher.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
        improvementVoucher = voucherService.createBody(improvementVoucher);

        BigDecimal ufvExchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name());
        BigDecimal susExchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());

        for (FixedAssetMovement eventFixedAssetMovement : fixedAssetMovementList) {
            FixedAssetMovement fixedAssetMovement = getEntityManager().find(FixedAssetMovement.class, eventFixedAssetMovement.getId());
            fixedAssetMovement.setBsUfvRate(ufvExchangeRate);
            fixedAssetMovement.setBsSusRate(susExchangeRate);
            fixedAssetMovement.setLastMonthBsUfvRate(fixedAssetMovement.getBsUfvRate());
            fixedAssetMovement.setLastMonthBsSusRate(fixedAssetMovement.getBsSusRate());
            fixedAssetMovement.setUfvAmount(BigDecimalUtil.divide(fixedAssetMovement.getBsAmount(), fixedAssetMovement.getBsUfvRate()));
            fixedAssetMovement.setSusAmount(BigDecimalUtil.divide(fixedAssetMovement.getBsAmount(), fixedAssetMovement.getBsSusRate()));

            FixedAsset fixedAsset = getEntityManager().find(FixedAsset.class, eventFixedAssetMovement.getFixedAsset().getId());
            fixedAsset.setImprovement(BigDecimalUtil.sum(fixedAsset.getImprovement(), fixedAssetMovement.getUfvAmount()));
            getEntityManager().merge(fixedAsset);
            getEntityManager().flush();
            // Always approved state
            fixedAssetMovement.setState(FixedAssetMovementState.APR);
            fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
            fixedAssetMovement.setMovementDate(new Date());
            fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
            fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
            fixedAssetMovement.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
            fixedAssetMovement.setFixedAssetMovementAccount(fixedAsset.getFixedAssetSubGroup().getImprovementAccount());
            fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));

            getEntityManager().flush();

            /* original value account vs fixedasset in transit account */
            improvementVoucher.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                            fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                            fixedAssetMovement.getBsAmount(),
                            fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount().getCurrency(),
                            getCurrentExchangeRate(fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount().getCurrency(), susExchangeRate, ufvExchangeRate)));
            improvementVoucher.addVoucherDetail(
                    VoucherDetailBuilder.newCreditVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                            fixedAsset.getCostCenterCode(),
                            companyConfiguration.getFixedAssetInTransitAccount(),
                            fixedAssetMovement.getBsAmount(),
                            companyConfiguration.getFixedAssetInTransitAccount().getCurrency(),
                            getCurrentExchangeRate(companyConfiguration.getFixedAssetInTransitAccount().getCurrency(), susExchangeRate, ufvExchangeRate)));

            fixedAssetMovement.setTransactionNumber(improvementVoucher.getTransactionNumber());
            fixedAssetMovement.setState(FixedAssetMovementState.APR);

            if (!getEntityManager().contains(fixedAsset)) {
                getEntityManager().merge(fixedAsset);
            }
            if (!getEntityManager().contains(fixedAssetMovement)) {
                getEntityManager().merge(fixedAssetMovement);
            }
        }

        voucherService.create(improvementVoucher);

        return improvementVoucher;
    }

    @SuppressWarnings({"NullableProblems"})
    private void clearUnusedPaymentData(FixedAssetPayment fixedAssetPayment) {
        if (fixedAssetPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT)
                || fixedAssetPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
            fixedAssetPayment.setCashBoxCashAccount(null);
            fixedAssetPayment.setSourceCurrency(fixedAssetPayment.getBankAccount().getCurrency());
        }
        if (fixedAssetPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_CASHBOX)) {
            fixedAssetPayment.setBankAccount(null);
            fixedAssetPayment.setSourceCurrency(fixedAssetPayment.getCashBoxCashAccount().getCurrency());
        }
    }

    public void annulFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher)
            throws FixedAssetVoucherApprovedException,
            ConcurrencyException, EntryDuplicatedException,
            FixedAssetVoucherAnnulledException {
        FixedAssetVoucher dbFixedAssetVoucher = findFixedAssetVoucher(fixedAssetVoucher.getId());
        if (canChangeFixedAssetVoucher(dbFixedAssetVoucher)) {
            fixedAssetVoucher.setState(FixedAssetVoucherState.ANL);
            try {
                @SuppressWarnings({"NullableProblems"})
                List<FixedAssetMovement> fixedAssetMovementList = fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(fixedAssetVoucher, null);
                for (FixedAssetMovement fixedAssetMovement : fixedAssetMovementList) {
                    fixedAssetMovement.setState(FixedAssetMovementState.ANL);
                    if (!getEntityManager().contains(fixedAssetMovement)) {
                        getEntityManager().merge(fixedAssetMovement);
                    }
                }
                if (!getEntityManager().contains(fixedAssetVoucher)) {
                    getEntityManager().merge(fixedAssetVoucher);
                }
                getEntityManager().flush();
            } catch (OptimisticLockException e) {
                throw new ConcurrencyException(e);
            } catch (PersistenceException ee) {
                throw new EntryDuplicatedException(ee);
            }
        }
    }

    public FixedAssetVoucher findInDataBase(Long id) {
        FixedAssetVoucher fixedAssetVoucher = listEm.find(FixedAssetVoucher.class, id);
        if (null == fixedAssetVoucher) {
            throw new RuntimeException("Cannot find the PurchaseOrder entity for id=" + id);
        }
        return fixedAssetVoucher;
    }

    public FixedAssetVoucher findFixedAssetVoucher(Long id) {
        findInDataBase(id);
        FixedAssetVoucher fixedAssetVoucher = getEntityManager().find(FixedAssetVoucher.class, id);
        getEntityManager().refresh(fixedAssetVoucher);
        return fixedAssetVoucher;
    }

    public boolean canChangeFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher)
            throws FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException {

        if (isFixedAssetVoucherApproved(fixedAssetVoucher)) {
            findFixedAssetVoucher(fixedAssetVoucher.getId());
            throw new FixedAssetVoucherApprovedException("The fixedAssetVoucher was already approved, and cannot be changed");
        }

        if (isFixedAssetVoucherNullified(fixedAssetVoucher)) {
            findFixedAssetVoucher(fixedAssetVoucher.getId());
            throw new FixedAssetVoucherAnnulledException("The fixedAssetVoucher was already annulled, and cannot be changed");
        }
        return FixedAssetVoucherState.PEN.equals(fixedAssetVoucher.getState());
    }

    /**
     * This method is used in pre validation process, to check the state of a list of fixedAssets
     * Throws when the state of the FixedAsset do not match with the desired state
     * this exception is used only for pre validation purpose
     *
     * @param fixedAssetVoucher The voucher which contains the information of the underlying kind of movement.
     *                          According to this kind of movement the validation process detects invalid states.
     * @param fixedAssetList    The list of FixedAssets to validate it's states.
     * @throws com.encens.khipus.exception.fixedassets.FixedAssetInvalidStateException
     *          This exception holds a list of the FixedAssets
     *          which have invalid states
     */
    private void validateFixedAssetsStates(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> fixedAssetList)
            throws FixedAssetInvalidStateException {
        List<FixedAsset> invalidFixedAssetList = new ArrayList<FixedAsset>();
        for (FixedAsset fixedAsset : fixedAssetList) {
            fixedAsset = fixedAssetService.getDataBaseFixedAsset(fixedAsset);
            if ((isTransferenceMovement(fixedAssetVoucher) || isDischargeMovement(fixedAssetVoucher) || isImprovementMovement(fixedAssetVoucher))
                    && !fixedAsset.getState().equals(FixedAssetState.VIG)
                    && !fixedAsset.getState().equals(FixedAssetState.TDP)) {
                invalidFixedAssetList.add(fixedAsset);
            }
        }
        if (invalidFixedAssetList.size() > 0) {
            throw new FixedAssetInvalidStateException(invalidFixedAssetList);
        }
    }

    /**
     * This method is used in pre validation process, to check the state of a list of fixedAssetMovements
     * Throws when the state of the FixedAssetMovement do not match with the desired state
     * this exception is used only for pre validation purpose
     *
     * @param fixedAssetMovementList The list of FixedAssetMovements to validate it's states.
     * @throws com.encens.khipus.exception.fixedassets.FixedAssetMovementInvalidStateException
     *          This exception holds a list of the FixedAssetMovements
     *          which have invalid states
     */
    private void validateFixedAssetMovementsStates(List<FixedAssetMovement> fixedAssetMovementList)
            throws FixedAssetMovementInvalidStateException {
        List<FixedAssetMovement> invalidFixedAssetMovementList = new ArrayList<FixedAssetMovement>();
        for (FixedAssetMovement fixedAssetMovement : fixedAssetMovementList) {
            fixedAssetMovement = listEm.find(FixedAssetMovement.class, fixedAssetMovement.getId());
            if (!fixedAssetMovement.getState().equals(FixedAssetMovementState.PEN)) {
                invalidFixedAssetMovementList.add(fixedAssetMovement);
            }
        }
        if (invalidFixedAssetMovementList.size() > 0) {
            throw new FixedAssetMovementInvalidStateException(invalidFixedAssetMovementList);
        }
    }

    private boolean isKindOfMovement(FixedAssetVoucher fixedAssetVoucher, FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum) {
        return fixedAssetVoucher.getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(fixedAssetMovementTypeEnum);
    }

    private boolean isTransferenceMovement(FixedAssetVoucher fixedAssetVoucher) {
        return isKindOfMovement(fixedAssetVoucher, FixedAssetMovementTypeEnum.TRA);
    }

    private boolean isDischargeMovement(FixedAssetVoucher fixedAssetVoucher) {
        return isKindOfMovement(fixedAssetVoucher, FixedAssetMovementTypeEnum.BAJ);
    }

    private boolean isImprovementMovement(FixedAssetVoucher fixedAssetVoucher) {
        return isKindOfMovement(fixedAssetVoucher, FixedAssetMovementTypeEnum.MEJ);
    }

    /* It is not possible to approve a set of fixed assets if there is another fixedAssetVoucher that contains the same purchaseOrder*/

    public boolean canApproveRegistration(FixedAssetVoucher fixedAssetVoucher) {
        List<FixedAssetMovement> fixedAssetMovementList =
                fixedAssetMovementService.findFixedAssetMovementByPurchaseOrderAndState(fixedAssetVoucher.getPurchaseOrder(), FixedAssetMovementState.APR, listEm);
        return (null == fixedAssetMovementList || fixedAssetMovementList.size() <= 0);
    }

    public Boolean isFixedAssetVoucherApproved(FixedAssetVoucher instance) {
        return isFixedAssetVoucherState(instance, FixedAssetVoucherState.APR);
    }

    public Boolean isFixedAssetVoucherNullified(FixedAssetVoucher instance) {
        return isFixedAssetVoucherState(instance, FixedAssetVoucherState.ANL);
    }

    protected Boolean isFixedAssetVoucherState(FixedAssetVoucher instance, FixedAssetVoucherState state) {
        FixedAssetVoucher fixedAssetVoucher = findInDataBase(instance.getId());
        return null != fixedAssetVoucher.getState() && state.equals(fixedAssetVoucher.getState());
    }
}