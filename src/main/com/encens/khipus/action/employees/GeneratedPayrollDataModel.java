package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.GestionPayroll;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for GeneratedPayroll
 *
 * @author
 */

@Name("generatedPayrollDataModel")
@Scope(ScopeType.PAGE)
public class GeneratedPayrollDataModel extends QueryDataModel<Long, GeneratedPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(generatedPayroll.name) like concat('%', concat(lower(#{generatedPayrollDataModel.criteria.name}), '%'))",
            "generatedPayroll.generatedPayrollType = #{generatedPayrollDataModel.generatedPayrollType}"
    };

    public GestionPayroll gestionPayroll;
    public GeneratedPayrollType generatedPayrollType;

    @Create
    public void init() {
        setGestionPayroll((GestionPayroll) Component.getInstance("gestionPayroll"));
        sortProperty = "generatedPayroll.generationDate";
        this.sortAsc = false;
    }

    @Override
    public void search() {
        setGestionPayroll((GestionPayroll) Component.getInstance("gestionPayroll"));
        super.search();
    }

    @Override
    public String getEjbql() {
        return "select generatedPayroll from GeneratedPayroll generatedPayroll " +
                "where generatedPayroll.gestionPayroll.id=" + getGestionPayroll().getId();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    public GeneratedPayrollType getGeneratedPayrollType() {
        return generatedPayrollType;
    }

    public void setGeneratedPayrollType(GeneratedPayrollType generatedPayrollType) {
        this.generatedPayrollType = generatedPayrollType;
    }
}