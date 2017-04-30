package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.AcademicFormationType;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Employee data model
 *
 * @author
 * @version 1.0
 */
@Name("employeeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EMPLOYEE','VIEW')}")
public class EmployeeDataModel extends QueryDataModel<Long, Employee> {
    @Logger
    private Log log;
    private CostCenter costCenter = null;
    private String academicFormationName;
    private String academicFormationUniversity;
    private AcademicFormationType academicFormationType;

    private static final String[] RESTRICTIONS = {
            "lower(employee.firstName) like concat('%', concat(lower(#{employeeDataModel.criteria.firstName}), '%'))",
            "lower(employee.lastName) like concat('%', concat(lower(#{employeeDataModel.criteria.lastName}), '%'))",
            "lower(employee.maidenName) like concat('%', concat(lower(#{employeeDataModel.criteria.maidenName}), '%'))",
            "lower(employee.idNumber) like concat(lower(#{employeeDataModel.criteria.idNumber}), '%')",
            "lower(employee.employeeCode) like concat(lower(#{employeeDataModel.criteria.employeeCode}), '%')",
            "employee in (select distinct e from JobContract jobContract " +
                    "left join jobContract.job job " +
                    "left join job.organizationalUnit organizationalUnit " +
                    "left join organizationalUnit.costCenter cc " +
                    "left join jobContract.contract contract " +
                    "left join contract.employee e where cc=#{employeeDataModel.costCenter})",
            "lower(academicFormation.name) like concat('%', concat(lower( #{employeeDataModel.academicFormationName}), '%'))",
            "lower(academicFormation.university) like concat('%', concat(lower( #{employeeDataModel.academicFormationUniversity}), '%'))",
            "academicFormation.academicFormationType = #{employeeDataModel.academicFormationType}",
    };

    @Override
    public String getEjbql() {
        return "select distinct employee from Employee employee " +
                " left join employee.academicFormationList academicFormation";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getAcademicFormationName() {
        return academicFormationName;
    }

    public void setAcademicFormationName(String academicFormationName) {
        this.academicFormationName = academicFormationName;
    }

    public String getAcademicFormationUniversity() {
        return academicFormationUniversity;
    }

    public void setAcademicFormationUniversity(String academicFormationUniversity) {
        this.academicFormationUniversity = academicFormationUniversity;
    }

    public AcademicFormationType getAcademicFormationType() {
        return academicFormationType;
    }

    public void setAcademicFormationType(AcademicFormationType academicFormationType) {
        this.academicFormationType = academicFormationType;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    @Override
    public void clear() {
        setCostCenter(null);
        setAcademicFormationName(null);
        setAcademicFormationUniversity(null);
        setAcademicFormationType(null);
        super.clear();
    }
}