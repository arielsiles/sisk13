package com.encens.khipus.model.employees;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.util.DateUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version $Id: Employee.java 2008-8-28 11:54:02 $
 */
@NamedQueries(
        {
                @NamedQuery(name = "Employee.findAll", query = "select o from Employee o order by o.id"),
                @NamedQuery(name = "Employee.findEmployeeByIdRange", query = "select o from Employee o WHERE o.id>=:idInit and o.id<=:idEnd order by o.id"),
                @NamedQuery(name = "Employee.findEmployeeById", query = "select o from Employee o WHERE o.id=:id order by o.id"),
                @NamedQuery(name = "Employee.findEmployeeByCode", query = "select o from Employee o WHERE o.employeeCode=:code order by o.id"),
                @NamedQuery(name = "Employee.findEmployeesByIdNumber", query = "select o from Employee o WHERE o.idNumber =:idNumber order by o.id"),
                @NamedQuery(name = "Employee.findEmployeesByMarkCode", query = "select o from Employee o WHERE o.markCode =:markCode order by o.markCode"),
                @NamedQuery(name = "Employee.findWithValidContracts", query = "select distinct contract.employee from Contract contract" +
                        "  where contract.activeForPayrollGeneration=:activeForPayrollGeneration and  " +
                        " ((contract.initDate <=:endDate and contract.endDate is null ) or (contract.initDate<=:endDate and contract.endDate>=:initDate))"),
                @NamedQuery(name = "Employee.findEmployeesForPayrollGeneration", query = "select distinct jobContract.contract.employee" +
                        " from JobContract jobContract" +
                        "  where jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration and " +
                        " ((jobContract.contract.initDate <=:endDate and jobContract.contract.endDate is null ) " +
                        " or (jobContract.contract.initDate<=:initDate and jobContract.contract.endDate>=:endDate)" +
                        " or (jobContract.contract.initDate>=:initDate and jobContract.contract.initDate<=:endDate)" +
                        " or (jobContract.contract.endDate>=:initDate and jobContract.contract.endDate<=:endDate)) and" +
                        " jobContract.job.organizationalUnit.businessUnit=:businessUnit and  jobContract.job.jobCategory=:jobCategory"),
                @NamedQuery(name = "Employee.findEmployeesForPayrollGenerationByLastDayOfMonth", query = "select distinct jobContract.contract.employee" +
                        " from JobContract jobContract" +
                        "  where jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration and " +
                        " ((jobContract.contract.initDate <=:endDate and jobContract.contract.endDate is null ) " +
                        " or (jobContract.contract.initDate<=:initDate and jobContract.contract.endDate>=:endDate)" +
                        " or (jobContract.contract.initDate>=:initDate and jobContract.contract.initDate<=:endDate)" +
                        " or (jobContract.contract.initDate<=:lastDayOfMonth and jobContract.contract.initDate>=:endDate)" +
                        " or (jobContract.contract.endDate>=:initDate and jobContract.contract.endDate<=:endDate)) and" +
                        " jobContract.job.organizationalUnit.businessUnit=:businessUnit and  jobContract.job.jobCategory=:jobCategory"),

                @NamedQuery(name = "Employee.findEmployeesForMarkAndHoraryBandProcessByDateRange",
                        query = "select distinct jobContract.contract.employee" +
                                " from JobContract jobContract" +
                                "  where jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration and " +
                                " ((jobContract.contract.initDate <=:endDate and jobContract.contract.endDate is null ) " +
                                " or (jobContract.contract.initDate<=:initDate and jobContract.contract.endDate>=:endDate)" +
                                " or (jobContract.contract.initDate>=:initDate and jobContract.contract.initDate<=:endDate)" +
                                " or (jobContract.contract.endDate>=:initDate and jobContract.contract.endDate<=:endDate)) "),

                @NamedQuery(name = "Employee.countEmployeesForPayrollGeneration", query = "select count (distinct jobContract.contract.employee)" +
                        " from JobContract jobContract" +
                        "  where jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration and " +
                        " ((jobContract.contract.initDate <=:endDate and jobContract.contract.endDate is null ) or (jobContract.contract.initDate<=:endDate and jobContract.contract.endDate>=:initDate)) and" +
                        " jobContract.job.organizationalUnit.businessUnit=:businessUnit and  jobContract.job.jobCategory=:jobCategory"),
                @NamedQuery(name = "Employee.findEmployeesByBusinessUnitOrganizationalUnitInRangeDate", query = "SELECT DISTINCT employee FROM Employee employee" +
                        " LEFT JOIN employee.contractList contract" +
                        " LEFT JOIN contract.jobContractList jobContract" +
                        " LEFT JOIN jobContract.job job" +
                        " LEFT JOIN job.organizationalUnit organizationalUnit" +
                        " LEFT JOIN organizationalUnit.businessUnit businessUnit" +
                        " WHERE businessUnit =:businessUnit AND organizationalUnit =:organizationalUnit AND contract.initDate >=:initDate AND contract.endDate <=:endDate")
        }
)

@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "empleado")
@DiscriminatorValue("empleado")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idempleado", referencedColumnName = "idpersona")/*,
    @PrimaryKeyJoinColumn(name = "NO_IDENTIFICACION", referencedColumnName = "NO_IDENTIFICACION")*/
})
@EntityListeners(UpperCaseStringListener.class)
public class Employee extends Person {

    @Column(name = "fechaingreso")
    @Temporal(TemporalType.DATE)
    private Date hireDate;

    @Column(name = "fechasalida")
    @Temporal(TemporalType.DATE)
    private Date retireDate;

    @Column(name = "salario", precision = 13, scale = 2)
    private BigDecimal salary;

    @Column(name = "flagcontrol", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean controlFlag = Boolean.TRUE;

    @Column(name = "flagafp", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean afpFlag = Boolean.FALSE;

    @Column(name = "flagret", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean retentionFlag = Boolean.FALSE;

    @Column(name = "FLAGJUBILADO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean jubilateFlag = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", referencedColumnName = "idunidadnegocio", nullable = true)
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Contract> contractList = new ArrayList<Contract>(0);

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<BankAccount> bankAccountList = new ArrayList<BankAccount>(0);

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<SalaryMovement> salaryMovementList = new ArrayList<SalaryMovement>(0);

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<SpecialDate> specialDateList = new ArrayList<SpecialDate>(0);

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<EmployeeAcademicFormation> academicFormationList = new ArrayList<EmployeeAcademicFormation>(0);

    @Column(name = "tipodepago", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "codigoempleado")
    private String employeeCode;

    @Column(name = "codigomarcacion", length = 100, nullable = false)
    private String markCode;

    public Boolean getAfpFlag() {
        return afpFlag;
    }

    public void setAfpFlag(Boolean afpFlag) {
        this.afpFlag = afpFlag;
    }

    public Boolean getRetentionFlag() {
        return retentionFlag;
    }

    public void setRetentionFlag(Boolean retentionFlag) {
        this.retentionFlag = retentionFlag;
    }

    public Boolean getJubilateFlag() {
        return jubilateFlag;
    }

    public void setJubilateFlag(Boolean jubilateFlag) {
        this.jubilateFlag = jubilateFlag;
    }

    public List<BankAccount> getBankAccountList() {
        return bankAccountList;
    }

    public void setBankAccountList(List<BankAccount> bankAccountList) {
        this.bankAccountList = bankAccountList;
    }

    public Company getCompany() {
        return company;
    }

    public Boolean getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(Boolean controlFlag) {
        this.controlFlag = controlFlag;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Date getRetireDate() {
        return retireDate;
    }

    public void setRetireDate(Date retireDate) {
        this.retireDate = retireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public List<Contract> getContractList() {
        return contractList;
    }

    public void setContractList(List<Contract> contractList) {
        this.contractList = contractList;
    }

    public List<SalaryMovement> getSalaryMovementList() {
        return salaryMovementList;
    }

    public void setSalaryMovementList(List<SalaryMovement> salaryMovementList) {
        this.salaryMovementList = salaryMovementList;
    }

    public List<SpecialDate> getSpecialDateList() {
        return specialDateList;
    }

    public void setSpecialDateList(List<SpecialDate> specialDateList) {
        this.specialDateList = specialDateList;
    }

    public List<EmployeeAcademicFormation> getAcademicFormationList() {
        return academicFormationList;
    }

    public void setAcademicFormationList(List<EmployeeAcademicFormation> academicFormationList) {
        this.academicFormationList = academicFormationList;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getMarkCode() {
        return markCode;
    }

    public void setMarkCode(String markCode) {
        this.markCode = markCode;
    }

    public Integer computeAgeInDaysAtDate(Date date) {
        return null == getBirthDay() ? 0 : ((Long) DateUtils.daysBetween(getBirthDay(), date)).intValue();
    }

    @Override
    public String toString() {
        return "Employee{" +
                "getId()='" + getId() + '\'' +
                ", getIdNumber='" + getIdNumber() + '\'' +
                ", getLastName='" + getLastName() + '\'' +
                ", getMaidenName='" + getMaidenName() + '\'' +
                ", getFirstName='" + getFirstName() + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                '}';
    }
}
