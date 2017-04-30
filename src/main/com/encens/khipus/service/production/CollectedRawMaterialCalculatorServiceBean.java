package com.encens.khipus.service.production;


import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProductiveZone;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;

@Name("collectedRawMaterialCalculatorService")
@Stateless
@AutoCreate
public class CollectedRawMaterialCalculatorServiceBean implements CollectedRawMaterialCalculatorService {

    @In("entityManager")
    private EntityManager em;

    @Override
    public double calculateCollectedAmountBetweenDates(Date startDate,Date endDate, MetaProduct rawMaterial,ProductiveZone productiveZone) {
        Double sum = (Double)em.createNamedQuery("CollectionForm.calculateWeightedAmountBetweebDateByMetaProductAndGAB")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("metaProduct", rawMaterial)
                .setParameter("productiveZone", productiveZone)
                .getSingleResult();
        return cast(sum);
    }

    @Override
    public double calculateCollectedAmountBetweenDates(Date startDate,Date endDate, MetaProduct rawMaterial) {
        Double sum = (Double)em.createNamedQuery("CollectionForm.calculateWeightedAmountBetweebDateByMetaProduct")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("metaProduct", rawMaterial)
                .getSingleResult();
        return cast(sum);
    }

    @Override
    public double calculateCollectedAmount(Date date, MetaProduct rawMaterial) {
        Double sum = (Double)em.createNamedQuery("CollectionForm.calculateWeightedAmountOnDateByMetaProduct")
                               .setParameter("date", date)
                               .setParameter("metaProduct", rawMaterial)
                               .getSingleResult();
        return cast(sum);
    }

    private double cast(Double value) {
        return (value == null ? 0 : value.doubleValue());
    }

    @Override
    public double calculateAvailableAmount(Date date, MetaProduct rawMaterial) {
        Double available = (Double) em.createNamedQuery("CollectionForm.calculateWeightedAmountToDateByMetaProduct")
                                      .setParameter("date", date)
                                      .setParameter("metaProduct", rawMaterial)
                                      .getSingleResult();

        Double used = (Double) em.createNamedQuery("CollectionForm.calculateUsedAmountToDateByMetaProduct")
                                 .setParameter("date", date)
                                 .setParameter("metaProduct", rawMaterial)
                                 .getSingleResult();

        return cast(available) - cast(used);
    }

    @Override
    public double calculateUsedAmount(Date date, MetaProduct rawMaterial) {
        Double used = (Double) em.createNamedQuery("CollectionForm.calculateUsedAmountOnDateByMetaProduct")
                                 .setParameter("date", date)
                                 .setParameter("metaProduct", rawMaterial)
                                 .getSingleResult();

        return cast(used);
    }
}
