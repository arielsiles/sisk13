package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */

@Name("payrollGenerationCycleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
public class PayrollGenerationCycleDataModel extends QueryDataModel<Long, PayrollGenerationCycle> {
    private static final String[] RESTRICTIONS =
            {
                    "payrollGenerationCycle.businessUnit = #{payrollGenerationCycleDataModel.criteria.businessUnit}",
                    "payrollGenerationCycle.month = #{payrollGenerationCycleDataModel.criteria.month}",
                    "payrollGenerationCycle.gestion = #{payrollGenerationCycleDataModel.criteria.gestion}"
            };

    @Create
    public void init() {
        sortProperty = "payrollGenerationCycle.gestion, payrollGenerationCycle.month";
    }

    @Override
    public String getEjbql() {
        return "select payrollGenerationCycle " +
                "from PayrollGenerationCycle payrollGenerationCycle " +
                "left join fetch payrollGenerationCycle.gestion gestion " +
                "left join fetch payrollGenerationCycle.businessUnit businessUnit " +
                "left join fetch businessUnit.organization organization";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}