package com.encens.khipus.service.finances;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

/**
 * FinancesSequenceServiceBean
 *
 * Genera el siguiente correlativo de finanzas por (nombre, compania) sobre la tabla
 * '_sequence'. El incremento se hace con SQL nativo ATOMICO (UPDATE ... seq_val + 1),
 * que toma lock de fila (concurrency-safe) y NO flushea el persistence context del que
 * llama. Esto es clave: el correlativo se pide en medio de otros flujos (ordenes de
 * compra, vales, ventas) que pueden tener entidades transitorias a medio guardar; un
 * em.flush() sobre el PC compartido las arrastraria y romperia (TransientObjectException).
 * Replica el comportamiento seguro de la funcion almacenada getNextSeq() a la que
 * reemplaza, pero por compania.
 *
 * Corre en REQUIRES_NEW: el numero se confirma en su propia transaccion.
 *
 * @author
 * @version 2.0
 */
@Stateless
@Name("financesSequenceService")
@AutoCreate
public class FinancesSequenceServiceBean implements FinancesSequenceService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long nextValue(String sequenceName, Long companyId) {

        /**
         * Incremento atomico de la fila (seq_name, idcompania). Una sola sentencia:
         * el motor bloquea la fila mientras la actualiza, asi dos usuarios concurrentes
         * no obtienen el mismo numero. setFlushMode(COMMIT) evita cualquier auto-flush
         * del PC antes de la consulta.
         */
        Query update = em.createNativeQuery(
                "update _sequence set seq_val = seq_val + 1, version = version + 1 " +
                " where seq_name = :name and idcompania = :company")
                .setParameter("name", sequenceName)
                .setParameter("company", companyId)
                .setFlushMode(FlushModeType.COMMIT);
        int updated = update.executeUpdate();

        if (updated == 0) {
            /** Primer uso de esta secuencia en esta compania: arranca en 1. **/
            em.createNativeQuery(
                    "insert into _sequence (seq_name, idcompania, seq_val, version) " +
                    " values (:name, :company, 1, 0)")
                    .setParameter("name", sequenceName)
                    .setParameter("company", companyId)
                    .setFlushMode(FlushModeType.COMMIT)
                    .executeUpdate();
            return 1L;
        }

        Number value = (Number) em.createNativeQuery(
                "select seq_val from _sequence where seq_name = :name and idcompania = :company")
                .setParameter("name", sequenceName)
                .setParameter("company", companyId)
                .setFlushMode(FlushModeType.COMMIT)
                .getSingleResult();

        return value.longValue();
    }
}
