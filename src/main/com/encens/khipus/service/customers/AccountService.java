package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
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
    List<VoucherDetail> getMovementAccountBetweenDates(Account account, Date startDate, Date endDate);
    List<Account> getAccountList();

}
