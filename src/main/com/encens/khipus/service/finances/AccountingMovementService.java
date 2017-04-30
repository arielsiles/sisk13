package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.AccountingMovement;

import javax.ejb.Local;

/**
 * @author
 * @version 3.5
 */
@Local
public interface AccountingMovementService extends GenericService {
    AccountingMovement findByMaximumTransactionNumber(String maximumTransactionNumber);
}
