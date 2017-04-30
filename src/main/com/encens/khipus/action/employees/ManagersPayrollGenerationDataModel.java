package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ManagersPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * ManagersPayrollGenerationDataModel
 *
 * @author
 * @version 2.2
 */
@Name("managersPayrollGenerationDataModel")
@Scope(ScopeType.PAGE)
public class ManagersPayrollGenerationDataModel extends QueryDataModel<Long, ManagersPayroll> {
    private static final String[] RESTRICTIONS =
            {
                    "managersPayroll.generatedPayroll=#{accountingRecord.generatedPayroll}",
                    "lower(employee.idNumber) like concat(lower(#{accountingRecord.idNumber}),'%')",
                    "managersPayroll.liquid >= #{accountingRecord.lesserAmount}",
                    "managersPayroll.liquid <= #{accountingRecord.higherAmount}",
                    "managersPayroll.costCenter=#{accountingRecord.costCenter}"};

    @Create
    public void init() {
//        sortProperty = "employee.lastName,employee.maidenName,employee.firstName";
        sortProperty = "managersPayroll.employee.lastName";
    }

    @Override
    public String getEjbql() {
        return "SELECT managersPayroll FROM ManagersPayroll managersPayroll " +
                " left join fetch managersPayroll.employee employee" +
                " WHERE #{true}=#{not empty accountingRecord.generatedPayroll}" +
                " and ((#{accountingRecordAction.managed}=#{false} and managersPayroll.hasAccountingRecord=#{false} and managersPayroll.hasActivePayment=#{true}) or " +
                " (#{accountingRecordAction.managed}=#{true} and managersPayroll.hasAccountingRecord=#{true} and managersPayroll.hasActivePayment=#{true}))";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
