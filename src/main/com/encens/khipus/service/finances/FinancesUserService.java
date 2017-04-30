package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.User;

import javax.ejb.Local;

/**
 * @author
 * @version 2.1.2
 */
@Local
public interface FinancesUserService extends GenericService {
    Boolean isAvailableCode(String financeUserCode, Long userId);

    String getFinancesUserCode();

    void createFinanceUser(User khipusUser);

    Boolean isFinanceUser(Long userId);
}
