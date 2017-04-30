package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.*;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Encens Team
 *
 * @author
 * @version : EmployeeService, 02-12-2009 03:33:54 PM
 */
@Local
public interface EmployeeService {
    List<Employee> getEmployeesByIdNumber(String idNumber);

    List<Employee> getEmployeesByIdRange(Long idInit, Long idEnd);

    List<Employee> getAllEmployees();

    Map<Employee, List<Contract>> getEmployeeWithValidContractMap(Date initDate, Date endDate);

    List<Map<Employee, Map<Contract, List<HoraryBandContract>>>> getEmployeeWithValidContractNHoraryBandContactMap(Date initDate, Date endDate, Integer segmentSize);

    List<Employee> getEmployeesByValidContracts(Date initDate, Date endDate);

    List<Employee> getEmployeesForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate, Integer firstResult, Integer maxResults);

    Long countEmployeesForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate);

    List<Employee> getEmployeesForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate);

    Employee getEmployeeByCode(String employeeCode);

    List<Employee> getEmployeesForPayrollGenerationByLastDayOfMonth(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate, Integer firstResult, Integer maxResults);

    List<Employee> getEmployeesByBusinessUnitOrganizationalUnit(BusinessUnit businessUnit, OrganizationalUnit organizationalUnit, Date contractInitDate, Date contractEndDate);

    CostCenter getCostCenterByGeneratedPayrollAndEmployee(GeneratedPayroll generatedPayroll, Employee employee);

    Job getJobByGeneratedPayrollAndEmployee(GeneratedPayroll generatedPayroll, Employee employee);

    PaymentType getEmployeesPaymentType(Employee employee);

    com.encens.khipus.model.employees.Currency getEmployeesCurrencyByPaymentType(GeneratedPayroll generatedPayroll, Employee employee, PaymentType paymentType);

    com.encens.khipus.model.employees.Currency getEmployeesCurrency(GeneratedPayroll generatedPayroll, Employee employee);

    Job getJobByGestionPayrollAndEmployee(GestionPayroll gestionPayroll, Employee employee);

    @SuppressWarnings({"unchecked"})
    List<Employee> getEmployeesByMarkCode(String markCode);

    @SuppressWarnings({"unchecked"})
    List<Employee> findEmployeesForMarkAndHoraryBandProcessByDateRange(Date initDate, Date endDate, Integer firstResult, Integer maxResults);

    Employee getEmployeeById(Long id);
}
