package com.encens.khipus.service.common;

import com.encens.khipus.model.common.Sequence;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;

/**
 * SequenceServiceBean
 *
 * @author
 * @version 2.0
 */
@Name("sequenceService")
@Stateless
@AutoCreate
public class SequenceServiceBean implements SequenceService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public long createOrUpdateNextSequenceValue(String sequenceName) {
        Sequence sequence = getSequence(sequenceName);
        if (sequence != null && !ValidatorUtil.isBlankOrNull(sequence.getName())) {
            em.lock(sequence, LockModeType.WRITE);
            sequence.setValue(sequence.getValue() + 1);
            em.merge(sequence);
        } else {
            sequence = new Sequence(sequenceName);
            em.persist(sequence);
        }
        em.flush();
        return sequence.getValue();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long findNextSequenceValue(String sequenceName) {
        Sequence sequence = getSequence(sequenceName);
        return (sequence != null && !ValidatorUtil.isBlankOrNull(sequence.getName())) ? sequence.getValue() + 1 : 1;
    }

    private Sequence getSequence(String sequenceName) {
        Sequence sequence = null;
        try {
            sequence = (Sequence) em.createNamedQuery("Sequence.findByName")
                    .setParameter("sequenceName", sequenceName)
                    .getSingleResult();
        } catch (NoResultException ignored) {
        }
        if (null != sequence) {
            em.refresh(sequence);
        }
        return sequence;
    }

}
