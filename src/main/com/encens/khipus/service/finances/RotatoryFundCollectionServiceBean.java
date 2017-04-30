package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GenericPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.employees.RotatoryFundMigrationData;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("rotatoryFundCollectionService")
@AutoCreate
public class RotatoryFundCollectionServiceBean extends GenericServiceBean implements RotatoryFundCollectionService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private RotatoryFundService rotatoryFundService;
    @In
    private QuotaService quotaService;
    @In
    private RotatoryFundAccountEntryService rotatoryFundAccountEntryService;
    @In
    private RotatoryFundCollectionSpendDistributionService rotatoryFundCollectionSpendDistributionService;
    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private FinanceAccountingDocumentService financeAccountingDocumentService;
    @In
    private User currentUser;
    @In
    protected Map<String, String> messages;

    @TransactionAttribute(REQUIRES_NEW)
    public void createRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection,
                                             CollectionDocument collectionDocument,
                                             List<RotatoryFundCollectionSpendDistribution> spendDistributionList)
            throws RotatoryFundLiquidatedException, CollectionSumExceedsRotatoryFundAmountException, RotatoryFundNullifiedException, CompanyConfigurationNotFoundException, IceCanNotBeGreaterThanAmountException, ExemptCanNotBeGreaterThanAmountException, ExemptPlusIceCanNotBeGreaterThanAmountException {
        rotatoryFundCollection.setCode(getNextCodeNumber().intValue());
        RotatoryFund rotatoryFund = rotatoryFundCollection.getRotatoryFund();
        rotatoryFundCollection.setCreationDate(new Date());
        /* update document info*/
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
            collectionDocument.setAmount(rotatoryFundCollection.getCollectionAmount());
            collectionDocument.setNetAmount(rotatoryFundCollection.getCollectionAmount());
            collectionDocument.setIva(BigDecimal.ZERO);
            if (collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE)) {
                collectionDocument.setNetAmount(
                        BigDecimalUtil.toBigDecimal(
                                rotatoryFundCollection.getCollectionAmount().doubleValue()
                                        - collectionDocument.getExempt().doubleValue() -
                                        collectionDocument.getIce().doubleValue()));
                collectionDocument.setIva(BigDecimalUtil.multiply(collectionDocument.getNetAmount(), Constants.VAT));
                if (collectionDocument.getIce().compareTo(collectionDocument.getAmount()) > 0) {
                    throw new IceCanNotBeGreaterThanAmountException("Ice can not be greater than amount");
                }
                if (collectionDocument.getExempt().compareTo(collectionDocument.getAmount()) > 0) {
                    throw new ExemptCanNotBeGreaterThanAmountException("Exempt can not be greater than amount");
                }
                if ((collectionDocument.getExempt().doubleValue() +
                        collectionDocument.getIce().doubleValue()) > collectionDocument.getAmount().doubleValue()) {
                    throw new ExemptPlusIceCanNotBeGreaterThanAmountException("Exempt plus ice can not be greater than amount");
                }
            }
        }

        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund)) {
            BigDecimal collectionSum = getCollectionSum(rotatoryFund);
            BigDecimal total = BigDecimalUtil.sum(collectionSum, rotatoryFundCollection.getCollectionAmount());
            if (total.compareTo(rotatoryFund.getAmount()) > 0) {
                throw new CollectionSumExceedsRotatoryFundAmountException("The collections amount sum exceeds the rotatory fund amount");
            }
            try {
                /* persist associated document if it have one associated*/
                if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
                    getEntityManager().persist(collectionDocument);
                    getEntityManager().flush();
                    rotatoryFundCollection.setCollectionDocument(collectionDocument);
                    //noinspection NullableProblems
                    rotatoryFundCollection.setBankDepositNumber(null);
                }
                super.create(rotatoryFundCollection);

                if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
                    for (RotatoryFundCollectionSpendDistribution spendDistribution : spendDistributionList) {
                        if (spendDistribution.hasValues()) {
                            spendDistribution.setRotatoryFundCollection(rotatoryFundCollection);
                            rotatoryFundCollection.getRotatoryFundCollectionSpendDistributionList().add(spendDistribution);
                        }
                    }
                }

                try {
                    super.update(rotatoryFundCollection);
                } catch (ConcurrencyException e) {
                }

            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }
        }
    }

    @SuppressWarnings({"NullableProblems"})
    private void clearUnusedCollectionData(RotatoryFundCollection rotatoryFundCollection) {
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_BANK_ACCOUNT)) {
            rotatoryFundCollection.setCashBoxCashAccount(null);
            rotatoryFundCollection.setSourceCurrency(rotatoryFundCollection.getBankAccount().getCurrency());
        }
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_CASHBOX)) {
            rotatoryFundCollection.setBankAccount(null);
            rotatoryFundCollection.setSourceCurrency(rotatoryFundCollection.getCashBoxCashAccount().getCurrency());
        }
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
            FinancesCurrencyType currency = rotatoryFundCollection.getCollectionCurrency();
            rotatoryFundCollection.setBankAccount(null);
            rotatoryFundCollection.setCashBoxCashAccount(null);
            rotatoryFundCollection.setSourceCurrency(currency);
        }
    }

    private void fireAccountingEntry(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount, FinancesCurrencyType defaultCurrency, RotatoryFundMigrationData rotatoryFundMigrationData) throws CompanyConfigurationNotFoundException {
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_BANK_ACCOUNT)) {
            rotatoryFundAccountEntryService.createRotatoryFundCollectionAccountVsBankAccountEntry(
                    rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode(),
                    rotatoryFundCollection.getRotatoryFund().getCostCenterCode(),
                    cashAccount,
                    rotatoryFundCollection.getSourceAmount(),
                    rotatoryFundCollection.getSourceCurrency(),
                    rotatoryFundCollection);
        } else if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
            rotatoryFundAccountEntryService.createRotatoryFundSpendDistributedCollectionAccountEntry(rotatoryFundCollection, cashAccount);
        } else if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_BY_PAYROLL)) {
            rotatoryFundAccountEntryService.createRotatoryFundCollectionByPayrollAccountEntry(rotatoryFundCollection, cashAccount, defaultCurrency, rotatoryFundMigrationData);
        } else if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_CASH_ACCOUNT_ADJ)) {
            rotatoryFundAccountEntryService.createRotatoryFundCollectionCashAccountAdjustment(rotatoryFundCollection, cashAccount);
        } else if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_DEPOSIT_ADJ)) {
            rotatoryFundAccountEntryService.createRotatoryFundCollectionDepositAdjustment(rotatoryFundCollection, cashAccount);
        }
    }

    public RotatoryFundCollection findRotatoryFundCollection(Long id) throws RotatoryFundCollectionNotFoundException {
        findInDataBase(id);
        RotatoryFundCollection rotatoryFundCollection = getEntityManager().find(RotatoryFundCollection.class, id);
        getEntityManager().refresh(rotatoryFundCollection);
        return rotatoryFundCollection;
    }

    public RotatoryFundCollection findDatabaseRotatoryFundCollection(Long id) throws RotatoryFundCollectionNotFoundException {
        RotatoryFundCollection rotatoryFundCollection = listEm.find(RotatoryFundCollection.class, id);
        if (rotatoryFundCollection == null) {
            throw new RotatoryFundCollectionNotFoundException("Cannot find the RotatoryFundCollection entity for id=" + id);
        }
        return rotatoryFundCollection;
    }

    public void updateRotatoryFund(RotatoryFundCollection rotatoryFundCollection, CollectionDocument collectionDocument, FinancesCurrencyType defaultCurrency, Boolean fireAccountingEntry, RotatoryFundMigrationData rotatoryFundMigrationData)
            throws RotatoryFundLiquidatedException,
            ConcurrencyException, RotatoryFundCollectionNotFoundException,
            CollectionSumExceedsRotatoryFundAmountException, IceCanNotBeGreaterThanAmountException,
            ExemptCanNotBeGreaterThanAmountException, ExemptPlusIceCanNotBeGreaterThanAmountException,
            RotatoryFundNullifiedException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionApprovedException, CompanyConfigurationNotFoundException {

        RotatoryFund rotatoryFund = rotatoryFundCollection.getRotatoryFund();
        if (rotatoryFundService.isRotatoryFundLiquidated(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(rotatoryFundCollection.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }
        /* update document info*/
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
            collectionDocument.setAmount(rotatoryFundCollection.getSourceAmount());
            collectionDocument.setNetAmount(rotatoryFundCollection.getSourceAmount());
            collectionDocument.setIva(BigDecimal.ZERO);
            if (collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE)) {
                collectionDocument.setNetAmount(
                        BigDecimalUtil.toBigDecimal(
                                rotatoryFundCollection.getSourceAmount().doubleValue()
                                        - collectionDocument.getExempt().doubleValue() -
                                        collectionDocument.getIce().doubleValue()));
                collectionDocument.setIva(BigDecimalUtil.multiply(collectionDocument.getNetAmount(), Constants.VAT));
                if (collectionDocument.getIce().compareTo(collectionDocument.getAmount()) > 0) {
                    throw new IceCanNotBeGreaterThanAmountException("Ice can not be greater than amount");
                }
                if (collectionDocument.getExempt().compareTo(collectionDocument.getAmount()) > 0) {
                    throw new ExemptCanNotBeGreaterThanAmountException("Exempt can not be greater than amount");
                }
                if ((collectionDocument.getExempt().doubleValue() +
                        collectionDocument.getIce().doubleValue()) > collectionDocument.getAmount().doubleValue()) {
                    throw new ExemptPlusIceCanNotBeGreaterThanAmountException("Exempt plus ice can not be greater than amount");
                }
            }
        }

        /* The update operation is allowed only in Pendant database State */
        if (canUpdateRotatoryFundCollection(rotatoryFundCollection, rotatoryFund)) {
            BigDecimal collectionSum = getCollectionSumButCurrent(rotatoryFund, rotatoryFundCollection);
            BigDecimal total = BigDecimalUtil.sum(collectionSum, rotatoryFundCollection.getCollectionAmount());
            double subTotal = total.doubleValue();
            if (total.compareTo(rotatoryFund.getAmount()) > 0) {
                throw new CollectionSumExceedsRotatoryFundAmountException("The collections amount sum exceeds the rotatory fund amount");
            }
            getEntityManager().merge(rotatoryFundCollection);

            /*update quota info only if state is approved this is sent by approveRotatoryFundCollection function*/
            if (rotatoryFundCollection.getState().equals(RotatoryFundCollectionState.APR)) {
                /*quotas which state is different that ANL*/
                List<Quota> quotaList = quotaService.getAllApprovedQuotaList(rotatoryFund);
                int index = 0;
                while (quotaList.size() > index && subTotal >= 0) {
                    Quota quota = quotaList.get(index);
                    if (subTotal >= quota.getAmount().doubleValue()) {
                        if (!quota.getState().equals(QuotaState.LIQ)) {
                            quota.setState(QuotaState.LIQ);
                            quota.setResidue(BigDecimal.ZERO);
                            getEntityManager().merge(quota);
                            getEntityManager().flush();
                        }
                        subTotal = subTotal - quota.getAmount().doubleValue();
                    } else if (subTotal > 0) {
                        quota.setState(QuotaState.PLI);
                        quota.setResidue(BigDecimalUtil.subtract(quota.getAmount(), BigDecimalUtil.toBigDecimal(subTotal)));
                        getEntityManager().merge(quota);
                        getEntityManager().flush();
                        subTotal = 0.0;
                    } else if (subTotal == 0) {
                        quota.setState(QuotaState.APR);
                        quota.setResidue(quota.getAmount());
                        getEntityManager().merge(quota);
                        getEntityManager().flush();
                    }
                    index++;
                }

                if (fireAccountingEntry) {
                    /* uncomment this to fire data to accounting system */
                    CashAccount cashAccount = rotatoryFundService.matchCashAccount(rotatoryFund);
                    fireAccountingEntry(rotatoryFundCollection, cashAccount, defaultCurrency, rotatoryFundMigrationData);
                }

                getEntityManager().merge(rotatoryFund);
                getEntityManager().flush();


            }
            /* persist associated document if it have one associated*/
            if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
                getEntityManager().merge(collectionDocument);
                getEntityManager().flush();
            }

            getEntityManager().merge(rotatoryFund);

            //update rotatoryFund number of rotatoryFundCollections info only if it is approved
            if (rotatoryFundService.isRotatoryFundApproved(rotatoryFund)) {
                getEntityManager().merge(rotatoryFund);
            }
            getEntityManager().flush();
            getEntityManager().refresh(rotatoryFund);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void annulRotatoryFundCollection(RotatoryFundCollection entity)
            throws RotatoryFundCollectionNotFoundException, RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException, RotatoryFundCollectionNullifiedException,
            RotatoryFundCollectionApprovedException, ConcurrencyException {
        RotatoryFundCollection dbRotatoryFundCollection = findDatabaseRotatoryFundCollection(entity.getId());
        if (canUpdateRotatoryFundCollection(dbRotatoryFundCollection, dbRotatoryFundCollection.getRotatoryFund())) {
            entity.setState(RotatoryFundCollectionState.ANL);
            try {
                super.update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }
        }
    }

    private boolean canUpdateRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection, RotatoryFund rotatoryFund)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException,
            RotatoryFundCollectionApprovedException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionNotFoundException {
        RotatoryFundCollection databaseRotatoryFundCollection = findDatabaseRotatoryFundCollection(rotatoryFundCollection.getId());
        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund) && databaseRotatoryFundCollection.getState().equals(RotatoryFundCollectionState.APR)) {
            throw new RotatoryFundCollectionApprovedException("The rotatory fund collection was already approved, and can not be changed");
        }
        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund) && rotatoryFundCollection.getState().equals(RotatoryFundCollectionState.ANL)) {
            throw new RotatoryFundCollectionNullifiedException("The rotatory fund collection was already annulled, and can not be changed");
        }
        return true;
    }


    @TransactionAttribute(REQUIRES_NEW)
    public void approveRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection, CollectionDocument collectionDocument, Boolean fireAccountingEntry)
            throws RotatoryFundNullifiedException, IceCanNotBeGreaterThanAmountException,
            ConcurrencyException, RotatoryFundLiquidatedException, ExemptCanNotBeGreaterThanAmountException,
            ExemptPlusIceCanNotBeGreaterThanAmountException, CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundCollectionNotFoundException, RotatoryFundCollectionNullifiedException,
            RotatoryFundCollectionApprovedException, CompanyConfigurationNotFoundException,
            RotatoryFundCollectionSpendDistributionEmptyException, RotatoryFundCollectionSpendDistributionSumIsNotTotalException,
            RotatoryFundCollectionStateException, DuplicatedFinanceAccountingDocumentException {

        validateRotatoryFundCollectionState(rotatoryFundCollection, RotatoryFundCollectionState.PEN);
        financeAccountingDocumentService.validatePK(collectionDocument);
        if ((rotatoryFundCollection.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.RECEIVABLE_FUND)
                || rotatoryFundCollection.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.PARTNER_WITHDRAWAL))
                && rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
            if (isRotatoryFundCollectionDistributionEmpty(rotatoryFundCollection)) {
                throw new RotatoryFundCollectionSpendDistributionEmptyException("The spend distribution cannot be empty");
            }
            if (rotatoryFundCollectionSpendDistributionService.getAmountRotatoryFundCollectionSpendDistributionSum(rotatoryFundCollection).compareTo(rotatoryFundCollection.getSourceAmount()) != 0) {
                throw new RotatoryFundCollectionSpendDistributionSumIsNotTotalException("The spend distribution sum is not one hundred");
            }
        }
        rotatoryFundCollection.setState(RotatoryFundCollectionState.APR);
        //noinspection NullableProblems
        updateRotatoryFund(rotatoryFundCollection, collectionDocument, null, fireAccountingEntry, null);
        if (rotatoryFundCollection.getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)
                && collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE)) {
            financeAccountingDocumentService.createFinanceAccountingDocument(collectionDocument);
        }
        rotatoryFundService.computeRotatoryFundStatistics(rotatoryFundCollection.getRotatoryFund());
        rotatoryFundService.updateLiquidatedState(rotatoryFundCollection.getRotatoryFund());
        getEntityManager().flush();
    }

    /*Approve collections generated by payroll*/

    @TransactionAttribute(REQUIRES_NEW)
    public void approveRotatoryFundCollectionsByPayroll(RotatoryFundMigrationData rotatoryFundMigrationData, GeneratedPayroll generatedPayroll, Class<? extends GenericPayroll> entityClass, List<Long> payrollGenerationIdList, Map<Long, FinancesCurrencyType> currencyMapCollectionByEmployee)
            throws RotatoryFundNullifiedException, IceCanNotBeGreaterThanAmountException,
            ConcurrencyException, RotatoryFundLiquidatedException, ExemptCanNotBeGreaterThanAmountException,
            ExemptPlusIceCanNotBeGreaterThanAmountException, CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundCollectionNotFoundException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionApprovedException, CompanyConfigurationNotFoundException {
        GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();
        /* Check if it is managers payroll*/
        List<Long> employeeIdList = generatedPayrollService.getPayPayrollEmployeeIdList(entityClass, payrollGenerationIdList);

        List<RotatoryFundCollection> rotatoryFundCollectionList = findRotatoryFundCollectionByGestionPayrollByEmployeeList(gestionPayroll, employeeIdList);
        if (!ValidatorUtil.isEmptyOrNull(rotatoryFundCollectionList)) {
            for (RotatoryFundCollection rotatoryFundCollection : rotatoryFundCollectionList) {
                rotatoryFundCollection.setState(RotatoryFundCollectionState.APR);
                rotatoryFundCollection.setApprovalDate(new Date());
                rotatoryFundCollection.setApprovedByEmployee(currentUser);
                FinancesCurrencyType defaultCurrencyCollection = currencyMapCollectionByEmployee.get(rotatoryFundCollection.getRotatoryFund().getJobContract().getContract().getEmployee().getId());
                //noinspection NullableProblems
                updateRotatoryFund(rotatoryFundCollection, null, defaultCurrencyCollection, true, rotatoryFundMigrationData);
                rotatoryFundService.computeRotatoryFundStatistics(rotatoryFundCollection.getRotatoryFund());
                rotatoryFundService.updateLiquidatedState(rotatoryFundCollection.getRotatoryFund());
            }
        }
        getEntityManager().flush();
    }

    public void deleteRotatoryFund(RotatoryFundCollection entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            RotatoryFundCollectionNotFoundException {

        findInDataBase(entity.getId());
        RotatoryFund rotatoryFund = entity.getRotatoryFund();
        try {
            if (rotatoryFundService.canChangeRotatoryFund(rotatoryFund)) {
                try {
                    super.delete(entity);
                } catch (ConcurrencyException e) {
                    throw new RotatoryFundCollectionNotFoundException(e);
                }
            }
        } catch (RotatoryFundNullifiedException e) {
            log.debug("an rotatoryFund can't be annul never");
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollection> getRotatoryFundCollectionList(RotatoryFund rotatoryFund) {
        return getEntityManager()
                .createNamedQuery("RotatoryFundCollection.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollection> findRotatoryFundCollectionByGestionPayroll(GestionPayroll gestionPayroll) {
        List<RotatoryFundCollection> rotatoryFundCollectionList = new ArrayList<RotatoryFundCollection>();
        List<RotatoryFundCollection> resultList = listEm.createNamedQuery("RotatoryFundCollection.findRotatoryFundCollectionByGestionPayroll")
                .setParameter("gestionPayroll", gestionPayroll)
                .setParameter("state", RotatoryFundCollectionState.PEN)
                .getResultList();
        if (resultList != null) {
            rotatoryFundCollectionList = resultList;
        }
        return rotatoryFundCollectionList;
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollection> findRotatoryFundCollectionByGestionPayrollByEmployeeList(GestionPayroll gestionPayroll, List<Long> employeeIdList) {
        return getEntityManager().createNamedQuery("RotatoryFundCollection.findRotatoryFundCollectionByGestionPayrollByEmployeeList")
                .setParameter("gestionPayroll", gestionPayroll)
                .setParameter("state", RotatoryFundCollectionState.PEN)
                .setParameter("employeeIdList", employeeIdList)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollection> getEventRotatoryFundCollectionListByState(RotatoryFund rotatoryFund, RotatoryFundCollectionState rotatoryFundCollectionState) {
        return listEm
                .createNamedQuery("RotatoryFundCollection.findByRotatoryFundByState")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("rotatoryFundCollectionState", rotatoryFundCollectionState).getResultList();
    }

    public Date getMaxRotatoryFundCollectionDate(RotatoryFund rotatoryFund) {
        return (Date) getEntityManager()
                .createNamedQuery("RotatoryFundCollection.findMaxDateByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getSingleResult();
    }

    public BigDecimal getCollectionSum(RotatoryFund rotatoryFund) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundCollection.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", RotatoryFundCollectionState.APR).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getCollectionSumButCurrent(RotatoryFund rotatoryFund, RotatoryFundCollection rotatoryFundCollection) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundCollection.findSumByRotatoryFundButCurrent")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("id", rotatoryFundCollection.getId())
                .setParameter("state", RotatoryFundCollectionState.APR).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    private RotatoryFundCollection findInDataBase(Long id) throws RotatoryFundCollectionNotFoundException {
        RotatoryFundCollection rotatoryFundCollection = listEm.find(RotatoryFundCollection.class, id);
        if (null == rotatoryFundCollection) {
            throw new RotatoryFundCollectionNotFoundException("Cannot find the RotatoryFundCollection entity for id=" + id);
        }

        return rotatoryFundCollection;
    }

    @SuppressWarnings(value = "unchecked")
    public void deleteRotatoryFundByGestionPayroll(GestionPayroll gestionPayroll) {
        getEntityManager()
                .createNamedQuery("RotatoryFundCollection.deleteRotatoryFundByGestionPayroll")
                .setParameter("gestionPayroll", gestionPayroll)
                .setParameter("state", RotatoryFundCollectionState.PEN).executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    public void annulPendantRotatoryFundCollections(RotatoryFund rotatoryFund) {
        getEntityManager()
                .createNamedQuery("RotatoryFundCollection.annulPendantRotatoryFundCollections")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("databaseState", RotatoryFundCollectionState.PEN)
                .setParameter("state", RotatoryFundCollectionState.ANL).executeUpdate();
    }

    public Boolean isRotatoryFundCollectionApproved(RotatoryFundCollection instance) {
        return isRotatoryFundCollectionState(instance, RotatoryFundCollectionState.APR);
    }

    public void validateRotatoryFundCollectionState(RotatoryFundCollection rotatoryFundCollection, RotatoryFundCollectionState rotatoryFundCollectionState)
            throws RotatoryFundCollectionNotFoundException, RotatoryFundCollectionStateException {
        RotatoryFundCollection databaseRotatoryFundCollection = findDatabaseRotatoryFundCollection(rotatoryFundCollection.getId());
        if (!databaseRotatoryFundCollection.getState().equals(rotatoryFundCollectionState)) {
            throw new RotatoryFundCollectionStateException(databaseRotatoryFundCollection.getState());
        }
    }

    public Boolean isRotatoryFundCollectionNullified(RotatoryFundCollection instance) {
        return isRotatoryFundCollectionState(instance, RotatoryFundCollectionState.ANL);
    }

    protected Boolean isRotatoryFundCollectionState(RotatoryFundCollection instance, RotatoryFundCollectionState state) {
        RotatoryFundCollection rotatoryFundCollection;
        try {
            rotatoryFundCollection = findInDataBase(instance.getId());
        } catch (RotatoryFundCollectionNotFoundException e) {
            return false;
        }
        return null != rotatoryFundCollection.getState() && state.equals(rotatoryFundCollection.getState());
    }

    public boolean canChangeRotatoryFundCollection(RotatoryFundCollection entity)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException, RotatoryFundNullifiedException, RotatoryFundCollectionNotFoundException, RotatoryFundCollectionApprovedException, RotatoryFundCollectionNullifiedException {

        if (isRotatoryFundCollectionApproved(entity)) {
            findRotatoryFundCollection(entity.getId());
            throw new RotatoryFundCollectionApprovedException("The rotatoryFundCollection was already approved, and cannot be changed");
        }
        if (isRotatoryFundCollectionNullified(entity)) {
            findRotatoryFundCollection(entity.getId());
            throw new RotatoryFundCollectionNullifiedException("The rotatoryFundCollection was already nullified, and cannot be changed");
        }
        return RotatoryFundCollectionState.PEN.equals(entity.getState());
    }

    protected Boolean isRotatoryFundCollectionDistributionEmpty(RotatoryFundCollection rotatoryFundCollection) {
        List<RotatoryFundCollectionSpendDistribution> rotatoryFundCollectionSpendDistributionList = rotatoryFundCollectionSpendDistributionService.getRotatoryFundCollectionSpendDistributionList(rotatoryFundCollection);
        return rotatoryFundCollectionSpendDistributionList == null || rotatoryFundCollectionSpendDistributionList.isEmpty();
    }

    public Long getNextCodeNumber() {
        Integer codeNumber = ((Integer) listEm.createNamedQuery("RotatoryFundCollection.maxNumber").getSingleResult());
        if (null == codeNumber) {
            codeNumber = 1;
        } else {
            codeNumber++;
        }
        return codeNumber.longValue();
    }

    @SuppressWarnings({"NullableProblems"})
    public void generateCollectionForPurchaseOrderPayment(PurchaseOrderPayment purchaseOrderPayment)
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
            RotatoryFundCollectionApprovedException {
        RotatoryFundCollection rotatoryFundCollection = new RotatoryFundCollection();
        rotatoryFundCollection.setRegisterEmployee(currentUser);
        rotatoryFundCollection.setState(RotatoryFundCollectionState.PEN);
        rotatoryFundCollection.setCreationDate(new Date());
        rotatoryFundCollection.setDescription(purchaseOrderPayment.getDescription());

        rotatoryFundCollection.setPurchaseOrderPayment(purchaseOrderPayment);

        rotatoryFundCollection.setCollectionDate(new Date());
        rotatoryFundCollection.setRotatoryFund(purchaseOrderPayment.getRotatoryFund());
        rotatoryFundCollection.setRotatoryFundCollectionType(RotatoryFundCollectionType.COLLECTION_BY_PURCHASE_ORDER);

        rotatoryFundCollection.setExchangeRate(purchaseOrderPayment.getExchangeRate());
        rotatoryFundCollection.setSourceCurrency(purchaseOrderPayment.getPayCurrency());
        rotatoryFundCollection.setCollectionCurrency(purchaseOrderPayment.getSourceCurrency());
        rotatoryFundCollection.setSourceAmount(purchaseOrderPayment.getPayAmount());
        rotatoryFundCollection.setCollectionAmount(purchaseOrderPayment.getSourceAmount());

        createRotatoryFundCollection(rotatoryFundCollection, null, null);
        approveRotatoryFundCollection(rotatoryFundCollection, null, false);

        rotatoryFundCollection.setTransactionNumber(purchaseOrderPayment.getTransactionNumber());
        getEntityManager().merge(rotatoryFundCollection);
        getEntityManager().flush();
    }

    public RotatoryFundCollection buildRotatoryFundCollection(GestionPayroll gestionPayroll, Quota quota,
                                                              BigDecimal exchangeRate, double discount,
                                                              FinancesCurrencyType currency) {
        RotatoryFundCollection rotatoryFundCollection = new RotatoryFundCollection();
        rotatoryFundCollection.setRegisterEmployee(currentUser);
        rotatoryFundCollection.setState(RotatoryFundCollectionState.PEN);
        rotatoryFundCollection.setCreationDate(new Date());
        rotatoryFundCollection.setDescription(messages.get("RotatoryFundCollection.payrollGenerationGloss"));
        rotatoryFundCollection.setExchangeRate(exchangeRate);
        rotatoryFundCollection.setGestionPayroll(gestionPayroll);
        rotatoryFundCollection.setCollectionDate(new Date());
        rotatoryFundCollection.setRotatoryFund(quota.getRotatoryFund());
        rotatoryFundCollection.setRotatoryFundCollectionType(RotatoryFundCollectionType.COLLECTION_BY_PAYROLL);
        rotatoryFundCollection.setSourceCurrency(currency);
        rotatoryFundCollection.setCollectionCurrency(quota.getCurrency());
        rotatoryFundCollection.setSourceAmount(BigDecimalUtil.toBigDecimal(discount));
        rotatoryFundCollection.setCollectionAmount(
                BigDecimalUtil.toBigDecimal((discount) / exchangeRate.doubleValue()));
        rotatoryFundCollection.setQuota(quota);
        rotatoryFundCollection.setQuotaResidue(quota.getResidue());
        return rotatoryFundCollection;
    }
}