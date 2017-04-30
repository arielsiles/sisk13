package com.encens.khipus.service.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.BudgetDistributionType;
import com.encens.khipus.model.budget.BudgetType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BudgetDistributionServiceBean
 *
 * @author
 * @version 2.5
 */
@Name("budgetDistributionService")
@Stateless
@AutoCreate
public class BudgetDistributionServiceBean extends GenericServiceBean implements BudgetDistributionService {

    public Boolean validateDuplicated(BudgetDistribution budgetDistribution) {
        Long count;
        if (budgetDistribution.getId() == null) {
            count = ((Long) getEntityManager().createNamedQuery("BudgetDistribution.sumDuplicated")
                    .setParameter("businessUnit", budgetDistribution.getBusinessUnit())
                    .setParameter("gestion", budgetDistribution.getGestion())
                    .setParameter("type", budgetDistribution.getType())
                    .setParameter("budgetDistributionType", BudgetDistributionType.GLOBAL).getSingleResult());
        } else {
            count = ((Long) getEntityManager().createNamedQuery("BudgetDistribution.sumDuplicatedWithoutReferences")
                    .setParameter("budgetDistribution", budgetDistribution)
                    .setParameter("businessUnit", budgetDistribution.getBusinessUnit())
                    .setParameter("gestion", budgetDistribution.getGestion())
                    .setParameter("type", budgetDistribution.getType())
                    .setParameter("budgetDistributionType", BudgetDistributionType.GLOBAL).getSingleResult());
        }
        return count == 0;
    }

    public void create(BudgetDistribution budgetDistribution, List<BudgetDistributionDetail> budgetDistributionDetailList) throws EntryDuplicatedException {
        super.create(budgetDistribution);
        for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
            budgetDistributionDetail.setBudgetDistribution(budgetDistribution);
            log.info(budgetDistributionDetail);
            super.create(budgetDistributionDetail);
        }
    }

    public void update(BudgetDistribution budgetDistribution, List<BudgetDistributionDetail> budgetDistributionDetailList) throws EntryDuplicatedException, ConcurrencyException {
        super.update(budgetDistribution);
        for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
            super.update(budgetDistributionDetail);
        }
    }

    /**
     * This method return a map of businessUnitId, businessDistributionsMap
     *
     * @param gestion The year
     * @return The result map
     */
    public Map<Long, Map<Month, Double>> getGlobalBudgetDistributionDetailsByGestion(Gestion gestion) {
        List<BudgetDistribution> queryResult = (getEntityManager().createNamedQuery("BudgetDistribution.findByGestionAndBudgetDistributionTypeAndType")
                .setParameter("gestion", gestion)
                .setParameter("budgetDistributionTypeParam", BudgetType.EXPENSE))
                .setParameter("budgetDistributionType", BudgetDistributionType.GLOBAL)
                .getResultList();
        Map<Long, Map<Month, Double>> res = new HashMap<Long, Map<Month, Double>>();
        for (BudgetDistribution budgetDistribution : queryResult) {
            Map<Month, Double> distributionPercentByMonthMap = new HashMap<Month, Double>();
            for (BudgetDistributionDetail budgetDistributionDetail : budgetDistribution.getBudgetDistributionDetailList()) {
                distributionPercentByMonthMap.put(budgetDistributionDetail.getMonth(), budgetDistributionDetail.getPercentDistribution().doubleValue());
            }
            res.put(budgetDistribution.getBusinessUnit().getId(), distributionPercentByMonthMap);
        }
        return (res);
    }

    /**
     * This method return a map of month, percent
     *
     * @param gestion The year
     * @return The result map
     */
    public Map<Month, Double> getGlobalBudgetDistributionByGestion(Gestion gestion) {
        List<BudgetDistribution> queryResult = (getEntityManager().createNamedQuery("BudgetDistribution.findByGestionAndBudgetDistributionTypeAndType")
                .setParameter("gestion", gestion)
                .setParameter("budgetDistributionTypeParam", BudgetType.EXPENSE))
                .setParameter("budgetDistributionType", BudgetDistributionType.GLOBAL)
                .getResultList();
        Map<Month, Double> res = new HashMap<Month, Double>();
        for (BudgetDistribution budgetDistribution : queryResult) {
            for (BudgetDistributionDetail budgetDistributionDetail : budgetDistribution.getBudgetDistributionDetailList()) {
                Double currentValue = res.get(budgetDistributionDetail.getMonth());
                res.put(budgetDistributionDetail.getMonth(), currentValue == null ? budgetDistributionDetail.getPercentDistribution().doubleValue() : (currentValue + budgetDistributionDetail.getPercentDistribution().doubleValue()) / 2);
            }
        }
        return (res);
    }
}
