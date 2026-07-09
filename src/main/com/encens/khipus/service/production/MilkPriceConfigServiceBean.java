package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.MilkPriceConfig;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

@Name("milkPriceConfigService")
@Stateless
@AutoCreate
public class MilkPriceConfigServiceBean extends ExtendedGenericServiceBean
        implements MilkPriceConfigService {

    @Override
    @SuppressWarnings("unchecked")
    public MilkPriceConfig findVigente(Date startDate, Date endDate) {
        List<MilkPriceConfig> list = getEntityManager().createQuery(
                "select c from MilkPriceConfig c " +
                " where c.state = 'ENABLE' " +
                " and c.startDate <= :startDate " +
                " and c.endDate >= :endDate " +
                " order by c.startDate desc")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MilkPriceConfig> findOverlapping(Date startDate, Date endDate, Long excludeId) {
        return getEntityManager().createQuery(
                "select c from MilkPriceConfig c " +
                " where c.state = 'ENABLE' " +
                " and c.startDate <= :endDate " +
                " and c.endDate >= :startDate " +
                " and (:excludeId is null or c.id <> :excludeId)")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("excludeId", excludeId)
                .getResultList();
    }
}
