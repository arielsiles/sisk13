package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.OrganizationalUnit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Data model to generate lis of employees to generate contracts
 *
 * @author
 * @version 3.4
 */
@Name("employeeTeacherContractGenerationDataModel")
@Scope(ScopeType.PAGE)
public class EmployeeTeacherContractGenerationDataModel extends QueryDataModel<Long, Employee> {
    private String idNumber;
    private BusinessUnit businessUnit;
    private OrganizationalUnit organizationalUnit;
    private JobCategory jobCategory;
    private Date initDate;
    private Date endDate;
    private Boolean jubilateFlag;

    private static final String[] RESTRICTIONS =
            {"businessUnit = #{employeeTeacherContractGenerationDataModel.businessUnit}",
                    "jobCategory = #{employeeTeacherContractGenerationDataModel.jobCategory}",
                    "organizationalUnit.id = #{employeeTeacherContractGenerationDataModel.organizationalUnit.id}",
                    "employee.idNumber = #{employeeTeacherContractGenerationDataModel.idNumber}",
                    "employee.jubilateFlag = #{employeeTeacherContractGenerationDataModel.jubilateFlag}"};


    @Create
    public void init() {
        sortProperty = "employee.lastName, employee.maidenName, employee.firstName";
    }

    @Override
    public String getEjbql() {
        String ejbql;

        //verify if this is laboral professor by career contracts
        if (organizationalUnit != null && PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(jobCategory.getPayrollGenerationType())) {
            ejbql = getByCareerEjbql();
        } else {
            ejbql = getGeneralEjbql();
        }

        ejbql = ejbql + getGeneralWhereConditions();
        return ejbql;
    }

    private String getGeneralEjbql() {
        return "SELECT DISTINCT employee FROM Employee employee " +
                " LEFT JOIN FETCH employee.extensionSite extensionSite" +
                " LEFT JOIN FETCH employee.salutation salutation" +
                " LEFT JOIN FETCH employee.country country" +
                " LEFT JOIN FETCH employee.maritalStatus maritalStatus" +
                " LEFT JOIN employee.contractList contract " +
                " LEFT JOIN contract.jobContractList jobContract " +
                " LEFT JOIN jobContract.job job " +
                " LEFT JOIN job.jobCategory jobCategory " +
                " LEFT JOIN job.organizationalUnit organizationalUnit " +
                " LEFT JOIN organizationalUnit.businessUnit businessUnit";
    }

    private String getByCareerEjbql() {
        return "SELECT DISTINCT employee FROM Employee employee " +
                " LEFT JOIN FETCH employee.extensionSite extensionSite" +
                " LEFT JOIN FETCH employee.salutation salutation" +
                " LEFT JOIN FETCH employee.country country" +
                " LEFT JOIN FETCH employee.maritalStatus maritalStatus" +
                " LEFT JOIN employee.contractList contract " +
                " LEFT JOIN contract.jobContractList jobContract " +
                " LEFT JOIN jobContract.job job " +
                " LEFT JOIN job.jobCategory jobCategory " +
                " LEFT JOIN jobContract.horaryBandContractList horaryBandContract " +
                " LEFT JOIN horaryBandContract.horary horary " +
                " LEFT JOIN horary.organizationalUnit organizationalUnit " +
                " LEFT JOIN organizationalUnit.businessUnit businessUnit";
    }

    private String getGeneralWhereConditions() {
        return " WHERE (( contract.initDate <= #{employeeTeacherContractGenerationDataModel.endDate} AND contract.endDate IS NULL ) " +
                " OR ( contract.initDate <= #{employeeTeacherContractGenerationDataModel.initDate} AND contract.endDate >= #{employeeTeacherContractGenerationDataModel.endDate} ) " +
                " OR ( contract.initDate >= #{employeeTeacherContractGenerationDataModel.initDate} AND contract.initDate <= #{employeeTeacherContractGenerationDataModel.endDate} ) " +
                " OR ( contract.endDate >= #{employeeTeacherContractGenerationDataModel.initDate} AND contract.endDate <= #{employeeTeacherContractGenerationDataModel.endDate} )) ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getJubilateFlag() {
        return jubilateFlag;
    }

    public void setJubilateFlag(Boolean jubilateFlag) {
        this.jubilateFlag = jubilateFlag;
    }
}
