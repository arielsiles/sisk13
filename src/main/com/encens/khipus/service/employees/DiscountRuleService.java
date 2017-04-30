package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.DiscountRule;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Local
public interface DiscountRuleService extends GenericService {
    void createDiscountRule(DiscountRule discountRule) throws EntryDuplicatedException;

    void updateDiscountRule(DiscountRule discountRule) throws ConcurrencyException, EntryDuplicatedException;

    void deleteDiscountRule(DiscountRule discountRule) throws ConcurrencyException, ReferentialIntegrityException;

    @SuppressWarnings("unchecked")
    List<DiscountRule> findActiveByGestionAndBusinessUnitAndJobCategory(Gestion gestion, BusinessUnit businessUnit,
                                                                        JobCategory jobCategory);

    @SuppressWarnings("unchecked")
    List<DiscountRule> findBusinessUnitGlobalActiveDiscountRuleByGestion(Gestion gestion, BusinessUnit businessUnit);

    @SuppressWarnings("unchecked")
    List<DiscountRule> findGlobalActiveDiscountRuleByGestion(Gestion gestion);
}
