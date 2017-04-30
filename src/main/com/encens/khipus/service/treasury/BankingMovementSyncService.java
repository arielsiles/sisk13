package com.encens.khipus.service.treasury;

import com.encens.khipus.model.finances.FinancesDocumentType;
import com.encens.khipus.model.treasury.BankingMovementSync;

import javax.ejb.Local;
import java.util.Map;

/**
 * BankingMovementSyncService
 *
 * @author
 * @version 2.10
 */
@Local
public interface BankingMovementSyncService {
    Boolean registerBankingMovementSync(BankingMovementSync bankingMovementSync, Map<Integer, Map<Integer, String>> mapDataContainer, Map<Integer, FinancesDocumentType> documentTypeMapping);
}
