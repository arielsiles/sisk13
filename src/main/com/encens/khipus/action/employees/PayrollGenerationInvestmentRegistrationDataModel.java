package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.PayrollGenerationInvestmentRegistration;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Name("payrollGenerationInvestmentRegistrationDataModel")
@Scope(ScopeType.PAGE)
public class PayrollGenerationInvestmentRegistrationDataModel extends QueryDataModel<Long, PayrollGenerationInvestmentRegistration> {
    private static final String[] RESTRICTIONS =
            {
                    "payrollGenerationInvestmentRegistration.payrollGenerationCycle = #{payrollGenerationCycleAction.instance}"
            };

    @Create
    public void init() {
        sortProperty = "socialWelfareEntity.name, payrollGenerationInvestmentRegistration.amount";
    }

    @Override
    public String getEjbql() {
        return "select payrollGenerationInvestmentRegistration " +
                " from PayrollGenerationInvestmentRegistration payrollGenerationInvestmentRegistration " +
                " left join fetch payrollGenerationInvestmentRegistration.socialWelfareEntity socialWelfareEntity " +
                " left join fetch payrollGenerationInvestmentRegistration.currency currency" +
                " left join fetch payrollGenerationInvestmentRegistration.payableDocument payableDocument";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}