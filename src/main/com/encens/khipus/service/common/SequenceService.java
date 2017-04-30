package com.encens.khipus.service.common;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * SequenceService
 *
 * @author
 * @version 2.0
 */
@Local
public interface SequenceService {
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    long createOrUpdateNextSequenceValue(String sequenceName);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    long findNextSequenceValue(String sequenceName);
}
