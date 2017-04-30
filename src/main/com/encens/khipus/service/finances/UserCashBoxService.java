package com.encens.khipus.service.finances;

import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashBox;
import com.encens.khipus.model.finances.CashBoxRecord;
import com.encens.khipus.model.finances.UserCashBox;
import com.encens.khipus.model.finances.UserCashBoxState;

import javax.ejb.Local;

/**
 * UserCashBox services interface
 *
 * @author:
 */

@Local
public interface UserCashBoxService {

    UserCashBox findByCashBox(CashBox cashBox);

    CashBox findByUser(User user);

    UserCashBox find(User user, CashBox cashBox);

    void update(UserCashBox userCashBox, UserCashBoxState userCashBoxState);

    void createCashBoxRecord(CashBoxRecord cashBoxRecord);

    boolean isCashierActive();
}