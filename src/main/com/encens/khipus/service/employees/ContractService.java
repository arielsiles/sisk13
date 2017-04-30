package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.OrganizationalUnit;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * ContractService business interface
 *
 * @author
 */
@Local
public interface ContractService {
    List<Contract> getContractsByEmployeeInDateRange(Employee employee, Date initDate, Date endDate);

    List<Contract> getContractsForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Employee employee, Date initDate, Date endDate);

    List<Contract> getContractsForPayrollGenerationByLastDayOfMonth(BusinessUnit businessUnit, JobCategory jobCategory, Employee employee, Date initDate, Date endDate);

    List<Contract> getContractsByEmployeeInGestion(Employee employee, Date initDate, Date endDate);

    OrganizationalUnit getMaxOrganizationalUnitProfessorAssigned(Employee employee, Date initDate, Date endDate);
}
