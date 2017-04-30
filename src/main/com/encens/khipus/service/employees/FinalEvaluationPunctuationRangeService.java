package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.FinalEvaluationPunctuationRange;

import javax.ejb.Local;

/**
 * FinalEvaluationPunctuationRangeService
 *
 * @author
 * @version 2.8
 */
@Local
public interface FinalEvaluationPunctuationRangeService {
    Boolean isOverlapRange(FinalEvaluationPunctuationRange punctuationRange);

    Boolean isDuplicatedByName(FinalEvaluationPunctuationRange punctuationRange);

    Boolean isDuplicatedByInterpretation(FinalEvaluationPunctuationRange punctuationRange);
}
