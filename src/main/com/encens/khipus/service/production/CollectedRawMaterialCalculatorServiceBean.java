package com.encens.khipus.service.production;


import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProductiveZone;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    @SuppressWarnings("unchecked")
    public double calculateCollectedAmountBetweenDates(Date startDate, Date endDate, MetaProduct rawMaterial, int dayFilter) {
        if (dayFilter == 0) {
            return calculateCollectedAmountBetweenDates(startDate, endDate, rawMaterial);
        }
        List<Object[]> perDay = em.createNamedQuery("CollectionForm.weightedAmountPerDayByMetaProduct")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("metaProduct", rawMaterial)
                .getResultList();
        double sum = 0;
        for (Object[] row : perDay) {
            Date date = (Date) row[0];
            if (shouldIncludeDate(date, dayFilter)) {
                sum += cast((Double) row[1]);
            }
        }
        return sum;
    }

    @Override
    @SuppressWarnings("unchecked")
    public double calculateCollectedAmountBetweenDates(Date startDate, Date endDate, MetaProduct rawMaterial, ProductiveZone productiveZone, int dayFilter) {
        if (dayFilter == 0) {
            return calculateCollectedAmountBetweenDates(startDate, endDate, rawMaterial, productiveZone);
        }
        List<Object[]> perDay = em.createNamedQuery("CollectionForm.weightedAmountPerDayByMetaProductAndGAB")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("metaProduct", rawMaterial)
                .setParameter("productiveZone", productiveZone)
                .getResultList();
        double sum = 0;
        for (Object[] row : perDay) {
            Date date = (Date) row[0];
            if (shouldIncludeDate(date, dayFilter)) {
                sum += cast((Double) row[1]);
            }
        }
        return sum;
    }

    private boolean shouldIncludeDate(Date date, int dayFilter) {
        if (dayFilter == 0) return true;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        boolean isSunday = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
        if (dayFilter == 1) return !isSunday;
        if (dayFilter == 2) return isSunday;
        return true;
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
