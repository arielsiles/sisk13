package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.FinalEvaluationPunctuationRange;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * FinalEvaluationPunctuationRangeServiceBean
 *
 * @author
 * @version 2.8
 */
@Name("finalEvaluationPunctuationRangeService")
@Stateless
@AutoCreate
public class FinalEvaluationPunctuationRangeServiceBean implements FinalEvaluationPunctuationRangeService {

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public Boolean isOverlapRange(FinalEvaluationPunctuationRange punctuationRange) {
        return countBetweenRangeValue(punctuationRange, punctuationRange.getStartRange()) > 0 ||
                countBetweenRangeValue(punctuationRange, punctuationRange.getEndRange()) > 0;
    }

    public Boolean isDuplicatedByName(FinalEvaluationPunctuationRange punctuationRange) {
        Long countResult = (punctuationRange.getId() == null) ?
                (Long) listEm.createNamedQuery("FinalEvaluationPunctuationRange.countName")
                        .setParameter("finalEvaluationForm", punctuationRange.getFinalEvaluationForm())
                        .setParameter("name", punctuationRange.getName()).getSingleResult() :
                (Long) listEm.createNamedQuery("FinalEvaluationPunctuationRange.countByPunctuationRangeName")
                        .setParameter("punctuationRange", punctuationRange)
                        .setParameter("finalEvaluationForm", punctuationRange.getFinalEvaluationForm())
                        .setParameter("name", punctuationRange.getName()).getSingleResult();
        return countResult != null && countResult > 0;
    }

    public Boolean isDuplicatedByInterpretation(FinalEvaluationPunctuationRange punctuationRange) {
        Long countResult = (punctuationRange.getId() == null) ?
                (Long) listEm.createNamedQuery("FinalEvaluationPunctuationRange.countInterpretation")
                        .setParameter("finalEvaluationForm", punctuationRange.getFinalEvaluationForm())
                        .setParameter("interpretation", punctuationRange.getInterpretation()).getSingleResult() :
                (Long) listEm.createNamedQuery("FinalEvaluationPunctuationRange.countByPunctuationRangeInterpretation")
                        .setParameter("punctuationRange", punctuationRange)
                        .setParameter("finalEvaluationForm", punctuationRange.getFinalEvaluationForm())
                        .setParameter("interpretation", punctuationRange.getInterpretation()).getSingleResult();
        return countResult != null && countResult > 0;
    }

    private Long countBetweenRangeValue(FinalEvaluationPunctuationRange punctuationRange, Integer rangeValue) {
        return (punctuationRange.getId() == null) ?
                (Long) listEm.createNamedQuery("FinalEvaluationPunctuationRange.countBetween")
                        .setParameter("finalEvaluationForm", punctuationRange.getFinalEvaluationForm())
                        .setParameter("rangeValue", rangeValue).getSingleResult() :
                (Long) listEm.createNamedQuery("FinalEvaluationPunctuationRange.countByPunctuationRangeBetween")
                        .setParameter("punctuationRange", punctuationRange)
                        .setParameter("finalEvaluationForm", punctuationRange.getFinalEvaluationForm())
                        .setParameter("rangeValue", rangeValue)
                        .getSingleResult();

    }
}
