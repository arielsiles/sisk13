package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.GestionPayrollType;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.service.employees.GestionPayrollService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("genCycleGestionPayrollDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GESTIONPAYROLL','VIEW')}")
public class GenCycleGestionPayrollDataModel extends QueryDataModel<Long, GestionPayroll> {
    @In(required = false)
    GestionPayrollService gestionPayrollService;

    private Integer year;
    private Month month;
    private GestionPayrollType gestionPayrollType;
    private PayrollGenerationCycle payrollGenerationCycle;

    private static final String[] RESTRICTIONS = {
            "lower(gestionPayroll.gestionName) like concat('%', concat(lower(#{genCycleGestionPayrollDataModel.criteria.gestionName}), '%'))",
            "gestion.year = #{genCycleGestionPayrollDataModel.year}",
            "gestionPayroll.month = #{genCycleGestionPayrollDataModel.month}",
            "gestionPayroll.gestionPayrollType = #{genCycleGestionPayrollDataModel.gestionPayrollType}",
            "gestionPayroll.payrollGenerationCycle = #{genCycleGestionPayrollDataModel.payrollGenerationCycle}"
    };


    @Create
    public void init() {
        sortAsc = true;
        sortProperty = "businessUnit.position,jobCategory.position,gestionPayroll.gestionName";
    }

    @Override
    public String getEjbql() {
        return "select gestionPayroll from GestionPayroll gestionPayroll" +
                " left join fetch gestionPayroll.businessUnit businessUnit" +
                " left join fetch businessUnit.organization organization" +
                " left join fetch gestionPayroll.jobCategory jobCategory" +
                " left join fetch gestionPayroll.gestion gestion" +
                " left join fetch gestionPayroll.exchangeRate exchangeRate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void setLastGestionPayroll() {
        GestionPayroll gestionPayroll = gestionPayrollService.findAvailableGestionPayroll();
        if (null != gestionPayroll) {
            setMonth(gestionPayroll.getMonth());
            setYear(gestionPayroll.getGestion().getYear());
        } else {
            Calendar today = Calendar.getInstance();
            /*set month as current month*/
            setMonth(Month.getMonth(today.get(Calendar.MONTH) + 1));
            setYear(today.get(Calendar.YEAR));
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void gestionPayrollTypeChanged() {
        setMonth(null);
    }

    public boolean isShowSalaryFields() {
        return null == getGestionPayrollType() ||
                getGestionPayrollType().equals(GestionPayrollType.SALARY);
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

    public GestionPayrollType getGestionPayrollType() {
        return gestionPayrollType;
    }

    public void setGestionPayrollType(GestionPayrollType gestionPayrollType) {
        this.gestionPayrollType = gestionPayrollType;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void clear() {
        setYear(null);
        setMonth(null);
        setGestionPayrollType(null);
        setPayrollGenerationCycle(null);
        super.clear();
        update();
        search();
    }

}