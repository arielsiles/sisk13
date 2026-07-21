package com.encens.khipus.service.finances;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * Generacion de correlativos de finanzas por compania sobre la tabla '_sequence'
 * (entidad FinancesSequence). Analogo a SequenceService pero por (nombre, compania).
 *
 * @author
 * @version 1.0
 */
@Local
public interface FinancesSequenceService {

    /**
     * Incrementa y devuelve el siguiente valor de la secuencia (nombre, compania).
     * Corre en transaccion propia (REQUIRES_NEW) para que el reintento optimista del
     * llamador pueda repetirse en una transaccion limpia ante OptimisticLockException.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    long nextValue(String sequenceName, Long companyId);
}
