package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.RotatoryFundCollection;
import com.encens.khipus.model.finances.RotatoryFundPayment;
import com.encens.khipus.util.employees.RotatoryFundMigrationData;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.24
 */
@Local
public interface RotatoryFundAccountEntryService extends GenericService {

    void createRotatoryFundCollectionAccountVsBankAccountEntry(String executorUnitCode,
                                                               String costCenterCode,
                                                               CashAccount cashAccount,
                                                               BigDecimal amount,
                                                               FinancesCurrencyType currency,
                                                               RotatoryFundCollection rotatoryFundCollection)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundSpendDistributedCollectionAccountEntry(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundCollectionByPayrollAccountEntry(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount, FinancesCurrencyType defaultCurrency, RotatoryFundMigrationData rotatoryFundMigrationData)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundPaymentAccountVsBankAccountEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundPaymentAccountVsCashBoxEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundCollectionCashAccountAdjustment(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundCollectionDepositAdjustment(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException;

    void createRotatoryFundPaymentAccountVsCashAccountAdjustmentEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException;
}