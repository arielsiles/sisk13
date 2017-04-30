package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.FiscalPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
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
@Name("fiscalPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class FiscalPayrollDataModel extends QueryDataModel<Long, FiscalPayroll> {

    private PayrollGenerationCycle payrollGenerationCycle;
    private String personalIdentifier;
    private String name;


    private static final String[] RESTRICTIONS =
            {
                    "payrollGenerationCycle = #{fiscalPayrollDataModel.payrollGenerationCycle}",
                    "fiscalPayroll.personalIdentifier = #{fiscalPayrollDataModel.personalIdentifier}",
                    "lower(fiscalPayroll.name) like concat('%', concat(lower(#{fiscalPayrollDataModel.name}), '%'))"
            };

    @Create
    public void init() {
        sortProperty = "fiscalPayroll.number";
    }

    @Override
    public String getEjbql() {
        return "SELECT fiscalPayroll FROM FiscalPayroll fiscalPayroll " +
                " LEFT JOIN fiscalPayroll.payrollGenerationCycle payrollGenerationCycle ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        if (payrollGenerationCycle == null) {
            payrollGenerationCycle = (PayrollGenerationCycle) Component.getInstance("payrollGenerationCycle");
        }

        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
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
