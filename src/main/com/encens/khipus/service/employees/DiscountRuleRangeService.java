package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.DiscountRuleRangeOverlapException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.DiscountRule;
import com.encens.khipus.model.employees.DiscountRuleRange;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
@Local
public interface DiscountRuleRangeService extends GenericService {

    void createDiscountRuleRange(DiscountRuleRange discountRuleRange) throws EntryDuplicatedException, DiscountRuleRangeOverlapException;

    void updateDiscountRuleRange(DiscountRuleRange discountRuleRange) throws ConcurrencyException, EntryDuplicatedException, DiscountRuleRangeOverlapException;

    void deleteDiscountRuleRange(DiscountRuleRange discountRuleRange) throws ConcurrencyException, ReferentialIntegrityException;

    DiscountRuleRange findLastDiscountRuleRange(DiscountRule discountRule);

    List<DiscountRuleRange> findByDiscountRule(DiscountRule discountRule);

    DiscountRuleRange findDiscountRuleRangeInList(Integer minutes, List<DiscountRule> discountRuleList,
                                                  Map<Integer, DiscountRuleRange> minuteDiscountRuleRangeMap);

}
