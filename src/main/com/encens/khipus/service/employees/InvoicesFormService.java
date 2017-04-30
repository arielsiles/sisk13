package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.InvoicesForm;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
@Local
public interface InvoicesFormService extends GenericService {
    boolean exists(PayrollGenerationCycle payrollGenerationCycle, List<JobContract> jobContracts);

    Map<Long, InvoicesForm> findInvoicesFormMapByPayrollGenerationCycleAndEmployeeList(PayrollGenerationCycle payrollGenerationCycle, List<Long> employeeIdList);
}
