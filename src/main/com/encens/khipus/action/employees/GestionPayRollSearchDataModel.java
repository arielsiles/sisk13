package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
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
@Name("gestionPayRollSearchDataModel")
@Scope(ScopeType.PAGE)
public class GestionPayRollSearchDataModel extends QueryDataModel<Long, GestionPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(gestionPayroll.gestionName) like concat('%', concat(lower(#{gestionPayRollSearchDataModel.criteria.gestionName}), '%'))",
            "gestionPayroll.gestion.year = #{gestionPayRollSearchDataModel.year}",
            "gestionPayroll.month = #{gestionPayRollSearchDataModel.month}",
            "gestionPayroll.jobCategory.payrollGenerationType = #{gestionPayRollSearchDataModel.payrollGenerationType}",
            "gestionPayroll.businessUnit = #{gestionPayRollSearchDataModel.businessUnitFilter}"

    };

    private Integer year;

    private Month month;

    private PayrollGenerationType payrollGenerationType;

    private boolean enablePayrollGenerationTypeFilter = true;

    private BusinessUnit businessUnitFilter;

    private boolean enableCleaningBusinessUnitFilter = true;

    @Create
    public void init() {
        sortAsc = true;
        sortProperty = "gestionPayroll.businessUnit.position,gestionPayroll.jobCategory.position,gestionPayroll.gestionName";
    }

    @Override
    public String getEjbql() {
        return "select gestionPayroll from GestionPayroll gestionPayroll";
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

    public PayrollGenerationType getPayrollGenerationType() {
        return payrollGenerationType;
    }

    public void setPayrollGenerationType(PayrollGenerationType payrollGenerationType) {
        this.payrollGenerationType = payrollGenerationType;
    }

    public boolean isEnablePayrollGenerationTypeFilter() {
        return enablePayrollGenerationTypeFilter;
    }

    public void setEnablePayrollGenerationTypeFilter(boolean enablePayrollGenerationTypeFilter) {
        this.enablePayrollGenerationTypeFilter = enablePayrollGenerationTypeFilter;
    }

    public BusinessUnit getBusinessUnitFilter() {
        return businessUnitFilter;
    }

    public void setBusinessUnitFilter(BusinessUnit businessUnitFilter) {
        this.businessUnitFilter = businessUnitFilter;
    }

    public boolean isEnableCleaningBusinessUnitFilter() {
        return enableCleaningBusinessUnitFilter;
    }

    public void setEnableCleaningBusinessUnitFilter(boolean enableCleaningBusinessUnitFilter) {
        this.enableCleaningBusinessUnitFilter = enableCleaningBusinessUnitFilter;
    }

    @Override
    public void clear() {
        setYear(null);
        setMonth(null);
        if (enablePayrollGenerationTypeFilter) {
            setPayrollGenerationType(null);
        }

        if (enableCleaningBusinessUnitFilter) {
            setBusinessUnitFilter(null);
        }

        super.clear();
    }
}
