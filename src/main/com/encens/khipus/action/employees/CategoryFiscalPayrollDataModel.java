package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.CategoryFiscalPayroll;
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
@Name("categoryFiscalPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class CategoryFiscalPayrollDataModel extends QueryDataModel<Long, CategoryFiscalPayroll> {

    private GeneratedPayroll generatedPayroll;
    private String personalIdentifier;
    private String name;


    private static final String[] RESTRICTIONS =
            {
                    "generatedPayroll = #{categoryFiscalPayrollDataModel.generatedPayroll}",
                    "categoryFiscalPayroll.personalIdentifier = #{categoryFiscalPayrollDataModel.personalIdentifier}",
                    "lower(categoryFiscalPayroll.name) like concat('%', concat(lower(#{categoryFiscalPayrollDataModel.name}), '%'))"
            };

    @Create
    public void init() {
        sortProperty = "categoryFiscalPayroll.number";
    }

    @Override
    public String getEjbql() {
        return "SELECT categoryFiscalPayroll FROM CategoryFiscalPayroll categoryFiscalPayroll " +
                " LEFT JOIN categoryFiscalPayroll.generatedPayroll generatedPayroll ";
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


    public String getPersonalIdentifier() {
        return personalIdentifier;
    }

    public void setPersonalIdentifier(String personalIdentifier) {
        this.personalIdentifier = personalIdentifier;
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
