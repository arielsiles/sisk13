package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ScheduleException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Name("scheduleExceptionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SCHEDULEEXCEPTION','VIEW')}")
public class ScheduleExceptionDataModel extends QueryDataModel<Long, ScheduleException> {

    private static final String[] RESTRICTIONS = {
            "lower(concat(concat(concat(coalesce(employee.firstName,''),' '),concat(coalesce(employee.lastName,''),' ')),coalesce(employee.maidenName,''))) like concat('%', concat(lower(#{scheduleExceptionDataModel.employeeName}), '%'))",
            "scheduleException.date >= #{scheduleExceptionDataModel.startDate}",
            "scheduleException.date <= #{scheduleExceptionDataModel.endDate}"};

    private String employeeName;
    private java.util.Date startDate;
    private java.util.Date endDate;

    @Create
    public void init() {
        sortProperty = "scheduleException.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select scheduleException from ScheduleException scheduleException"
                + " left join fetch scheduleException.contract contract"
                + " left join fetch contract.employee employee"
                + " left join fetch scheduleException.workShift workShift";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        employeeName = null;
        startDate = null;
        endDate = null;
        super.clear();
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public java.util.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }
}
