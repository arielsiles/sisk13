package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.employees.PayrollGenerationType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Name("gestionPayrollOfficialSearchDataModel")
@Scope(ScopeType.PAGE)
public class GestionPayrollOfficialSearchDataModel extends QueryDataModel<Long, GestionPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(gestionPayroll.gestionName) like concat('%', concat(lower(#{gestionPayrollOfficialSearchDataModel.criteria.gestionName}), '%'))",
            "gestionPayroll.gestion.year = #{gestionPayrollOfficialSearchDataModel.year}",
            "gestionPayroll.month = #{gestionPayrollOfficialSearchDataModel.month}",
            "gestionPayroll.jobCategory.payrollGenerationType = #{gestionPayrollOfficialSearchDataModel.generationBySalaryConstant}",
            "gestionPayroll.businessUnit = #{gestionPayrollOfficialSearchDataModel.businessUnitStaticFilter}",
            "element.generatedPayrollType = #{gestionPayrollOfficialSearchDataModel.officialTypeConstant}"
    };

    private Integer year;

    private Month month;

    private BusinessUnit businessUnitStaticFilter;

    @Create
    public void init() {
        sortAsc = true;
        sortProperty = "gestionPayroll.businessUnit.position,gestionPayroll.jobCategory.position,gestionPayroll.gestionName";
    }

    @Override
    public String getEjbql() {
        return "select gestionPayroll from GestionPayroll gestionPayroll left join gestionPayroll.generatedPayrollList element";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BusinessUnit getBusinessUnitStaticFilter() {
        return businessUnitStaticFilter;
    }

    public void setBusinessUnitStaticFilter(BusinessUnit businessUnitStaticFilter) {
        this.businessUnitStaticFilter = businessUnitStaticFilter;
    }

    public GeneratedPayrollType getOfficialTypeConstant() {
        return GeneratedPayrollType.OFFICIAL;
    }

    public PayrollGenerationType getGenerationBySalaryConstant() {
        return PayrollGenerationType.GENERATION_BY_SALARY;
    }

    @Override
    public void clear() {
        setYear(null);
        setMonth(null);
        super.clear();
    }
}
