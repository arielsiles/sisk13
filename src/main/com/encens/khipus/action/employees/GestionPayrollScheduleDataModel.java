package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.GestionPayrollSchedule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for GestionPayrollSchedule
 *
 * @author
 * @version 2.26
 */

@Name("gestionPayrollScheduleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GESTIONPAYROLLSCHEDULE','VIEW')}")
public class GestionPayrollScheduleDataModel extends QueryDataModel<Long, GestionPayrollSchedule> {
    private Integer year;

    private static final String[] RESTRICTIONS = {
            "lower(gestionPayrollSchedule.name) like concat('%', concat(lower(#{gestionPayrollScheduleDataModel.criteria.name}), '%'))",
            "gestionPayrollSchedule.gestion.year = #{gestionPayrollScheduleDataModel.year}"
    };


    @Create
    public void init() {
        sortAsc = true;
        sortProperty = "gestionPayrollSchedule.gestion.year";
    }

    @Override
    public String getEjbql() {
        return "select gestionPayrollSchedule from GestionPayrollSchedule gestionPayrollSchedule";
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

    @Override
    public void clear() {
        setYear(null);
        super.clear();
    }

}