package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.ProducerCollectionRestriction;
import com.encens.khipus.model.production.RawMaterialProducer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("producerCollectionRestrictionService")
@Stateless
@AutoCreate
public class ProducerCollectionRestrictionServiceBean extends ExtendedGenericServiceBean
        implements ProducerCollectionRestrictionService {

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, ProducerCollectionRestriction> preloadRestrictions(Date startDate, Date endDate) {
        List<ProducerCollectionRestriction> list = getEntityManager().createQuery(
                "select r from ProducerCollectionRestriction r " +
                " join fetch r.rawMaterialProducer " +
                " where r.state = 'ENABLE' " +
                " and r.startDate <= :startDate " +
                " and r.endDate >= :endDate")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .getResultList();

        Map<Long, ProducerCollectionRestriction> result = new HashMap<Long, ProducerCollectionRestriction>();
        for (ProducerCollectionRestriction r : list) {
            result.put(r.getRawMaterialProducer().getId(), r);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProducerCollectionRestriction> findOverlapping(RawMaterialProducer producer, Date startDate, Date endDate, Long excludeId) {
        return getEntityManager().createQuery(
                "select r from ProducerCollectionRestriction r " +
                " where r.state = 'ENABLE' " +
                " and r.rawMaterialProducer = :producer " +
                " and r.startDate <= :endDate " +
                " and r.endDate >= :startDate " +
                " and (:excludeId is null or r.id <> :excludeId)")
                .setParameter("producer", producer)
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("excludeId", excludeId)
                .getResultList();
    }
}
