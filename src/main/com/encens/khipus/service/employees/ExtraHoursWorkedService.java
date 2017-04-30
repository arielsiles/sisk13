package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ExtraHoursWorked;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;

import javax.ejb.Local;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
@Local
public interface ExtraHoursWorkedService extends GenericService {
    boolean exists(PayrollGenerationCycle payrollGenerationCycle, JobContract jobContract);

    @SuppressWarnings(value = "unchecked")
    Map<Long, ExtraHoursWorked> findByPayrollGenerationCycleAndJobCategory(PayrollGenerationCycle payrollGenerationCycle, JobCategory jobCategory);
}
