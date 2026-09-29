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

    /**
     * Cuantas planillas OFICIALES se generaron con esta regla. El ciclo de generacion congela
     * una FK a la regla, no una copia de sus rangos, asi que editarla despues cambia lo que
     * daria una regeneracion de un mes ya cerrado.
     *
     * @return numero de planillas oficiales; 0 si la regla todavia se puede editar
     */
    Long countOfficialPayrolls(DiscountRule discountRule);

    boolean isLockedByOfficialPayroll(DiscountRule discountRule);
}
