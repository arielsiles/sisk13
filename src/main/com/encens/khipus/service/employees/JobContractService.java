package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.OrganizationalUnit;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : JobContractService, 03-12-2009 04:24:37 PM
 */
@Local
public interface JobContractService extends GenericService {

    List<JobContract> getJobContractList(Employee emloyee);

    JobContract getJobContractByEmployeeAndOrgUnitAndDatesOfContract(Employee employee, String planestudio, Date initDate, Date endDate);

    List<JobContract> getJobContractByEmployeeAndOrgUnit(Employee emloyee, OrganizationalUnit organizationalUnit);

    JobContract lastJobContractByEmployee(Employee employee);

    JobContract find(JobContract jobContract, EntityManager entityManager);

    JobContract load(JobContract jobContract);

    JobContract load(JobContract jobContract, EntityManager entityManager);

    List<JobContract> load(List<Long> idList);

    List<JobContract> load(List<Long> idList, EntityManager entityManager);

    public JobContract getJobContract(Employee emloyee);

    @SuppressWarnings({"unchecked"})
    List<JobContract> findActiveByEmployeeAndDateRange(Employee emloyee, Date initDate, Date endDate, PayrollGenerationType payrollGenerationType);

    Long countActiveByEmployeeAndDateRangeAndGenerationType(Employee emloyee, Date initDate, Date endDate, PayrollGenerationType payrollGenerationType);
}
