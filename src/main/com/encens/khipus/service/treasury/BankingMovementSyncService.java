package com.encens.khipus.service.treasury;

import com.encens.khipus.model.finances.FinancesDocumentType;
import com.encens.khipus.model.treasury.BankingMovementSync;

import javax.ejb.Local;
import java.util.Map;

/**
 * BankingMovementSyncService
 *
 * OBSOLETO: importacion de extractos bancarios. Funcionalidad antigua, actualmente
 * SIN USO. Se difiere su revision/retiro a una etapa posterior.
 *
 * @author
 * @version 2.10
 * @deprecated funcionalidad antigua sin uso; pendiente de retiro en otra etapa.
 */
@Deprecated
@Local
public interface BankingMovementSyncService {
    Boolean registerBankingMovementSync(BankingMovementSync bankingMovementSync, Map<Integer, Map<Integer, String>> mapDataContainer, Map<Integer, FinancesDocumentType> documentTypeMapping);
}
