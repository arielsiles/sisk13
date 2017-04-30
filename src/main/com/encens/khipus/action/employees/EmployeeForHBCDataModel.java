package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version 1.2.3
 */
@Name("employeeForHBCDataModel")
@Scope(ScopeType.PAGE)
public class EmployeeForHBCDataModel extends QueryDataModel<Long, Employee> {
    private static final String[] RESTRICTIONS =
            {"organizationalUnit = #{horaryBandContract.jobContract.job.organizationalUnit}",
                    "lower(employee.lastName) like concat(lower(#{employeeForHBCDataModel.criteria.lastName}),'%')",
                    "lower(employee.maidenName) like concat(lower(#{employeeForHBCDataModel.criteria.maidenName}),'%')",
                    "lower(employee.firstName) like concat(lower(#{employeeForHBCDataModel.criteria.firstName}),'%')",
                    "employee.idNumber=#{employeeForHBCDataModel.criteria.idNumber}"};

    @Create
    public void init() {
        sortProperty = "employee.lastName";
    }

    @Override
    public String getEjbql() {
        return "select distinct employee from Employee employee" +
                " join employee.contractList contract" +
                " join contract.jobContractList jobContract" +
                " left join jobContract.job job" +
                " left join job.organizationalUnit organizationalUnit";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
