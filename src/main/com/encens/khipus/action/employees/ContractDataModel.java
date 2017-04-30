package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Contract data model
 *
 * @author Ariel Siles Encinas
 * @version 3.2.9
 */
@Name("contractDataModel")
@Scope(ScopeType.PAGE)
public class ContractDataModel extends QueryDataModel<Long, Contract> {

    private static final String[] RESTRICTIONS =
            {
                    "contract.numberOfContract=#{contractDataModel.criteria.numberOfContract}",
                    "contract.cycle.gestion=#{contractDataModel.gestion}",
                    "contract.initDate >= #{contractDataModel.criteria.initDate}",
                    "contract.endDate <= #{contractDataModel.criteria.endDate}",
                    "contract.id in (" +
                            "select distinct contract.id from Contract contract " +
                            "left join contract.jobContractList jobContract " +
                            "left join jobContract.job job " +
                            "left join job.organizationalUnit organizationalUnit " +
                            "where organizationalUnit.businessUnit =#{contractDataModel.businessUnit})",
                    "contract.id in (" +
                            "select distinct contract.id from Contract contract " +
                            "left join contract.jobContractList jobContract " +
                            "left join jobContract.job job " +
                            "where job.jobCategory =#{contractDataModel.jobCategory})",
                    "employee = #{contractDataModel.employee}"
            };

    private Employee employee;
    private JobCategory jobCategory;
    private BusinessUnit businessUnit;
    private Gestion gestion;
    private Sector sector;
    private Boolean enableBusinessUnitFilter = true;

    @Override
    public String getEjbql() {
        return "select contract from Contract contract " +
                " left join fetch contract.employee employee";
    }


    @Create
    public void defaultSort() {
        sortProperty = "contract.numberOfContract";
    }


    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        if (enableBusinessUnitFilter) {
            setBusinessUnit(null);
        }

        setSector(null);
        clearEmployee();
        clearJobCategory();
        clearGestion();
        super.clear();
    }

    private void clearGestion() {
        setGestion(null);
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    private void clearJobCategory() {
        setJobCategory(null);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Boolean getEnableBusinessUnitFilter() {
        return enableBusinessUnitFilter;
    }

    public void setEnableBusinessUnitFilter(Boolean enableBusinessUnitFilter) {
        this.enableBusinessUnitFilter = enableBusinessUnitFilter;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }
}