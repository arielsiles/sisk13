package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.GeneratedPayroll;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("categoryTributaryPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class CategoryTributaryPayrollDataModel extends QueryDataModel<Long, CategoryTributaryPayroll> {

    private GeneratedPayroll generatedPayroll;
    private String code;
    private String name;


    private static final String[] RESTRICTIONS =
            {
                    "generatedPayroll = #{categoryTributaryPayrollDataModel.generatedPayroll}",
                    "categoryTributaryPayroll.code = #{categoryTributaryPayrollDataModel.code}",
                    "lower(categoryTributaryPayroll.name) like concat('%', concat(lower(#{categoryTributaryPayrollDataModel.name}), '%'))"
            };

    @Create
    public void init() {
        sortProperty = "categoryTributaryPayroll.name";
    }

    @Override
    public String getEjbql() {
        return "SELECT categoryTributaryPayroll FROM CategoryTributaryPayroll categoryTributaryPayroll " +
                " LEFT JOIN categoryTributaryPayroll.generatedPayroll generatedPayroll ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public GeneratedPayroll getGeneratedPayroll() {
        if (generatedPayroll == null) {
            generatedPayroll = (GeneratedPayroll) Component.getInstance("generatedPayroll");
        }
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @End(beforeRedirect = true)
    public String cancel() {
        return Outcome.CANCEL;
    }
}
