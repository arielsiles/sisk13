package com.encens.khipus.service.common;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * SequenceGeneratorService
 *
 * @author
 * @version 2.0
 */
@Local
public interface SequenceGeneratorService {
    long nextValue(String sequenceName);

    long findNextSequenceValue(String sequenceName);

    @TransactionAttribute(REQUIRES_NEW)
    long forceNextValue(String sequenceName);

    void nextCreditNumber(Long partnerId);
}
