package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.*;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SalaryMovementService
 *
 * @author
 */
@Local
public interface SalaryMovementService extends GenericService {
    List<SalaryMovement> findByEmployeeAndInitDateEndDate(Employee employee, Date initDate, Date endDate);

    List<SalaryMovement> findByEmployeeAndGestionPayroll(Employee employee, GestionPayroll gestionPayroll);

    SalaryMovement load(SalaryMovement salaryMovement) throws EntryNotFoundException;

    void matchGeneratedSalaryMovement(GeneratedPayroll generatedPayroll, List<? extends GenericPayroll> genericPayrollList) throws ConcurrencyException, EntryDuplicatedException;

    @SuppressWarnings({"unchecked"})
    Map<Long, List<SalaryMovement>> findByPayrollGenerationIdList(Class<? extends GenericPayroll> genericPayrollClass, List<Long> payrollGenerationIdList, GestionPayroll gestionPayroll);
}
