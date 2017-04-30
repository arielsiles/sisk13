package com.encens.khipus.service.employees;

import com.encens.khipus.exception.employees.TaxPayrollException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ConfigurationTaxPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.TaxPayrollGenerated;
import com.encens.khipus.model.employees.TributaryPayroll;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
@Local
public interface TributaryPayrollGeneratorService extends GenericService {

    TaxPayrollGenerated getOfficialTributaryPayroll(ConfigurationTaxPayroll configuration);

    void validateGestionPayrollEmployees(ConfigurationTaxPayroll configuration) throws TaxPayrollException;

    @SuppressWarnings(value = "unchecked")
    Map<Long, TributaryPayroll> getTributaryPayrollsForLastMonth(PayrollGenerationCycle payrollGenerationCycle, List<Long> employeeIdList);
}
