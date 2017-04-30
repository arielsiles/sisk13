package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashBox;
import com.encens.khipus.model.finances.CashBoxTransaction;

import javax.ejb.Local;

/**
 * CashBoxTransaction services interface
 *
 * @author:
 */

@Local
public interface CashBoxTransactionService {

    void openCashBox(CashBox cashBox, User currentUser) throws EntryDuplicatedException, ConcurrencyException;

    void closeCashBox(CashBox cashBox) throws ConcurrencyException;

    CashBoxTransaction findByCashBox(CashBox cashBox);

    CashBoxTransaction findByCashBoxUser(User user);

    boolean cashBoxOpen(CashBox cashBox);

    boolean cashBoxClosedToday(CashBox cashBox);

}
