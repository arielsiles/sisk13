package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.FinancesBankAccountPk;
import com.encens.khipus.model.finances.FinancesCurrencyType;

import javax.ejb.Local;
import java.util.List;

/**
 * FinancesBankAccountService
 *
 * @author
 * @version 2.0
 */
@Local
public interface FinancesBankAccountService {
    List<FinancesBankAccount> findByCurrencyType(FinancesCurrencyType financesCurrencyType);

    FinancesBankAccount findFinancesBankAccount(FinancesBankAccountPk financesBankAccountPk);
}
