package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.FinancesSequence;
import com.encens.khipus.model.finances.FinancesSequenceId;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

/**
 * FinancesSequenceServiceBean
 *
 * Genera el siguiente correlativo de finanzas por (nombre, compania) sobre la tabla
 * '_sequence', con control de concurrencia optimista (columna version de la entidad
 * FinancesSequence). Sigue el mismo patron que SequenceServiceBean (gensecuencia):
 * em.refresh para leer el estado fresco y em.lock(WRITE) que, al existir @Version,
 * fuerza el chequeo/incremento de version al hacer flush.
 *
 * Corre en REQUIRES_NEW: si dos usuarios chocan, uno recibe OptimisticLockException y
 * el llamador (FinancesPkGeneratorServiceBean) reintenta en una transaccion nueva.
 *
 * @author
 * @version 1.0
 */
@Stateless
@Name("financesSequenceService")
@AutoCreate
public class FinancesSequenceServiceBean implements FinancesSequenceService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long nextValue(String sequenceName, Long companyId) {

        FinancesSequence sequence = em.find(FinancesSequence.class, new FinancesSequenceId(sequenceName, companyId));

        if (sequence != null) {
            /** Estado fresco (evita cache viejo en un reintento) + chequeo optimista. **/
            em.refresh(sequence);
            em.lock(sequence, LockModeType.WRITE);
            sequence.setValue(sequence.getValue() + 1);
            em.merge(sequence);
        } else {
            /** Primer uso de esta secuencia en esta compania: arranca en 1. **/
            sequence = new FinancesSequence(sequenceName, companyId, 1L);
            em.persist(sequence);
        }

        em.flush();
        return sequence.getValue();
    }
}
