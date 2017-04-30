package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.BudgetProgram;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * BudgetProgramDataModel
 *
 * @author
 * @version 2.0
 */
@Name("budgetProgramDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BUDGETPROGRAM','VIEW')}")
public class BudgetProgramDataModel extends QueryDataModel<Long, BudgetProgram> {
    private static final String[] RESTRICTIONS = {
            "lower(budgetProgram.name) like concat('%', concat(lower(#{budgetProgramDataModel.criteria.name}), '%'))",
            "lower(budgetProgram.code) like concat(lower(#{budgetProgramDataModel.criteria.code}), '%')"};

    @Create
    public void init() {
        sortProperty = "budgetProgram.name";
    }

    @Override
    public String getEjbql() {
        return "select budgetProgram from BudgetProgram budgetProgram";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
