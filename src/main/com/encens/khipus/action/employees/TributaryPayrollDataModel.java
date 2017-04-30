package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.TributaryPayroll;
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
@Name("tributaryPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class TributaryPayrollDataModel extends QueryDataModel<Long, TributaryPayroll> {

    private PayrollGenerationCycle payrollGenerationCycle;
    private String code;
    private String name;


    private static final String[] RESTRICTIONS =
            {
                    "payrollGenerationCycle = #{tributaryPayrollDataModel.payrollGenerationCycle}",
                    "tributaryPayroll.code = #{tributaryPayrollDataModel.code}",
                    "lower(tributaryPayroll.name) like concat('%', concat(lower(#{tributaryPayrollDataModel.name}), '%'))"
            };

    @Create
    public void init() {
        sortProperty = "tributaryPayroll.name";
    }

    @Override
    public String getEjbql() {
        return "SELECT tributaryPayroll FROM TributaryPayroll tributaryPayroll " +
                " LEFT JOIN tributaryPayroll.payrollGenerationCycle payrollGenerationCycle ";
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
