package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.VoucherDetail;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Credit service interface
 *
 * @author
 */

@Local
public interface AccountService {

    List<VoucherDetail> getAccountDetailList(Account account);
    BigDecimal calculateAccountBalance(Account account, Date startDate, Date endDate);
    List<VoucherDetail> getMovementAccountBetweenDates(Account account, Date startDate, Date endDate);
    List<Account> getAccountList();
    List<Account> getSavingsAccounts(SavingType savingType);
    List<Account> getSavingsAccounts(SavingType savingType, FinancesCurrencyType currencyType);
    List<Account> getAccountList(Partner partner);

}
