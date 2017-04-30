package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.CashAccount;

import javax.ejb.Local;

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
}
