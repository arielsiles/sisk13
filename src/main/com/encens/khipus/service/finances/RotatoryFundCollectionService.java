package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GenericPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.util.employees.RotatoryFundMigrationData;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.26
 */
@Local
public interface RotatoryFundCollectionService extends GenericService {
    void createRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection, CollectionDocument collectionDocument, List<RotatoryFundCollectionSpendDistribution> spendDistributionList)
            throws RotatoryFundLiquidatedException, CollectionSumExceedsRotatoryFundAmountException, RotatoryFundNullifiedException, CompanyConfigurationNotFoundException, IceCanNotBeGreaterThanAmountException, ExemptCanNotBeGreaterThanAmountException, ExemptPlusIceCanNotBeGreaterThanAmountException;

    RotatoryFundCollection findRotatoryFundCollection(Long id) throws RotatoryFundCollectionNotFoundException;

    void updateRotatoryFund(RotatoryFundCollection rotatoryFundCollection, CollectionDocument collectionDocument, FinancesCurrencyType defaultCurrency, Boolean fireAccountingEntry, RotatoryFundMigrationData rotatoryFundMigrationData)
            throws RotatoryFundLiquidatedException,
            ConcurrencyException, RotatoryFundCollectionNotFoundException, CollectionSumExceedsRotatoryFundAmountException, IceCanNotBeGreaterThanAmountException, ExemptCanNotBeGreaterThanAmountException, ExemptPlusIceCanNotBeGreaterThanAmountException, RotatoryFundNullifiedException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionApprovedException, CompanyConfigurationNotFoundException;

    void deleteRotatoryFund(RotatoryFundCollection entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            RotatoryFundCollectionNotFoundException;

    Date getMaxRotatoryFundCollectionDate(RotatoryFund rotatoryFund);

    BigDecimal getCollectionSum(RotatoryFund rotatoryFund);

    BigDecimal getCollectionSumButCurrent(RotatoryFund rotatoryFund, RotatoryFundCollection rotatoryFundCollection);

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundCollection> findRotatoryFundCollectionByGestionPayroll(GestionPayroll gestionPayroll);

    @SuppressWarnings(value = "unchecked")
    void deleteRotatoryFundByGestionPayroll(GestionPayroll gestionPayroll);

    @TransactionAttribute(REQUIRES_NEW)
    void approveRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection, CollectionDocument collectionDocument, Boolean fireAccountingEntry)
            throws RotatoryFundNullifiedException, IceCanNotBeGreaterThanAmountException,
            ConcurrencyException, RotatoryFundLiquidatedException, ExemptCanNotBeGreaterThanAmountException,
            ExemptPlusIceCanNotBeGreaterThanAmountException, CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundCollectionNotFoundException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionApprovedException, CompanyConfigurationNotFoundException, RotatoryFundCollectionSpendDistributionEmptyException, RotatoryFundCollectionSpendDistributionSumIsNotTotalException, RotatoryFundCollectionStateException, DuplicatedFinanceAccountingDocumentException;

    RotatoryFundCollection findDatabaseRotatoryFundCollection(Long id) throws RotatoryFundCollectionNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void annulRotatoryFundCollection(RotatoryFundCollection entity)
            throws RotatoryFundCollectionNotFoundException, RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException, RotatoryFundCollectionNullifiedException,
            RotatoryFundCollectionApprovedException, ConcurrencyException;

    Boolean isRotatoryFundCollectionApproved(RotatoryFundCollection instance);

    Boolean isRotatoryFundCollectionNullified(RotatoryFundCollection instance);

    boolean canChangeRotatoryFundCollection(RotatoryFundCollection entity)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException, RotatoryFundNullifiedException, RotatoryFundCollectionNotFoundException, RotatoryFundCollectionApprovedException, RotatoryFundCollectionNullifiedException;

    @TransactionAttribute(REQUIRES_NEW)
    void approveRotatoryFundCollectionsByPayroll(RotatoryFundMigrationData rotatoryFundMigrationData, GeneratedPayroll generatedPayroll, Class<? extends GenericPayroll> entityClass, List<Long> payrollGenerationIdList, Map<Long, FinancesCurrencyType> currencyMapCollectionByEmployee)
            throws RotatoryFundNullifiedException, IceCanNotBeGreaterThanAmountException,
            ConcurrencyException, RotatoryFundLiquidatedException, ExemptCanNotBeGreaterThanAmountException,
            ExemptPlusIceCanNotBeGreaterThanAmountException, CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundCollectionNotFoundException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionApprovedException, CompanyConfigurationNotFoundException;

    Long getNextCodeNumber();

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundCollection> getEventRotatoryFundCollectionListByState(RotatoryFund rotatoryFund, RotatoryFundCollectionState rotatoryFundCollectionState);

    void generateCollectionForPurchaseOrderPayment(PurchaseOrderPayment purchaseOrderPayment)
            throws IceCanNotBeGreaterThanAmountException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            ExemptCanNotBeGreaterThanAmountException,
            CompanyConfigurationNotFoundException,
            ExemptPlusIceCanNotBeGreaterThanAmountException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundCollectionNullifiedException,
            ConcurrencyException,
            RotatoryFundCollectionStateException,
            RotatoryFundCollectionSpendDistributionEmptyException,
            DuplicatedFinanceAccountingDocumentException,
            RotatoryFundCollectionSpendDistributionSumIsNotTotalException,
            RotatoryFundCollectionNotFoundException,
            RotatoryFundCollectionApprovedException;

    RotatoryFundCollection buildRotatoryFundCollection(GestionPayroll gestionPayroll, Quota quota,
                                                       BigDecimal exchangeRate, double discount, FinancesCurrencyType currency);
}