package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.model.employees.GenericPayroll;
import com.encens.khipus.model.finances.AccountingRecord;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.ObservableMap;
import com.encens.khipus.util.employees.AccountingRecordData;
import com.encens.khipus.util.employees.AccountingRecordMap;
import com.encens.khipus.util.employees.AccountingRecordResult;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AccountingRecordService
 *
 * @author
 * @version 1.4
 */
@Local
public interface AccountingRecordService {


    AccountingRecordResult create(Class<? extends GenericPayroll> genericPayrollClass,
                                  AccountingRecord accountingRecord,
                                  List<Long> payrollGenerationIdList,
                                  AccountingRecordMap<String, AccountingRecordData> nationalAmountForCheckDataMap,
                                  AccountingRecordMap<String, AccountingRecordData> foreignAmountForCheckDataMap,
                                  AccountingRecordMap<String, AccountingRecordData> nationalAmountForBankDataMap,
                                  AccountingRecordMap<String, AccountingRecordData> foreignAmountForBankDataMap,
                                  ObservableMap<String, BigDecimal> exchangeRateAmountMap,
                                  Map<Long, FinancesBankAccount> selectedBankAccountMap,
                                  Map<Long, FinancesCurrencyType> currencyMapPayment,
                                  String voucherGlossForGeneration,
                                  String voucherGlossForSalaryMovement,
                                  String voucherGlossForChristmasProvision,
                                  String voucherGlossForCompensationPrevision,
                                  String voucherGlossForPayment) throws RotatoryFundNullifiedException, IceCanNotBeGreaterThanAmountException, ConcurrencyException, RotatoryFundCollectionNullifiedException, RotatoryFundLiquidatedException, ExemptCanNotBeGreaterThanAmountException, CompanyConfigurationNotFoundException, ExemptPlusIceCanNotBeGreaterThanAmountException, RotatoryFundCollectionNotFoundException, CollectionSumExceedsRotatoryFundAmountException, RotatoryFundCollectionApprovedException;
}
