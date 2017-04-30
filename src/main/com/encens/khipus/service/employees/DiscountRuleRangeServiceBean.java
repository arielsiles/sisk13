package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.DiscountRuleRangeOverlapException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.DiscountRule;
import com.encens.khipus.model.employees.DiscountRuleRange;
import com.encens.khipus.model.employees.IntervalType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Name("discountRuleRangeService")
@Stateless
@AutoCreate
public class DiscountRuleRangeServiceBean extends GenericServiceBean implements DiscountRuleRangeService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createDiscountRuleRange(DiscountRuleRange discountRuleRange) throws EntryDuplicatedException, DiscountRuleRangeOverlapException {
        discountRuleRange.setSequence(sequenceGeneratorService.nextValue(Constants.DISCOUNTRULERANGE_SEQUENCE));
        validateRangeOverlap(discountRuleRange);
        create(discountRuleRange);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateDiscountRuleRange(DiscountRuleRange discountRuleRange) throws ConcurrencyException, EntryDuplicatedException, DiscountRuleRangeOverlapException {
        update(discountRuleRange);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteDiscountRuleRange(DiscountRuleRange discountRuleRange) throws ConcurrencyException, ReferentialIntegrityException {
        delete(discountRuleRange);
    }

    public List<DiscountRuleRange> findByDiscountRule(DiscountRule discountRule) {
        List<DiscountRuleRange> resultList;
        resultList = (List<DiscountRuleRange>) getEntityManager().createNamedQuery("DiscountRuleRange.findByDiscountRule")
                .setParameter("discountRuleId", discountRule.getId())
                .getResultList();

        return resultList;
    }

    /**
     * find last range rule
     *
     * @return DiscountRuleRange
     */
    public DiscountRuleRange findLastDiscountRuleRange(DiscountRule discountRule) {
        DiscountRuleRange lastDiscountRuleRange = null;
        List resultList = (List<DiscountRuleRange>) getEntityManager().createNamedQuery("DiscountRuleRange.findByDiscountRuleOrderBySequence")
                .setParameter("discountRuleId", discountRule.getId())
                .getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            lastDiscountRuleRange = (DiscountRuleRange) resultList.get(0);
        }
        return lastDiscountRuleRange;
    }

    /**
     * Validate if exist other rules registered with same ranges
     *
     * @param discountRuleRange a given rule range
     * @throws DiscountRuleRangeOverlapException
     *          exception to throw
     */
    private void validateRangeOverlap(DiscountRuleRange discountRuleRange) throws DiscountRuleRangeOverlapException {
        if (!IntervalType.OVERLAP.equals(discountRuleRange.getDiscountRule().getIntervalType())) {

            Long discountRuleRangeId = discountRuleRange.getId() != null ? discountRuleRange.getId() : -1l;
            List resultList = getEventEntityManager().createNamedQuery("DiscountRuleRange.findByRangeOverlap")
                    .setParameter("discountRuleRangeId", discountRuleRangeId)
                    .setParameter("discountRuleId", discountRuleRange.getDiscountRule().getId())
                    .setParameter("initRange", discountRuleRange.getInitRange())
                    .getResultList();

            if (resultList != null && !resultList.isEmpty()) {
                DiscountRuleRange overlapDiscountRuleRange = (DiscountRuleRange) resultList.get(0);
                throw new DiscountRuleRangeOverlapException(overlapDiscountRuleRange.getName());
            }
        }
    }

    public DiscountRuleRange findDiscountRuleRangeInList(Integer minutes, List<DiscountRule> discountRuleList,
                                                         Map<Integer, DiscountRuleRange> minuteDiscountRuleRangeMap) {
        for (DiscountRule discountRule : discountRuleList) {
            for (DiscountRuleRange discountRuleRange : discountRule.getDiscountRuleRangeList()) {
                if (discountRuleRange.getInitRange().compareTo(minutes) <= 0
                        && (null == discountRuleRange.getEndRange() || discountRuleRange.getEndRange().compareTo(minutes) >= 0)) {
                    minuteDiscountRuleRangeMap.put(minutes, discountRuleRange);
                    return discountRuleRange;
                }
            }
        }
        return null;
    }

}
