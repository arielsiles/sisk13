package com.encens.khipus.service.production;

import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.IndirectCosts;
import com.encens.khipus.model.production.IndirectCostsConfig;
import com.encens.khipus.model.production.PeriodIndirectCost;

import com.encens.khipus.model.production.ProductionOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * Created by Diego on 29/08/2014.
 */
@Stateless
@Name("periodIndirectCostService")
@AutoCreate
public class PeriodIndirectCostServiceBean implements PeriodIndirectCostService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public PeriodIndirectCost findLastPeriodIndirectCost() {
        List<PeriodIndirectCost> periodIndirectCostList = (List<PeriodIndirectCost>)em.createQuery("select periodIndirectCost from PeriodIndirectCost periodIndirectCost order by periodIndirectCost desc")
                                                                                                  .getResultList();
        return periodIndirectCostList.get(0);
    }

    @Override
    public PeriodIndirectCost findPeriodIndirect(Month month, Gestion gestion) {
        List<PeriodIndirectCost> periodIndirectCostList;
        try {
            periodIndirectCostList = (List<PeriodIndirectCost>) em.createQuery(" select periodIndirectCost from PeriodIndirectCost periodIndirectCost " +
                    " where periodIndirectCost.month = :month " +
                    " and   periodIndirectCost.gestion = :gestion " +
                    " order by periodIndirectCost desc")
                    .setParameter("month", month.getValue() + 1)
                    .setParameter("gestion", gestion)
                    .getResultList();
        }catch(NoResultException e)
        {
            return null;
        }
        if(periodIndirectCostList.size() == 0)
            return null;

        return periodIndirectCostList.get(0);
    }

    @Override
    public boolean findPeriodIndirect(PeriodIndirectCost periodIndirectCost) {
        List<IndirectCosts> indirectCosts;
        try {
            indirectCosts = (List<IndirectCosts>) em.createQuery(" select indirectCosts from IndirectCosts indirectCosts " +
                    " where indirectCosts.periodIndirectCost = :periodIndirectCost ")
                    .setParameter("periodIndirectCost", periodIndirectCost)
                    .getResultList();
        }catch(NoResultException e)
        {
            return false;
        }

        return indirectCosts.size() > 0;
    }

    @Override
    public PeriodIndirectCost findLastPeriodIndirectCostUsed() {
        List<IndirectCosts> indirectCosts = (List<IndirectCosts>)em.createQuery("select indirectCosts from IndirectCosts indirectCosts " +
                " where indirectCosts.periodIndirectCost is not null" +
                " order by indirectCosts desc")
                .getResultList();
        return  indirectCosts.get(0).getPeriodIndirectCost();
    }

    @Override
    public List<IndirectCostsConfig> findPredefinedIndirectCost() {
        List<IndirectCostsConfig> indirectCostsConfigs = (List<IndirectCostsConfig>)em.createQuery("select indirectCostsConfig from IndirectCostsConfig indirectCostsConfig " +
                " where indirectCostsConfig.predefined = true")
                .getResultList();

        return  indirectCostsConfigs;
    }

    @Override
    public boolean findPeriodIndirectCostUsed(PeriodIndirectCost periodIndirectCost) {
        List<IndirectCosts> indirectCosts;
        try {
            indirectCosts = (List<IndirectCosts>) em.createQuery(" select indirectCosts from IndirectCosts indirectCosts " +
                    " where indirectCosts.periodIndirectCost = :periodIndirectCost ")
                    .setParameter("periodIndirectCost", periodIndirectCost)
                    .getResultList();
        }catch(NoResultException e)
        {
            return false;
        }

        return indirectCosts.size() > 0;
    }
}
