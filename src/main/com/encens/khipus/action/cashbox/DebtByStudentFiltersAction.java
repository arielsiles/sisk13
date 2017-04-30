package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.ViewAction;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.cashbox.sql.DebtByStudentReportSql;
import com.encens.khipus.model.academics.AcademicFaculty;
import com.encens.khipus.model.academics.Carrer;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.model.cashbox.CashboxAccount;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.12
 */
@Name("debtByStudentFiltersAction")
@Scope(ScopeType.PAGE)
public class DebtByStudentFiltersAction extends ViewAction {
    private ExecutorUnit executorUnit = null;

    private AcademicFaculty faculty = null;

    private Carrer carrer = null;

    private Integer year = DateUtils.getCurrentYear(new Date());

    private Integer period;

    private List<CashboxAccount> cashboxAccountList;

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new DebtByStudentReportSql();
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        String executorUnitCode = "%";
        if (null != executorUnit) {
            executorUnitCode = executorUnit.getId().toString();
        }

        String facultyCode = "%";
        String careerCode = "%";
        if (!"%".equals(executorUnitCode)) {
            if (null != faculty) {
                facultyCode = faculty.getId().toString();
            }

            if (null != carrer) {
                careerCode = carrer.getStudyPlan();
            }
        }

        List<String> cashboxAccountIdList = null;
        if (cashboxAccountList != null) {
            cashboxAccountIdList = new ArrayList<String>();
            for (CashboxAccount cashboxAccount : cashboxAccountList) {
                cashboxAccountIdList.add(cashboxAccount.getId().toString());
            }
        }

        ((DebtByStudentReportSql) sqlQuery).setExecutorUnitCode(executorUnitCode);
        ((DebtByStudentReportSql) sqlQuery).setCareer(careerCode);
        ((DebtByStudentReportSql) sqlQuery).setFaculty(facultyCode);
        ((DebtByStudentReportSql) sqlQuery).setYear(year);
        ((DebtByStudentReportSql) sqlQuery).setPeriod(period);
        ((DebtByStudentReportSql) sqlQuery).setDetailCodeList(cashboxAccountIdList);
    }

    public ExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(ExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public AcademicFaculty getFaculty() {
        return faculty;
    }

    public void setFaculty(AcademicFaculty faculty) {
        this.faculty = faculty;
    }

    public Carrer getCarrer() {
        return carrer;
    }

    public void setCarrer(Carrer carrer) {
        this.carrer = carrer;
    }

    public boolean isExecutorUnitSelected() {
        return null != executorUnit;
    }

    public List<CashboxAccount> getCashboxAccountList() {
        return cashboxAccountList;
    }

    public void setCashboxAccountList(List<CashboxAccount> cashboxAccountList) {
        this.cashboxAccountList = cashboxAccountList;
    }

    public List<Integer> getAccountIds() {
        return Arrays.asList(4904, 9568, 9569, 9570, 4931, 9603, 9604, 9605, 9606, 9607, 9608, 9609, 9610, 9611, 9612, 9613, 9614, 9615, 9809, 9810, 9863, 9864, 5054, 4878, 4873, 4874, 4875, 4876, 4877, 4923, 4889, 4913, 4914, 4915, 4916, 4917, 4932, 4933, 4934, 4935, 4936, 4937, 9529, 4872, 9527, 9526, 9528, 4918, 9940, 9682, 9943, 9683, 9939, 9942, 9941, 9944);
    }
}


