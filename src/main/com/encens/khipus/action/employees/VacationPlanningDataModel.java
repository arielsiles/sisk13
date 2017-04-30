package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.VacationPlanning;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("vacationPlanningDataModel")
@Scope(ScopeType.PAGE)
public class VacationPlanningDataModel extends QueryDataModel<Long, VacationPlanning> {
    private Employee employee;
    private BusinessUnit businessUnit;

    private static final String[] RESTRICTIONS = {
            "vacationPlanning.code = #{vacationPlanningDataModel.criteria.code}",
            "businessUnit = #{vacationPlanningDataModel.businessUnit}",
            "employee = #{vacationPlanningDataModel.employee}"
    };

    @Create
    public void init() {
        sortProperty = "vacationPlanning.code";
    }

    @Override
    public String getEjbql() {
        return "select vacationPlanning from VacationPlanning vacationPlanning" +
                " left join fetch vacationPlanning.jobContract jobContract " +
                " left join fetch jobContract.job job" +
                " left join fetch job.organizationalUnit organizationalUnit" +
                " left join fetch organizationalUnit.businessUnit businessUnit" +
                " left join fetch jobContract.contract contract" +
                " left join fetch contract.employee employee";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }
}
