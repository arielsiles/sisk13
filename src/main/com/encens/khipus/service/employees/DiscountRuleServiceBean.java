package com.encens.khipus.service.employees;

import com.encens.khipus.action.employees.DiscountRuleForInactiveDataModel;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.DiscountRule;
import com.encens.khipus.model.employees.DiscountRuleType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Name("discountRuleService")
@Stateless
@AutoCreate
public class DiscountRuleServiceBean extends GenericServiceBean implements DiscountRuleService {

    @In(create = true)
    private DiscountRuleForInactiveDataModel discountRuleForInactiveDataModel;

    @TransactionAttribute(REQUIRES_NEW)
    public void createDiscountRule(DiscountRule discountRule) throws EntryDuplicatedException {
        if (discountRule.getActive()) {
            inactiveOthersDiscountRules(discountRule);
        }

        create(discountRule);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateDiscountRule(DiscountRule discountRule) throws ConcurrencyException, EntryDuplicatedException {
        if (discountRule.getActive()) {
            inactiveOthersDiscountRules(discountRule);
        }

        update(discountRule);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteDiscountRule(DiscountRule discountRule) throws ConcurrencyException, ReferentialIntegrityException {
        delete(discountRule);
    }

    private List<DiscountRule> findDiscountRuleForInactive(DiscountRule discountRule) {
        List<DiscountRule> discountRuleList = new ArrayList<DiscountRule>();

        //add filters
        discountRuleForInactiveDataModel.setActiveDiscountRuleId(discountRule.getId());
        discountRuleForInactiveDataModel.setGestion(discountRule.getGestion());
        discountRuleForInactiveDataModel.setBusinessUnit(discountRule.getBusinessUnit());
        discountRuleForInactiveDataModel.setJobCategory(discountRule.getJobCategory());

        discountRuleList = discountRuleForInactiveDataModel.getResultList();
        return discountRuleList;
    }

    /**
     * Set as inactive all discount rules with same properties
     *
     * @param activeDiscountRule
     */
    private void inactiveOthersDiscountRules(DiscountRule activeDiscountRule) {
        List<DiscountRule> discountRuleList = findDiscountRuleForInactive(activeDiscountRule);
        for (DiscountRule discountRule : discountRuleList) {
            try {
                DiscountRule discountRuleInactive = findById(DiscountRule.class, discountRule.getId(), true);
                discountRuleInactive.setActive(false);
            } catch (EntryNotFoundException ignore) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiscountRule> findActiveByGestionAndBusinessUnitAndJobCategory(Gestion gestion, BusinessUnit businessUnit,
                                                                               JobCategory jobCategory) {
        try {
            return (List<DiscountRule>) getEntityManager().createNamedQuery("DiscountRule.findActiveByGestionAndBusinessUnitAndJobCategory")
                    .setParameter("gestion", gestion)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("active", Boolean.TRUE)
                    .setParameter("discountRuleType", DiscountRuleType.LATENESS)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<DiscountRule>();
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiscountRule> findBusinessUnitGlobalActiveDiscountRuleByGestion(Gestion gestion, BusinessUnit businessUnit) {
        try {
            return (List<DiscountRule>) getEntityManager().createNamedQuery("DiscountRule.findBusinessUnitGlobalActiveDiscountRuleByGestion")
                    .setParameter("gestion", gestion)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("active", Boolean.TRUE)
                    .setParameter("discountRuleType", DiscountRuleType.LATENESS)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<DiscountRule>();
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiscountRule> findGlobalActiveDiscountRuleByGestion(Gestion gestion) {
        try {
            return (List<DiscountRule>) getEntityManager().createNamedQuery("DiscountRule.findGlobalActiveDiscountRuleByGestion")
                    .setParameter("gestion", gestion)
                    .setParameter("active", Boolean.TRUE)
                    .setParameter("discountRuleType", DiscountRuleType.LATENESS)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<DiscountRule>();
        }
    }

}
