package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.employees.BankAccountService;
import com.encens.khipus.service.employees.JobContractService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Employee action class
 *
 * @author
 * @version 1.0
 */
@Name("employeeAction")
@Scope(ScopeType.CONVERSATION)
public class EmployeeAction extends GenericAction<Employee> {

    @In
    private ExtensionService extensionService;
    @In
    private JobContractService jobContractService;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    @In
    private BankAccountService bankAccountService;

    @Factory(value = "employee", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('EMPLOYEE','VIEW')}")
    public Employee initUser() {
        return getInstance();
    }

    @In("#{entityManager}")
    private EntityManager em;

    @Override
    public String getDisplayNameProperty() {
        return "fullName";
    }

    @Factory("paymentType")
    public PaymentType[] getPaymentTypes() {
        return PaymentType.values();
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('EMPLOYEE','VIEW')}")
    public String select(Employee instance) {
        String outCome = super.select(instance);
        updateShowExtension();
        return outCome;
    }

    public String getJobCategory(Employee employee) {
        String result = "";
        if (employee != null) {
            try {
                JobContract jobContract = jobContractService.lastJobContractByEmployee(employee);
                if (jobContract != null) {
                    result = jobContract.getJob().getJobCategory().getAcronym();
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    public String selectAllContracts(Employee employee) {
//        try {
//            setOp(OP_UPDATE);
//            setInstance(genericService.findById(getEntityClass(), getId(employee)));
//
//            System.out.println("these are the all contract of " + getInstance().getFullName());
//            System.out.println("getInstance().getId()=" + getInstance().getId());
//            System.out.println("getInstance().getIdNumber()=" + getInstance().getIdNumber());
//            System.out.println("getInstance().getContractList()=" + getInstance().getContractList());
//            System.out.println("getInstance().getContractList().size()=" + getInstance().getContractList().size());

        System.out.println("*****************************************************************");
        List<Contract> employeeListByContract = em.createQuery("select c from Contract c").getResultList();
        System.out.println("*****************************************************************");

        System.out.println("contractListByEmployee.size()=" + employeeListByContract.size());
        for (Contract contractTemp : employeeListByContract) {
            System.out.println("contractTemp.getEmployee()=" + contractTemp.getEmployee());
            System.out.println("contractTemp.getSpecialDates()=" + contractTemp.getSpecialDates());
            System.out.println("contractTemp.getJobContractList()=" + contractTemp.getJobContractList());
            System.out.println("contractTemp.getEmployee().getContractList()=" + contractTemp.getEmployee().getContractList());
            System.out.println("");
        }

//        System.out.println("*****************************************************************");
//        List<Employee> contractListByEmployee = em.createQuery("select e from Employee e").getResultList();
//
//        System.out.println("*****************************************************************");
//        System.out.println("contractListByEmployee.size()=" + contractListByEmployee.size());
//        for (Employee employeeTemp : contractListByEmployee) {
//            System.out.println("employeeTemp.getContractList()=" + employeeTemp.getContractList());
//            System.out.println("employeeTemp.getDiscountRules()=" + employeeTemp.getDiscountRules());
//            System.out.println("");
//        }
//
//        System.out.println("*****************************************************************");


        return Outcome.FAIL;
//
//        } catch (EntryNotFoundException e) {
//            addNotFoundMessage();
//            return Outcome.FAIL;
//        }
    }

    public String selectContract(Employee employee) {
        try {
            setOp(OP_UPDATE);
            setInstance(genericService.findById(getEntityClass(), getId(employee)));
            return Outcome.SUCCESS;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    /**
     * Get the salary of the employee's first contract
     *
     * @param contracts List of contracts
     * @return Salary
     */
    public String getSalary(List<Contract> contracts) {

        if (contracts.size() != 0) {
            return contracts.get(0).getJobContractList().get(0).getJob().getSalary().getAmount().toString();
        } else {
            return "-";
        }

    }

    public Boolean hasBankAccount(Employee employee) {
        if (employee != null && employee.getId() != null && PaymentType.PAYMENT_BANK_ACCOUNT.equals(employee.getPaymentType())) {
            return bankAccountService.hasDefaultAccount(employee);
        }
        return true;
    }

    public Boolean isBankAccountPaymentType(Employee employee) {
        if (employee != null) {
            return PaymentType.PAYMENT_BANK_ACCOUNT.equals(employee.getPaymentType());
        }
        return false;
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().setExtensionSite(null);
        }
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
    }
}