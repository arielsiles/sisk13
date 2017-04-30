package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleOverlapException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.VacationRule;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Name("vacationRuleService")
@Stateless
@AutoCreate
public class VacationRuleServiceBean extends GenericServiceBean implements VacationRuleService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createVacationRule(VacationRule vacationRule) throws EntryDuplicatedException, VacationRuleOverlapException {
        vacationRule.setCode(sequenceGeneratorService.nextValue(Constants.VACATIONRULE_CODE_SEQUENCE));
        validateRuleOverlap(vacationRule);

        create(vacationRule);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateVacationRule(VacationRule vacationRule) throws ConcurrencyException, EntryDuplicatedException, VacationRuleOverlapException {
        update(vacationRule);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteVacationRule(VacationRule vacationRule) throws ConcurrencyException, ReferentialIntegrityException {
        delete(vacationRule);
    }

    /**
     * find last vacation rule
     * @return VacationRule
     */
    public VacationRule findLastVacationRule() {
        VacationRule lastVacationRule = null;
        List resultList = (List) getEntityManager().createNamedQuery("VacationRule.findAllOrderByCode")
                .getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            lastVacationRule = (VacationRule) resultList.get(0);
        }
        return lastVacationRule;
    }

    public VacationRule findBySeniorityYear(Integer seniorityYear) {
        VacationRule vacationRule = null;

        try {
            vacationRule = (VacationRule) getEntityManager().createNamedQuery("VacationRule.findBySeniorityYear")
                    .setParameter("seniorityYear", seniorityYear)
                    .getSingleResult();
        } catch (NoResultException ignore) {
        }

        return vacationRule;
    }

    /**
     * Validate if exist other rules registered with same years ranges
     * @param vacationRule
     * @throws VacationRuleOverlapException
     */
    private void validateRuleOverlap(VacationRule vacationRule) throws VacationRuleOverlapException {
        Long vacationRuleId = vacationRule.getId() != null ? vacationRule.getId() : -1l;
        List resultList = (List) getEventEntityManager().createNamedQuery("VacationRule.findByRangeOverlap")
                .setParameter("vacationRuleId", vacationRuleId)
                .setParameter("fromYears", vacationRule.getFromYears())
                .getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            VacationRule overlapVacationRule = (VacationRule) resultList.get(0);
            throw new VacationRuleOverlapException(overlapVacationRule.getName());
        }
    }

}
