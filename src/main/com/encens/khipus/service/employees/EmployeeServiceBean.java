package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;

/**
 * Encens Team
 *
 * @author
 * @version : EmployeeServiceBean, 02-12-2009 03:33:41 PM
 */
@Stateless
@Name("employeeService")
@AutoCreate
public class EmployeeServiceBean implements EmployeeService {

    @In
    private BankAccountService bankAccountService;

    @In("#{entityManager}")
    private EntityManager em;

    @Logger
    private Log log;

    public List<Employee> getEmployeesByIdNumber(String idNumber) {
        if (idNumber != null && idNumber.trim().length() > 0) {
            try {
                return em.createNamedQuery("Employee.findEmployeesByIdNumber")
                        .setParameter("idNumber", idNumber)
                        .getResultList();
            } catch (NoResultException e) {
                return new ArrayList<Employee>();
            }
        }
        return new ArrayList<Employee>();
    }

    public Employee getEmployeeById(Long id) {
        try {
            return (Employee) em.createNamedQuery("Employee.findEmployeeById")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<Employee> getEmployeesByMarkCode(String markCode) {
        if (markCode != null && markCode.trim().length() > 0) {
            try {
                return em.createNamedQuery("Employee.findEmployeesByMarkCode")
                        .setParameter("markCode", markCode)
                        .getResultList();
            } catch (NoResultException e) {
                return new ArrayList<Employee>();
            }
        }
        return new ArrayList<Employee>();
    }

    public List<Employee> getEmployeesByIdRange(Long idInit, Long idEnd) {
        if (idInit != null && idEnd != null) {
            try {
                return em.createNamedQuery("Employee.findEmployeeByIdRange")
                        .setParameter("idInit", idInit)
                        .setParameter("idEnd", idEnd)
                        .getResultList();
            } catch (NoResultException e) {
                return new ArrayList<Employee>();
            }
        }
        return new ArrayList<Employee>();
    }

    public List<Employee> getAllEmployees() {
        try {
            return em.createNamedQuery("Employee.findAll").getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Employee>();
        }
    }

    public Map<Employee, List<Contract>> getEmployeeWithValidContractMap(Date initDate, Date endDate) {
        Map<Employee, List<Contract>> resultMap = new HashMap<Employee, List<Contract>>();
        try {
            List<Contract> contractList = em.createNamedQuery("Contract.findInDateRange")
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();

            for (Contract contract : contractList) {
                contract = em.find(Contract.class, contract.getId());
                if (resultMap.get(contract.getEmployee()) == null) {
                    List<Contract> tempList = new ArrayList<Contract>();
                    tempList.add(contract);
                    resultMap.put(contract.getEmployee(), tempList);
                } else {
                    resultMap.get(contract.getEmployee()).add(contract);
                }
            }
        } catch (Exception e) {
        }
        return resultMap;
    }

    public List<Map<Employee, Map<Contract, List<HoraryBandContract>>>> getEmployeeWithValidContractNHoraryBandContactMap(Date initDate, Date endDate, Integer segmentSize) {

        List<Map<Employee, Map<Contract, List<HoraryBandContract>>>> resultList = new ArrayList<Map<Employee, Map<Contract, List<HoraryBandContract>>>>();

        try {
            List<Object[]> queryResult = em.createNamedQuery("Contract.findInDateRangeWithHoraryBandContract")
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
            Map<Employee, Map<Contract, List<HoraryBandContract>>> resultMap = new HashMap<Employee, Map<Contract, List<HoraryBandContract>>>();
            int counter = 0;
            for (Object obj[] : queryResult) {
                if (counter == segmentSize) {
                    resultList.add(resultMap);
                    resultMap = new HashMap<Employee, Map<Contract, List<HoraryBandContract>>>();
                    counter = 0;
                }

                if (!resultMap.containsKey(((Contract) obj[0]).getEmployee())) {
                    resultMap.put(((Contract) obj[0]).getEmployee(), new HashMap<Contract, List<HoraryBandContract>>());
                    counter++;
                }

                if (!resultMap.get(((Contract) obj[0]).getEmployee()).containsKey(obj[0])) {
                    resultMap.get(((Contract) obj[0]).getEmployee()).put((Contract) obj[0], new ArrayList<HoraryBandContract>());
                }

                resultMap.get(((Contract) obj[0]).getEmployee()).get(obj[0]).add((HoraryBandContract) obj[1]);
            }

            if (counter > 0) {
                resultList.add(resultMap);
            }

        } catch (Exception e) {
        }
        return resultList;
    }

    public List<Employee> getEmployeesByValidContracts(Date initDate, Date endDate) {
        try {
            return em.createNamedQuery("Employee.findWithValidContracts")
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<Employee>(0);
    }

    public List<Employee> getEmployeesForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate, Integer firstResult, Integer maxResults) {
        try {

            return em.createNamedQuery("Employee.findEmployeesForPayrollGeneration")
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<Employee>(0);
    }

    public List<Employee> getEmployeesForPayrollGenerationByLastDayOfMonth(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate, Integer firstResult, Integer maxResults) {
        Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(endDate);
        Date lastDayOfMonth = DateUtils.getLastDayOfMonth(endDate);

        Calendar first = DateUtils.toCalendar(firstDayOfMonth);
        first.set(Calendar.HOUR, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);
        firstDayOfMonth = first.getTime();

        lastDayOfMonth.setDate(30);
        Calendar last = DateUtils.toCalendar(lastDayOfMonth);
        last.set(Calendar.HOUR, 0);
        last.set(Calendar.MINUTE, 0);
        last.set(Calendar.SECOND, 0);
        last.set(Calendar.MILLISECOND, 0);
        lastDayOfMonth = last.getTime();

        try {

            return em.createNamedQuery("Employee.findEmployeesForPayrollGenerationByLastDayOfMonth")
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .setParameter("lastDayOfMonth", lastDayOfMonth).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<Employee>(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<Employee> findEmployeesForMarkAndHoraryBandProcessByDateRange(Date initDate, Date endDate, Integer firstResult, Integer maxResults) {
        List<Employee> resultList = new ArrayList<Employee>();
        try {
            return em.createNamedQuery("Employee.findEmployeesForMarkAndHoraryBandProcessByDateRange")
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .getResultList();
        } catch (NoResultException e) {
            return resultList;
        }
    }

    public Long countEmployeesForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate) {
        try {
            return (Long) em.createNamedQuery("Employee.countEmployeesForPayrollGeneration")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getSingleResult();
        } catch (Exception e) {
        }
        return (long) 0;
    }

    public List<Employee> getEmployeesForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Date initDate, Date endDate) {
        try {
            List<Employee> resultList = em.createNamedQuery("Employee.findEmployeesForPayrollGeneration")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
            return resultList;
        } catch (Exception e) {
        }
        return new ArrayList<Employee>(0);
    }

    /**
     * Find employees by business unit and organizational unit
     *
     * @param businessUnit
     * @param organizationalUnit
     * @param contractInitDate
     * @param contractEndDate
     * @return List
     */
    public List<Employee> getEmployeesByBusinessUnitOrganizationalUnit(BusinessUnit businessUnit, OrganizationalUnit organizationalUnit, Date contractInitDate, Date contractEndDate) {
        log.debug("Excecuting getEmployeesByBusinessUnitOrganizationalUnit method.....");
        try {
            return em.createNamedQuery("Employee.findEmployeesByBusinessUnitOrganizationalUnitInRangeDate")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("organizationalUnit", organizationalUnit)
                    .setParameter("initDate", contractInitDate)
                    .setParameter("endDate", contractEndDate)
                    .getResultList();
        } catch (Exception e) {
            log.warn("Error in find employees", e);
        }
        return new ArrayList<Employee>();
    }

    public Employee getEmployeeByCode(String employeeCode) {
        Employee result = null;
        try {
            List<Employee> employeeList = em.createNamedQuery("Employee.findEmployeeByCode").setParameter("code", employeeCode).getResultList();
            if (!ValidatorUtil.isEmptyOrNull(employeeList)) {
                result = employeeList.get(0);
            }
        } catch (Exception e) {
        }

        return result;
    }

    public CostCenter getCostCenterByGeneratedPayrollAndEmployee(GeneratedPayroll generatedPayroll, Employee employee) {
        CostCenter costCenter = null;
        GenericPayroll genericPayroll = null;

        generatedPayroll = em.find(GeneratedPayroll.class, generatedPayroll.getId());

        try {
            if (GestionPayrollType.CHRISTMAS_BONUS.equals(generatedPayroll.getGestionPayroll().getGestionPayrollType())) {
                genericPayroll = (GenericPayroll) em.createNamedQuery("ChristmasPayroll.findByGeneratedPayrollAndEmployee")
                        .setParameter("generatedPayroll", generatedPayroll)
                        .setParameter("employee", employee).getSingleResult();
            } else if (PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
                genericPayroll = (GenericPayroll) em.createNamedQuery("FiscalProfessorPayroll.findByGeneratedPayrollAndEmployee")
                        .setParameter("generatedPayroll", generatedPayroll)
                        .setParameter("employee", employee).getSingleResult();
            } else if (PayrollGenerationType.GENERATION_BY_SALARY.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
                genericPayroll = (GenericPayroll) em.createNamedQuery("ManagersPayroll.findByGeneratedPayrollAndEmployee")
                        .setParameter("generatedPayroll", generatedPayroll)
                        .setParameter("employee", employee).getSingleResult();
            } else if (PayrollGenerationType.GENERATION_BY_TIME.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
                genericPayroll = (GenericPayroll) em.createNamedQuery("GeneralPayroll.findByGeneratedPayrollAndEmployee")
                        .setParameter("generatedPayroll", generatedPayroll)
                        .setParameter("employee", employee).getSingleResult();
            }
        } catch (NoResultException e) {
        }

        if (genericPayroll != null && genericPayroll.getCostCenter() != null) {
            costCenter = genericPayroll.getCostCenter();
        }

        if (costCenter == null) {
            try {
                String costCenterCode = (String) em.createNamedQuery("CostCenter.findMaxByGeneratedPayollAndEmployee")
                        .setParameter("generatedPayroll", generatedPayroll)
                        .setParameter("employee", employee)
                        .getSingleResult();
                if (!ValidatorUtil.isBlankOrNull(costCenterCode)) {
                    costCenter = em.find(CostCenter.class, new CostCenterPk(Constants.defaultCompanyNumber, costCenterCode));
                }
            } catch (Exception e) {
            }

            if (costCenter == null) {
                Job job = getJobByGestionPayrollAndEmployee(generatedPayroll.getGestionPayroll(), employee);
                costCenter = job != null ? job.getOrganizationalUnit().getCostCenter() : null;
            }
        }
        return costCenter;
    }

    public Job getJobByGeneratedPayrollAndEmployee(GeneratedPayroll generatedPayroll, Employee employee) {
        Job job = null;
        try {
            job = (Job) em.createNamedQuery("Job.findByGeneratedPayollAndEmployee")
                    .setParameter("generatedPayroll", generatedPayroll)
                    .setParameter("employee", employee)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
        }
        return job != null ? job : getJobByGestionPayrollAndEmployee(generatedPayroll.getGestionPayroll(), employee);
    }

    public Job getJobByGestionPayrollAndEmployee(GestionPayroll gestionPayroll, Employee employee) {

        Date initDate = gestionPayroll.getInitDate();
        Date endDate = gestionPayroll.getInitDate();
        if (gestionPayroll.isChristmasBonusType()) {
            initDate = DateUtils.getFirstDayOfMonth(Month.DECEMBER.getValueAsPosition(), gestionPayroll.getGestion().getYear());
            endDate = DateUtils.getLastDayOfMonth(initDate);
        }

        Long jobId = (Long) em.createNamedQuery("Job.findByGestionPayollAndEmployee")
                .setParameter("employee", employee)
                .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                .setParameter("businessUnit", gestionPayroll.getBusinessUnit())
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .setParameter("jobCategory", gestionPayroll.getJobCategory())
                .getSingleResult();
        return jobId != null && jobId != 0 ? em.find(Job.class, jobId) : null;
    }

    public PaymentType getEmployeesPaymentType(Employee employee) {
        if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(employee.getPaymentType()) && bankAccountService.hasDefaultAccount(employee)) {
            return PaymentType.PAYMENT_BANK_ACCOUNT;
        }
        return PaymentType.PAYMENT_WITH_CHECK;
    }

    public Currency getEmployeesCurrencyByPaymentType(GeneratedPayroll generatedPayroll, Employee employee, PaymentType paymentType) {
        Currency currency = null;
        if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
            Job job = getJobByGeneratedPayrollAndEmployee(generatedPayroll, employee);
            if (job != null) {
                currency = job.getSalary().getCurrency();
            }
        } else if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
            currency = bankAccountService.getDefaultAccount(employee).getCurrency();
        }
        return currency;
    }


    public Currency getEmployeesCurrency(GeneratedPayroll generatedPayroll, Employee employee) {
        return getEmployeesCurrencyByPaymentType(generatedPayroll, employee, getEmployeesPaymentType(employee));
    }
}

