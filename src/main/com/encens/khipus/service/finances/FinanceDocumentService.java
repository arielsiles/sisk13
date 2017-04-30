package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.AccountingMovement;
import com.encens.khipus.model.finances.AccountingMovementDetail;
import com.encens.khipus.model.finances.FinanceDocument;
import com.encens.khipus.model.finances.FinanceMovementType;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Local
public interface FinanceDocumentService extends GenericService {
    BigDecimal sumDetail(String transactionNumber, AccountingMovement accountingMovement, FinanceMovementType type);

    List<AccountingMovementDetail> findDetail(String transactionNumber, AccountingMovement accountingMovement, FinanceMovementType type);

    FinanceDocument findByTransactionNumber(String transactionNumber);
}
