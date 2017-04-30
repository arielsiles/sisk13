package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 */
@Name("gestionPayrollAllSearchDataModel")
@Scope(ScopeType.PAGE)
public class GestionPayrollAllSearchDataModel extends QueryDataModel<Long, GestionPayroll> {

    private static final String[] RESTRICTIONS = {
            "lower(gestionPayroll.gestionName) like concat('%', concat(lower(#{gestionPayrollAllSearchDataModel.criteria.gestionName}), '%'))",
            "gestionPayroll.gestion.year = #{gestionPayrollAllSearchDataModel.year}",
            "gestionPayroll.month = #{gestionPayrollAllSearchDataModel.month}",
            "gestionPayroll.businessUnit = #{gestionPayrollAllSearchDataModel.businessUnitStaticFilter}"
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
        return "select gestionPayroll from GestionPayroll gestionPayroll ";
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

    @Override
    public void clear() {
        setYear(null);
        setMonth(null);
        super.clear();
    }
}
