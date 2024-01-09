package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;

import javax.ejb.Local;
import java.util.List;

/**
 * CashAccountService
 *
 * @author
 * @version 2.0
 */
@Local
public interface CashAccountService {
    Boolean existsAccount(String accountCode);

    CashAccount findByAccountCode(String accountCode);

    Boolean isActiveAccount(String accountCode);

    public List<CashAccount> findCashAccountList();

    public List<CashAccount> findCashAccountListByType(CashAccountType accountType);

    Boolean createCashAccount(CashAccount cashAccount);

    public List<CashAccount> findCashAccountsUtil();

}
