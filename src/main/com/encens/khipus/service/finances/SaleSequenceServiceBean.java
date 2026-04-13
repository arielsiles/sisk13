package com.encens.khipus.service.finances;

import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Generador de secuencias con transaccion independiente (REQUIRES_NEW).
 * Cada llamada abre su propia transaccion, incrementa el contador y hace commit,
 * liberando el lock de la fila en ~1ms.
 *
 * Usa la tabla _sequence (seq_name, seq_val) existente, compatible con la funcion
 * MySQL getNextSeq() que reemplaza.
 */
@Stateless
@Name("saleSequenceService")
@AutoCreate
public class SaleSequenceServiceBean implements SaleSequenceService {

    @PersistenceContext(unitName = "khipus")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long getNextValue(String sequenceName) {
        String schema = Constants.FINANCES_SCHEMA;

        int updated = em.createNativeQuery(
                "UPDATE " + schema + "._sequence SET seq_val = seq_val + 1 WHERE seq_name = :name")
                .setParameter("name", sequenceName)
                .executeUpdate();

        if (updated == 0) {
            em.createNativeQuery(
                    "INSERT INTO " + schema + "._sequence (seq_name, seq_val) VALUES (:name, 1)")
                    .setParameter("name", sequenceName)
                    .executeUpdate();
            return 1;
        }

        Number result = (Number) em.createNativeQuery(
                "SELECT seq_val FROM " + schema + "._sequence WHERE seq_name = :name")
                .setParameter("name", sequenceName)
                .getSingleResult();

        return result.longValue();
    }
}
