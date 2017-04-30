package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.ExchangeRate;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Local
public interface PayrollGenerationCycleService extends GenericService {
    void createPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle, boolean includeActiveJobCategories) throws EntryDuplicatedException;

    Long countByName(String name);

    Long countByNameButThis(String name, Long id);

    Long countByBusinessUnitAndGestionAndMonth(PayrollGenerationCycle payrollGenerationCycle);

    Long countByBusinessUnitAndGestionAndMonthButThis(PayrollGenerationCycle payrollGenerationCycle);

    PayrollGenerationCycle read(PayrollGenerationCycle payrollGenerationCycle);

    void updatePayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) throws ConcurrencyException, EntryDuplicatedException;

    void applyTemplate(GestionPayroll gestionPayroll, ExchangeRate exchangeRate, PayrollGenerationCycle payrollGenerationCycle);

    PayrollGenerationCycle getLastPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle);

    Boolean isReadOnly(PayrollGenerationCycle payrollGenerationCycle);

    Boolean hasOfficialPayroll(PayrollGenerationCycle payrollGenerationCycle, BusinessUnit businessUnit, JobCategory jobCategory);

    Boolean hasAllPayrollsAsOfficial(PayrollGenerationCycle payrollGenerationCycle);

    Boolean hasTributaryPayroll(PayrollGenerationCycle payrollGenerationCycle);

    Boolean hasFiscalPayroll(PayrollGenerationCycle payrollGenerationCycle);

    Boolean hasAllPayrollsAsOfficialByGenerationType(PayrollGenerationCycle payrollGenerationCycle, List<PayrollGenerationType> payrollGenerationTypeList);

    Boolean hasPayrollGenerationInvestmentRegistration(PayrollGenerationCycle payrollGenerationCycle);
}
